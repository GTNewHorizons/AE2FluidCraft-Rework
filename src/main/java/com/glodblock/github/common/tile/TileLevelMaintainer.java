package com.glodblock.github.common.tile;

import java.util.concurrent.Future;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.AeStackInventoryImpl;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.CraftingLink;
import appeng.helpers.NonNullArrayIterator;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileLevelMaintainer extends AENetworkTile
        implements IAEAppEngInventory, IGridTickable, ICraftingRequester, IPowerChannelState {

    public static final int REQ_COUNT = 5;
    public final InventoryRequest requests = new InventoryRequest(this);
    private final BaseActionSource source;
    private final IInventory inv = new AeItemStackHandler(requests.getRequestStacks());
    private boolean isPowered = false;

    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
    }

    public AeStackInventory<IAEItemStack> getRequestSlots() {
        return requests.getRequestStacks();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(new NonNullArrayIterator<>(this.requests.getLinks()));
    }

    private IAEItemStack setState(ICraftingLink link, IAEItemStack items) {
        if (items != null) {
            requests.setState(link, State.Exporting);
        } else {
            requests.setState(link, State.Crafting);
        }
        return items;
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        try {
            if (this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = Math
                        .ceil(ItemFluidDrop.isFluidStack(items) ? items.getStackSize() / 1000D : items.getStackSize());
                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    if (ItemFluidDrop.isFluidStack(items)) {
                        IAEFluidStack notInserted = this.getProxy().getStorage().getFluidInventory()
                                .injectItems(ItemFluidDrop.getAeFluidStack(items), mode, this.source);
                        if (notInserted != null) {
                            items.setStackSize(notInserted.getStackSize());
                            return setState(link, items);
                        } else {
                            return null;
                        }
                    } else {
                        return setState(
                                link,
                                this.getProxy().getStorage().getItemInventory().injectItems(items, mode, this.source));
                    }
                }
            }
        } catch (GridAccessException e) {
            AELog.debug(e);
        }
        return setState(link, items);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (this.requests.getLinks() != null) {
            for (int x = 0; x < this.requests.getLinks().length; x++) {
                if (this.requests.getLinks()[x] == link) {
                    this.requests.getLinks()[x] = link;
                    return;
                }
            }
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        return this.canDoBusWork() ? this.doWork() : TickRateModulation.IDLE;
    }

    private TickRateModulation doWork() {
        if (!this.getProxy().isActive() || !this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        boolean didSomething = false;
        try {
            ICraftingGrid cg = this.getProxy().getCrafting();
            IGrid grid = this.getProxy().getGrid();
            final IItemList<IAEItemStack> inv = this.getProxy().getStorage().getItemInventory().getStorageList();
            for (int i = 0; i < REQ_COUNT; i++) {
                IAEItemStack is = requests.getRequestQtyStack(i);
                if (is != null && requests.getBatchSize(i) > 0) {
                    IAEItemStack craftItem = requests.getCraftItem(i);
                    if (this.requests.isDone(i)) this.requests.setState(i, State.Idling);
                    if (cg.canEmitFor(craftItem) || cg.isRequesting(craftItem)
                            || inv.findPrecise(is) == null
                            || !inv.findPrecise(is).isCraftable()
                            || inv.findPrecise(is).getStackSize() >= is.getStackSize()
                            || !this.requests.isDone(i))
                        continue;
                    // do crafting
                    Future<ICraftingJob> jobTask = requests.getJobs()[i];
                    if (jobTask == null) {
                        requests.getJobs()[i] = cg.beginCraftingJob(getWorldObj(), grid, this.source, craftItem, null);
                    } else if (jobTask.isDone()) {
                        requests.setState(i, State.Idling);
                        try {
                            ICraftingJob job = jobTask.get();
                            if (job != null) {
                                ICraftingLink link = cg.submitJob(job, this, null, false, this.source);
                                requests.jobs[i] = null;
                                if (link != null) {
                                    didSomething = true;
                                    requests.setState(i, State.Crafting);
                                    requests.getLinks()[i] = link;
                                }
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
        } catch (final GridAccessException ignore) {}
        return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {
        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            // NO-OP
        }
    }

    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    @Override
    public void gridChanged() {}

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    public InventoryRequest getRequestInventory() {
        return this.requests;
    }

    public IInventory getInventory() {
        return inv;
    }

    public void updateQuantity(int idx, long size) {
        this.requests.updateQuantity(idx, size);
    }

    public void updateBatchSize(int idx, long size) {
        this.requests.updateBatchSize(idx, size);
    }

    public void setRequestStatus(int idx, boolean enable) {
        this.requests.setEnable(idx, enable);
    }

    private void readLinkFromNBT(NBTTagCompound data) {
        try {
            for (int i = 0; i < this.requests.getLinks().length; i++) {
                final NBTTagCompound link = data.getCompoundTag("links-" + i);
                if (link != null && link.hasNoTags()) {
                    this.requests.getLinks()[i] = null;
                } else {
                    this.requests.getLinks()[i] = AEApi.instance().storage().loadCraftingLink(link, this);
                    if (!this.requests.isDone(i)) this.requests.setState(i, State.Crafting);
                }
            }
        } catch (Exception ignore) {}
    }

    private NBTTagCompound writeLinkToNBT(NBTTagCompound data) {
        for (int i = 0; i < this.requests.getLinks().length; i++) {
            if (this.requests.getLinks()[i] != null) {
                CraftingLink link = (CraftingLink) this.requests.getLinks()[i];
                NBTTagCompound linkData = new NBTTagCompound();
                link.writeToNBT(linkData);
                data.setTag("links-" + i, linkData);
            }
        }
        return data;
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        this.requests.requestStacks.writeToNbt(data, "Inventory");
        this.requests.requestBatches.writeToNbt(data, "Batch");
        this.requests.requestQtys.writeToNbt(data, "Count");
        return writeLinkToNBT(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.requests.requestStacks.readFromNbt(data, "Inventory");
        this.requests.requestBatches.readFromNbt(data, "Batch");
        this.requests.requestQtys.readFromNbt(data, "Count");
        this.readLinkFromNBT(data);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream(final ByteBuf data) {
        final boolean oldPower = this.isPowered;
        this.isPowered = data.readBoolean();
        return this.isPowered != oldPower;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream(final ByteBuf data) {
        data.writeBoolean(this.isActive());
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        this.updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        this.updatePowerState();
    }

    private void updatePowerState() {
        boolean newState = false;

        try {
            newState = this.getProxy().isActive()
                    && this.getProxy().getEnergy().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG)
                            > 0.0001;
        } catch (final GridAccessException ignored) {

        }
        if (newState != this.isPowered) {
            this.isPowered = newState;
            this.markForUpdate();
        }
    }

    public enum State {
        Nothing,
        Idling,
        Crafting,
        Exporting
    }

    public static class InventoryRequest {

        private final AeStackInventoryImpl<IAEItemStack> requestStacks;
        private final AeStackInventoryImpl<IAEItemStack> requestBatches;
        private final AeStackInventoryImpl<IAEItemStack> requestQtys;
        private final Future<ICraftingJob>[] jobs;
        private final ICraftingLink[] links;
        private final State[] state = new State[REQ_COUNT];

        @SuppressWarnings("unchecked")
        public InventoryRequest(TileLevelMaintainer tile) {
            this.requestStacks = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.requestBatches = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.requestQtys = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.jobs = new Future[REQ_COUNT];
            this.links = new ICraftingLink[REQ_COUNT];
        }

        public void setEnable(int idx, boolean enable) {
            IAEItemStack ias = this.getRequestStacks().getStack(idx);
            IAEItemStack ias1 = this.getRequestBatches().getStack(idx);
            if (ias != null && ias1 != null) {
                ItemStack is = ias1.getItemStack();
                NBTTagCompound data = new NBTTagCompound();
                data.setBoolean("Enable", enable);
                is.setTagCompound(data);
                IAEItemStack i = AEItemStack.create(is);
                i.setStackSize(ias1.getStackSize());
                this.getRequestBatches().setStack(idx, i);
            }
        }

        public State getState(int idx) {
            IAEItemStack ias = this.getRequestStacks().getStack(idx);
            if (ias == null) {
                return State.Nothing;
            } else {
                return this.state[idx] == null ? State.Idling : this.state[idx];
            }
        }

        public void setState(ICraftingLink link, State state) {
            int i = 0;
            for (ICraftingLink link1 : this.getLinks()) {
                if (link == link1) {
                    this.setState(i, state);
                    break;
                }
                i++;
            }
        }

        public void setState(int idx, State state) {
            this.state[idx] = state;
        }

        public boolean isEnable(int idx) {
            IAEItemStack ias = this.getRequestBatches().getStack(idx);
            if (ias != null) {
                ItemStack is = ias.getItemStack();
                try {
                    return is.getTagCompound().getBoolean("Enable");
                } catch (NullPointerException e) {
                    // support old version
                    return true;
                }
            } else {
                return true;
            }
        }

        public boolean isDone(int index) {
            if (this.getLinks()[index] != null) {
                return this.getLinks()[index].isDone() || this.getLinks()[index].isCanceled();
            }
            return true;
        }

        public Future<ICraftingJob>[] getJobs() {
            return this.jobs;
        }

        public ICraftingLink[] getLinks() {
            return links;
        }

        public AeStackInventory<IAEItemStack> getRequestQtys() {
            return this.requestQtys;
        }

        public AeStackInventory<IAEItemStack> getRequestBatches() {
            return this.requestBatches;
        }

        public AeStackInventory<IAEItemStack> getRequestStacks() {
            return this.requestStacks;
        }

        public void updateQuantity(int idx, long size) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            if (is != null) {
                IAEItemStack i = is.copy();
                i.setStackSize(size);
                this.getRequestQtys().setStack(idx, i);
            }
        }

        public void updateBatchSize(int idx, long size) {
            IAEItemStack ias = this.getRequestStacks().getStack(idx);
            if (ias != null) {
                ItemStack is = ias.copy().getItemStack();
                NBTTagCompound data = new NBTTagCompound();
                data.setBoolean("Enable", true);
                is.setTagCompound(data);
                IAEItemStack i = AEItemStack.create(is);
                i.setStackSize(size);
                this.getRequestBatches().setStack(idx, i);
            }
        }

        public IAEItemStack getRequestQtyStack(int idx) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            if (is == null) return null;
            IAEItemStack qis = this.getRequestQtys().getStack(idx);
            if (qis == null || !is.isSameType(qis)) return null;
            return this.getRequestQtys().getStack(idx);
        }

        public long getQuantity(int idx) {
            IAEItemStack ias = this.getRequestQtyStack(idx);
            if (ias == null) {
                return 0;
            }
            return ias.getStackSize();
        }

        public long getBatchSize(int idx) {
            if (!this.isEnable(idx)) return 0;
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            IAEItemStack is1 = this.getRequestBatches().getStack(idx);
            if (is != null && is1 != null) {
                return is1.getStackSize();
            }
            return 0;
        }

        public IAEItemStack getCraftItem(int idx) {
            IAEItemStack is = this.getRequestQtyStack(idx);
            if (is != null) {
                IAEItemStack ias = is.copy();
                ias.setStackSize(this.getBatchSize(idx));
                return ias;
            }
            return null;
        }
    }
}
