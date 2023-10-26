package com.glodblock.github.common.tile;

import java.util.concurrent.Future;
import java.util.function.UnaryOperator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.common.Config;
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
import appeng.helpers.NonNullArrayIterator;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileLevelMaintainer extends AENetworkTile
        implements IAEAppEngInventory, IGridTickable, ICraftingRequester, IPowerChannelState, ILevelViewable {

    public static final int REQ_COUNT = 5;
    public final InventoryRequest requests = new InventoryRequest(this);
    private final BaseActionSource source;
    private final IInventory inv = new AeItemStackHandler(requests.requestStacks);
    private boolean isPowered = false;

    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        source = new MachineSource(this);
    }

    public AeStackInventory<IAEItemStack> getRequestSlots() {
        return requests.requestStacks;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(new NonNullArrayIterator<>(requests.links));
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        int idx = requests.getIdxByLink(link);
        try {
            if (getProxy().isActive()) {
                final IEnergyGrid energy = getProxy().getEnergy();
                final double power = Math
                        .ceil(ItemFluidDrop.isFluidStack(items) ? items.getStackSize() / 1000D : items.getStackSize());
                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    if (ItemFluidDrop.isFluidStack(items)) {
                        IAEFluidStack notInjectedItems = getProxy().getStorage().getFluidInventory()
                                .injectItems(ItemFluidDrop.getAeFluidStack(items), mode, source);
                        if (notInjectedItems != null) {
                            items.setStackSize(notInjectedItems.getStackSize());
                            requests.updateState(idx, State.Export);

                            return items;
                        } else {
                            return null;
                        }
                    } else {
                        return getProxy().getStorage().getItemInventory().injectItems(items, mode, source);
                    }
                }
            }
        } catch (GridAccessException e) {
            AELog.debug(e);
        }

        return items;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        for (int x = 0; x < REQ_COUNT; x++) {
            if (requests.getLink(x) == link) {
                requests.updateLink(x, link);
                return;
            }
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(Config.levelMaintainerMinTicks, Config.levelMaintainerMaxTicks, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        return canDoBusWork() ? doWork() : TickRateModulation.IDLE;
    }

    private TickRateModulation doWork() {
        if (!getProxy().isActive() || !canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        try {
            final ICraftingGrid craftingGrid = getProxy().getCrafting();
            final IGrid grid = getProxy().getGrid();
            final IItemList<IAEItemStack> inv = getProxy().getStorage().getItemInventory().getStorageList();
            for (int i = 0; i < REQ_COUNT; i++) {
                long quantity = requests.getQuantity(i);
                long batchSize = requests.getBatchSize(i);
                boolean isEnable = requests.isEnable(i);
                if (!isEnable || batchSize == 0) requests.updateState(i, State.None);
                if (batchSize > 0) {
                    IAEItemStack craftItem = requests.getCraftItem(i);
                    IAEItemStack aeItem = inv.findPrecise(craftItem);
                    boolean isDone = requests.isDone(i);
                    boolean isCraftable = aeItem != null && aeItem.isCraftable();
                    boolean shouldCraft = isCraftable && aeItem.getStackSize() < quantity;
                    if (isDone) requests.updateState(i, State.Idle);
                    if (!isCraftable) requests.updateState(i, State.Error);
                    if (craftingGrid.canEmitFor(craftItem) || craftingGrid.isRequesting(craftItem)
                            || !isDone
                            || !shouldCraft)
                        continue;
                    // do crafting
                    Future<ICraftingJob> jobTask = requests.getJob(i);
                    if (jobTask == null) {
                        requests.updateJob(
                                i,
                                craftingGrid.beginCraftingJob(getWorldObj(), grid, source, craftItem, null));
                        requests.updateState(i, State.Craft);

                        // calculate only one job per tick request
                        return TickRateModulation.SAME;
                    } else if (jobTask.isDone()) {
                        requests.updateState(i, State.Craft);
                        try {
                            ICraftingJob job = jobTask.get();
                            if (job != null) {
                                ICraftingLink link = craftingGrid.submitJob(job, this, null, false, source);
                                requests.updateJob(i, null);
                                if (link != null) {
                                    requests.updateState(i, State.Craft);
                                    requests.updateLink(i, link);

                                    // submit only one job per tick request
                                    return TickRateModulation.SAME;
                                } else {
                                    requests.updateState(i, State.Error);
                                }
                            } else {
                                requests.updateState(i, State.Error);
                            }
                        } catch (Exception ignored) {
                            requests.updateState(i, State.Error);
                        }
                    }
                }
            }
        } catch (final GridAccessException ignore) {

        }

        return TickRateModulation.SAME;
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
        return getProxy().isActive();
    }

    @Override
    public void gridChanged() {}

    @Override
    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public boolean isActive() {
        return isPowered;
    }

    public InventoryRequest getRequestInventory() {
        return requests;
    }

    public IInventory getInventory() {
        return inv;
    }

    public IInventory getInventoryByName(String name) {
        if (name == "config") return new AeItemStackHandler(requests.requestStacks);

        return null;
    }

    public void updateQuantity(int idx, long size) {
        requests.updateQuantity(idx, size);
    }

    public void updateBatchSize(int idx, long size) {
        requests.updateBatchSize(idx, size);
    }

    public void updateStatus(int idx, boolean enable) {
        requests.updateStatus(idx, enable);
    }

    private void readLinkFromNBT(NBTTagCompound data) {
        for (int i = 0; i < REQ_COUNT; i++) {
            final NBTTagCompound stackData = requests.getItemStack(i).getTagCompound();
            if (stackData != null && stackData.hasNoTags()
                    && !stackData.hasKey(TLMTags.Link.tagName)
                    && stackData.getCompoundTag(TLMTags.Link.tagName).hasNoTags()) {
                requests.updateLink(i, null);
                requests.updateState(i, State.Idle);
            } else {
                NBTTagCompound linkData = stackData.getCompoundTag(TLMTags.Link.tagName);
                requests.updateLink(i, AEApi.instance().storage().loadCraftingLink(linkData, this));
                if (!requests.isDone(i)) requests.updateState(i, State.Craft);
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    private void readLinkFromNBT__old(NBTTagCompound data) {
        try {
            for (int i = 0; i < REQ_COUNT; i++) {
                final NBTTagCompound link = data.getCompoundTag("links-" + i);
                if (link != null && link.hasNoTags()) {
                    requests.updateLink(i, null);
                } else {
                    requests.updateLink(i, AEApi.instance().storage().loadCraftingLink(link, this));
                    if (!requests.isDone(i)) requests.updateState(i, State.Craft);
                }
            }
        } catch (Exception ignore) {}
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBTEvent(NBTTagCompound data) {
        requests.requestStacks.writeToNbt(data, TLMTags.RequestStacks.tagName);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        if (data.hasKey(TLMTags.RequestStacks.tagName)) {
            requests.requestStacks.readFromNbt(data, TLMTags.RequestStacks.tagName);
            if (Platform.isServer()) {
                for (int i = 0; i < REQ_COUNT; i++) {
                    if (requests.requestStacks.getStack(i) != null) {
                        ItemStack storageStack = requests.requestStacks.getStack(i).getItemStack();
                        ItemStack itemStack = loadItemStackFromTag(storageStack);
                        ItemStack craftStack = removeRecursion(storageStack);
                        if (!ItemStack.areItemStacksEqual(itemStack, craftStack)) {
                            requests.updateStack(i, craftStack);
                            AELog.info(
                                    "[TileLevelMaintainer] Replace craft stack from: " + itemStack.toString()
                                            + ":"
                                            + (itemStack.hasTagCompound() ? itemStack.getTagCompound() : "{no tags}")
                                            + "; with: "
                                            + craftStack
                                            + ":"
                                            + (craftStack.hasTagCompound() ? craftStack.getTagCompound()
                                                    : "{no tags}"));
                        }
                    }
                }
            }
        } else {
            // Migration from old data storage
            long[] batches = new long[REQ_COUNT];
            long[] quantyties = new long[REQ_COUNT];
            requests.requestStacks.readFromNbt(data, "Batch");
            for (int i = 0; i < REQ_COUNT; i++) {
                IAEItemStack batchStack = requests.requestStacks.getStack(i);
                batches[i] = batchStack != null ? batchStack.getStackSize() : 0;
            }
            requests.requestStacks.readFromNbt(data, "Count");
            for (int i = 0; i < REQ_COUNT; i++) {
                IAEItemStack quantityStack = requests.requestStacks.getStack(i);
                quantyties[i] = quantityStack != null ? quantityStack.getStackSize() : 0;
            }
            requests.requestStacks.readFromNbt(data, "Inventory");
            for (int i = 0; i < REQ_COUNT; i++) {
                IAEItemStack requestsStack = requests.requestStacks.getStack(i);
                if (requestsStack != null) {
                    requests.updateStack(i, requestsStack.getItemStack());
                    requests.updateBatchSize(i, batches[i]);
                    requests.updateQuantity(i, quantyties[i]);
                }
            }
            readLinkFromNBT__old(data);
        }
    }

    private ItemStack removeRecursion(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey(TLMTags.Stack.tagName)) {
            return removeRecursion(loadItemStackFromTag(itemStack));
        }
        return itemStack;
    }

    @Nullable
    private static ItemStack loadItemStackFromTag(ItemStack itemStack) {
        return ItemStack.loadItemStackFromNBT(itemStack.getTagCompound().getCompoundTag(TLMTags.Stack.tagName));
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream(final ByteBuf data) {
        final boolean oldPower = isPowered;
        isPowered = data.readBoolean();
        return isPowered != oldPower;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream(final ByteBuf data) {
        data.writeBoolean(isActive());
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        updatePowerState();
    }

    private void updatePowerState() {
        boolean newState = false;

        try {
            newState = getProxy().isActive()
                    && getProxy().getEnergy().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001;
        } catch (final GridAccessException ignored) {

        }
        if (newState != isPowered) {
            isPowered = newState;
            markForUpdate();
        }
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    public ForgeDirection getSide() {
        return ForgeDirection.UNKNOWN;
    }

    @Override
    public int rowSize() {
        return REQ_COUNT;
    }

    @Override
    public ItemStack getSelfItemStack() {
        return getItemFromTile(this);
    }

    public enum State {
        None,
        Idle,
        Craft,
        Export,
        Error
    }

    public enum TLMTags {

        RequestStacks("RequestStacks"),
        Enable("Enable"),
        Quantity("Quantity"),
        Batch("Batch"),
        Link("Link"),
        State("State"),
        Index("Index"),
        Stack("Stack");

        public final String tagName;

        TLMTags(String tagName) {
            this.tagName = tagName;
        }
    }

    public static final class InventoryRequest {

        private final AeStackInventoryImpl<IAEItemStack> requestStacks;
        private final Future<ICraftingJob>[] jobs;
        private final ICraftingLink[] links;
        private final State[] state = new State[REQ_COUNT];

        @SuppressWarnings("unchecked")
        public InventoryRequest(TileLevelMaintainer tile) {
            requestStacks = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            jobs = new Future[REQ_COUNT];
            links = new ICraftingLink[REQ_COUNT];
        }

        public State getState(int idx) {
            IAEItemStack ias = requestStacks.getStack(idx);
            if (ias == null) {
                return State.None;
            } else {
                return state[idx] == null ? State.Idle : state[idx];
            }
        }

        public int getIdxByLink(ICraftingLink link) {
            for (int i = 0; i < REQ_COUNT; i++) {
                if (links[i] == link) {
                    return i;
                }
            }
            return 0;
        }

        public boolean isEnable(int idx) {
            IAEItemStack ias = requestStacks.getStack(idx);
            if (ias == null) {
                return true;
            }

            ItemStack is = ias.getItemStack();
            return is.hasTagCompound() ? is.getTagCompound().getBoolean(TLMTags.Enable.tagName) : true;
        }

        public boolean isDone(int index) {
            if (links[index] == null) {
                return true;
            }
            return links[index].isDone() || links[index].isCanceled();
        }

        public Future<ICraftingJob> getJob(int idx) {
            return jobs[idx];
        }

        public ICraftingLink getLink(int idx) {
            return links[idx];
        }

        public void updateJob(int idx, Future<ICraftingJob> job) {
            jobs[idx] = job;
        }

        private void updateField(int idx, UnaryOperator<NBTTagCompound> updater) {
            IAEItemStack ias = requestStacks.getStack(idx);
            if (ias == null) return;

            ItemStack itemStack = ias.getItemStack();
            NBTTagCompound data = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
            data.setInteger(TLMTags.Index.tagName, idx);
            itemStack.setTagCompound(updater.apply(data));
            IAEItemStack newRequestStack = AEItemStack.create(itemStack);
            newRequestStack.setStackSize(ias.getStackSize());
            requestStacks.setStack(idx, newRequestStack);

        }

        public void updateLink(int idx, ICraftingLink link) {
            links[idx] = link;

            updateField(idx, data -> {
                NBTTagCompound linkData = new NBTTagCompound();
                if (link != null) link.writeToNBT(linkData);
                data.setTag(TLMTags.Link.tagName, linkData);

                return data;
            });
        }

        public void updateState(int idx, State nextState) {
            state[idx] = nextState;

            updateField(idx, data -> {
                data.setInteger(TLMTags.State.tagName, nextState.ordinal());

                return data;
            });
        }

        public void updateStatus(int idx, boolean enable) {
            updateField(idx, data -> {
                State nextState = enable ? State.Idle : State.None;

                data.setBoolean(TLMTags.Enable.tagName, enable);
                data.setInteger(TLMTags.State.tagName, nextState.ordinal());
                return data;
            });
        }

        public void updateQuantity(int idx, long quantity) {
            updateField(idx, data -> {
                data.setLong(TLMTags.Quantity.tagName, quantity);

                return data;
            });
        }

        public void updateBatchSize(int idx, long batch) {
            updateField(idx, data -> {
                data.setLong(TLMTags.Batch.tagName, batch);
                data.setBoolean(TLMTags.Enable.tagName, true);

                return data;
            });
        }

        public void updateStack(int idx, ItemStack itemStack) {
            updateField(idx, data -> {
                NBTTagCompound stackData = new NBTTagCompound();
                if (itemStack != null) itemStack.writeToNBT(stackData);
                data.setTag(TLMTags.Stack.tagName, stackData);

                return data;
            });
        }

        public IAEItemStack getAEItemStack(int idx) {
            return requestStacks.getStack(idx);
        }

        public ItemStack getItemStack(int idx) {
            return requestStacks.getStack(idx).getItemStack();
        }

        public long getQuantity(int idx) {
            if (!isEnable(idx)) return 0;
            IAEItemStack ias = requestStacks.getStack(idx);
            if (ias == null) return 0;
            ItemStack itemStack = ias.getItemStack();
            if (!itemStack.hasTagCompound()) return 0;

            return itemStack.getTagCompound().getLong(TLMTags.Quantity.tagName);
        }

        public long getBatchSize(int idx) {
            if (!isEnable(idx)) return 0;
            IAEItemStack ias = requestStacks.getStack(idx);
            if (ias == null) return 0;
            ItemStack itemStack = ias.getItemStack();
            if (!itemStack.hasTagCompound()) return 0;

            return itemStack.getTagCompound().getLong(TLMTags.Batch.tagName);
        }

        public IAEItemStack getCraftItem(int idx) {
            IAEItemStack is = requestStacks.getStack(idx);
            if (is == null) return null;
            if (is.getItemStack() == null) return null;
            ItemStack qis = loadItemStackFromTag(is.getItemStack());
            if (qis == null) return null;
            IAEItemStack qais = AEItemStack.create(qis);
            qais.setStackSize(getBatchSize(idx));

            return qais;
        }
    }
}
