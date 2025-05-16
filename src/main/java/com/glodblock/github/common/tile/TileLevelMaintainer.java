package com.glodblock.github.common.tile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.api.registries.LevelItemInfo;
import com.glodblock.github.api.registries.LevelState;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.ThaumicEnergisticsCrafting;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.util.ModAndClassUtil;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
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
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileLevelMaintainer extends AENetworkTile
        implements IAEAppEngInventory, IGridTickable, ICraftingRequester, IPowerChannelState, ILevelViewable {

    public static final int REQ_COUNT = 5;

    public final RequestInfo[] requests = new RequestInfo[REQ_COUNT];
    private final LevelMaintainerInventory inventory = new LevelMaintainerInventory(requests, this);
    private int firstRequest = 0;
    private final BaseActionSource source;
    private final IInventory inv = new AeItemStackHandler(inventory);
    private boolean isPowered = false;

    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        source = new MachineSource(this);
    }

    public AeStackInventory<IAEItemStack> getRequestSlots() {
        return inventory;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(
                Arrays.stream(this.requests).filter(Objects::nonNull).map(info -> info.link).filter(Objects::nonNull)
                        .iterator());
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        int idx = this.getRequestIndexByLink(link);
        if (idx == -1) {
            AELog.warn("Invalid crafting link: " + link);
            return items;
        } ;
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
                            this.requests[idx].updateState(LevelState.Export);

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
            RequestInfo info = requests[x];
            if (info != null && info.link == link) {
                info.link = null;
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

            // Check there are available crafting CPUs before doing any work.
            // This hopefully stops level maintainers busy-looping calculating
            // crafting tasks that cannot be successfully submitted.
            boolean allBusy = true;
            for (final ICraftingCPU cpu : craftingGrid.getCpus()) {
                if (!cpu.isBusy()) {
                    allBusy = false;
                    break;
                }
            }

            // Find a request that we can submit to the network.
            // If there is none, find the next request we can begin calculating.
            ICraftingJob jobToSubmit = null;
            int jobToSubmitIdx = -1;
            IAEItemStack itemToBegin = null;
            int itemToBeginIdx = -1;

            for (int j = 0; j < REQ_COUNT; ++j) {
                int i = (firstRequest + j) % REQ_COUNT;
                if (requests[i] == null) continue;

                final long quantity = requests[i].quantity;
                final long batchSize = requests[i].batchSize;
                final boolean isEnable = requests[i].enable;

                if (!isEnable || quantity == 0 || batchSize == 0) {
                    requests[i].updateState(LevelState.None);
                    continue;
                }

                IAEItemStack craftItem = requests[i].itemStack;

                if (ModAndClassUtil.ThE) {
                    if (ThaumicEnergisticsCrafting.isAspectStack(craftItem.getItemStack())) {
                        craftItem = ThaumicEnergisticsCrafting.convertAspectStack(craftItem);
                    }
                }

                IAEItemStack aeItem = inv.findPrecise(craftItem);

                long stackSize = aeItem == null ? 0 : aeItem.getStackSize();

                if (ModAndClassUtil.ThE) {
                    if (aeItem != null && ThaumicEnergisticsCrafting.isAspectStack(aeItem.getItemStack())) {
                        stackSize = ThaumicEnergisticsCrafting.getEssentiaAmount(aeItem, grid);
                    }
                }

                boolean isDone = this.isDone(i);
                boolean isCraftable = aeItem != null && aeItem.isCraftable();
                boolean shouldCraft = isCraftable && stackSize < quantity;

                if (isDone) this.requests[i].updateState(LevelState.Idle);
                if (!isCraftable) this.requests[i].updateState(LevelState.Error);

                if (allBusy || !isDone || !shouldCraft) {
                    continue;
                }

                if (craftingGrid.canEmitFor(craftItem)) {
                    continue;
                }

                if (craftingGrid.isRequesting(craftItem)) {
                    continue;
                }

                // do crafting
                Future<ICraftingJob> jobTask = requests[i].job;

                if (jobTask == null) {
                    if (itemToBegin == null) {
                        itemToBegin = craftItem;
                        itemToBeginIdx = i;
                    }
                } else if (jobTask.isDone()) {
                    requests[i].updateState(LevelState.Craft);
                    try {
                        ICraftingJob job = jobTask.get();
                        if (job != null) {
                            if (jobToSubmit == null) {
                                jobToSubmit = job;
                                jobToSubmitIdx = i;
                            }
                        } else {
                            requests[i].updateState(LevelState.Error);
                        }
                    } catch (Exception ignored) {
                        requests[i].updateState(LevelState.Error);
                    }
                }

            }

            if (jobToSubmit != null) {
                // Finished calculating a request, try to submit it.
                ICraftingLink link = craftingGrid.submitJob(jobToSubmit, this, null, false, source);
                requests[jobToSubmitIdx] = null;
                if (link != null) {
                    requests[jobToSubmitIdx].updateState(LevelState.Craft);
                    requests[jobToSubmitIdx].link = link;
                } else {
                    requests[jobToSubmitIdx].updateState(LevelState.Error);
                }
            } else if (itemToBegin != null) {
                // No jobs to submit, start calculating some item.

                requests[itemToBeginIdx].job = craftingGrid
                        .beginCraftingJob(getWorldObj(), grid, source, itemToBegin, null);
                requests[itemToBeginIdx].updateState(LevelState.Craft);

                // Try the next item next time.
                firstRequest = (firstRequest + 1) % REQ_COUNT;
            } else {
                // No work to be done:
                // Every item is at desired quantity, being crafted, or has no patterns.
                return TickRateModulation.IDLE;
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

    public IInventory getInventory() {
        return inv;
    }

    public IInventory getInventoryByName(String name) {
        if (Objects.equals(name, "config")) return new AeItemStackHandler(this.inventory);

        return null;
    }

    @Override
    public LevelItemInfo[] getLevelItemInfoList() {
        return Arrays.stream(this.requests).map(request -> {
            if (request == null) return null;
            return new LevelItemInfo(
                    request.itemStack.getItemStack(),
                    request.getQuantity(),
                    request.getBatchSize(),
                    request.getState());
        }).toArray(LevelItemInfo[]::new);
    }

    public void updateQuantity(int idx, long size) {
        if (requests[idx] == null) return;
        requests[idx].quantity = size > 0 ? size : 0;
        this.checkState(idx);
        this.saveChanges();
    }

    public void updateBatchSize(int idx, long size) {
        if (requests[idx] == null) return;
        requests[idx].batchSize = size > 0 ? size : 0;
        this.checkState(idx);
        this.saveChanges();
    }

    // TODO: rename
    public void updateStatus(int idx, boolean enable) {
        if (requests[idx] == null) return;
        requests[idx].enable = enable;

        this.checkState(idx);
        AELog.info(
                "[TileLevelMaintainer] " + requests[idx].quantity
                        + " "
                        + requests[idx].batchSize
                        + " "
                        + requests[idx].enable);
        this.saveChanges();
    }

    public void updateStack(int idx, ItemStack stack) {
        if (stack == null) {
            requests[idx] = null;
        } else {
            stack = this.removeRecursion(stack);
            requests[idx] = new RequestInfo(stack, this);
        }
        this.saveChanges();
    }

    private void checkState(int idx) {
        if (!requests[idx].enable || requests[idx].quantity == 0 || requests[idx].batchSize == 0) {
            requests[idx].updateState(LevelState.None);
        } else {
            requests[idx].updateState(LevelState.Idle);
        }
    }

    public boolean isDone(int i) {
        if (requests[i] == null) {
            return true;
        }
        ICraftingLink link = requests[i].link;
        return link == null || link.isDone() || link.isCanceled();
    }

    private int getRequestIndexByLink(ICraftingLink link) {
        for (int i = 0; i < REQ_COUNT; i++) {
            if (requests[i] != null && requests[i].link == link) {
                return i;
            }
        }
        return -1;
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBTEvent(NBTTagCompound data) {
        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            if (this.requests[i] != null) {
                tagList.appendTag(this.requests[i].writeToNBT());
            } else {
                tagList.appendTag(new NBTTagCompound());
            }
        }
        data.setTag("Requests", tagList);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        if (data.hasKey("Requests")) {
            NBTTagList tagList = data.getTagList("Requests", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                if (tag == null || !tag.hasKey("stack")) {
                    this.requests[i] = null;
                } else if (this.requests[i] == null) {
                    this.requests[i] = new RequestInfo(tag, this);
                } else {
                    this.requests[i].loadFromNBT(tag);
                }
            }
        } else if (data.hasKey(TLMTags.RequestStacks.tagName)) {
            // Migration from old NBT
            NBTTagList stacksTag = data.getCompoundTag(TLMTags.RequestStacks.tagName)
                    .getTagList("Contents", Constants.NBT.TAG_COMPOUND);
            IAEItemStack[] stacks = new IAEItemStack[REQ_COUNT];
            for (int i = 0; i < REQ_COUNT; i++) {
                NBTTagCompound stackTag = stacksTag.getCompoundTagAt(i);
                if (stackTag == null) continue;
                stacks[i] = AEItemStack.loadItemStackFromNBT(stackTag);
                if (stacks[i] == null) continue;
                ItemStack itemstack = stacks[i].getItemStack();
                if (!itemstack.hasTagCompound()) continue;
                NBTTagCompound itemTag = itemstack.getTagCompound();

                ItemStack craftStack = ItemStack.loadItemStackFromNBT(itemTag.getCompoundTag(TLMTags.Stack.tagName));
                craftStack = removeRecursion(craftStack);
                if (craftStack == null) continue;
                requests[i] = new RequestInfo(craftStack, this);
                if (itemTag.hasKey(TLMTags.Enable.tagName)) {
                    requests[i].enable = itemTag.getBoolean(TLMTags.Enable.tagName);
                }
                if (itemTag.hasKey(TLMTags.Quantity.tagName)) {
                    requests[i].quantity = itemTag.getLong(TLMTags.Quantity.tagName);
                }
                if (itemTag.hasKey(TLMTags.Batch.tagName)) {
                    requests[i].batchSize = itemTag.getLong(TLMTags.Batch.tagName);
                }
            }
        } else {
            // Migration from old old data storage

            long[] batches = new long[REQ_COUNT];
            long[] quantyties = new long[REQ_COUNT];
            ItemStack[] stacks = new ItemStack[REQ_COUNT];

            NBTTagList batchTag = data.getCompoundTag("Batch").getTagList("Contents", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < REQ_COUNT; i++) {
                NBTTagCompound stackTag = batchTag.getCompoundTagAt(i);
                IAEItemStack stack = AEItemStack.loadItemStackFromNBT(stackTag);
                batches[i] = stack != null ? stack.getStackSize() : 0;
            }

            NBTTagList quantityTag = data.getCompoundTag("Count").getTagList("Contents", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < REQ_COUNT; i++) {
                NBTTagCompound stackTag = quantityTag.getCompoundTagAt(i);
                IAEItemStack stack = AEItemStack.loadItemStackFromNBT(stackTag);
                quantyties[i] = stack != null ? stack.getStackSize() : 0;
            }

            NBTTagList inventoryTag = data.getCompoundTag("Count").getTagList("Contents", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < REQ_COUNT; i++) {
                NBTTagCompound stackTag = inventoryTag.getCompoundTagAt(i);
                IAEItemStack stack = AEItemStack.loadItemStackFromNBT(stackTag);
                stacks[i] = stack != null ? stack.getItemStack() : null;
            }

            for (int i = 0; i < REQ_COUNT; i++) {
                if (stacks[i] == null) continue;
                this.requests[i] = new RequestInfo(stacks[i], this);
                this.requests[i].batchSize = batches[i];
                this.requests[i].quantity = quantyties[i];
            }
        }
    }

    // Remove old format NBT data from ItemStack
    private ItemStack removeRecursion(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return itemStack;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag.hasKey(TLMTags.Stack.tagName) && tag.hasKey(TLMTags.Quantity.tagName)) {
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

    private enum TLMTags {

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

    public static class RequestInfo {

        private final TileLevelMaintainer tile;
        private @NotNull IAEItemStack itemStack;
        private long quantity;
        private long batchSize;
        private boolean enable;
        private LevelState state;
        @Nullable
        private Future<ICraftingJob> job;
        @Nullable
        private ICraftingLink link;

        public RequestInfo(@NotNull ItemStack stack, TileLevelMaintainer tile) {
            this.tile = tile;
            itemStack = AEItemStack.create(stack);
            quantity = 0;
            batchSize = 0;
            enable = false;
            state = LevelState.None;
            link = null;
            job = null;
        }

        public RequestInfo(NBTTagCompound tag, TileLevelMaintainer tile) {
            this.tile = tile;
            itemStack = AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
            quantity = tag.getLong("quantity");
            batchSize = tag.getLong("batch");
            enable = tag.getBoolean("enable");
            state = LevelState.values()[tag.getInteger("state")];
            if (tag.hasKey("link")) {
                this.link = AEApi.instance().storage().loadCraftingLink(tag.getCompoundTag("link"), this.tile);
            }
            job = null;
        }

        public void loadFromNBT(NBTTagCompound tag) {
            itemStack = AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
            quantity = tag.getLong("quantity");
            batchSize = tag.getLong("batch");
            enable = tag.getBoolean("enable");
            state = LevelState.values()[tag.getInteger("state")];
            if (tag.hasKey("link")) {
                this.link = AEApi.instance().storage().loadCraftingLink(tag.getCompoundTag("link"), this.tile);
            }
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound stackTag = new NBTTagCompound();
            itemStack.writeToNBT(stackTag);
            tag.setTag("stack", stackTag);
            tag.setLong("quantity", quantity);
            tag.setLong("batch", batchSize);
            tag.setBoolean("enable", enable);
            tag.setInteger("state", state.ordinal());
            if (this.link != null) {
                NBTTagCompound linkTag = new NBTTagCompound();
                this.link.writeToNBT(linkTag);
                tag.setTag("link", linkTag);
            }
            return tag;
        }

        public void updateState(LevelState state) {
            this.state = state;
            this.tile.saveChanges();
        }

        public void updateEnable(boolean enable) {
            this.enable = enable;
            this.tile.saveChanges();
        }

        public IAEItemStack getAEItemStack() {
            return this.itemStack;
        }

        public long getQuantity() {
            return quantity;
        }

        public long getBatchSize() {
            return batchSize;
        }

        public boolean isEnable() {
            return enable;
        }

        public LevelState getState() {
            return state;
        }
    }

    private static class LevelMaintainerInventory implements AeStackInventory<IAEItemStack> {

        private final RequestInfo[] requests;
        private final TileLevelMaintainer tile;

        private LevelMaintainerInventory(RequestInfo[] requests, TileLevelMaintainer tile) {
            this.requests = requests;
            this.tile = tile;
        }

        @Override
        public @NotNull Iterator<IAEItemStack> iterator() {
            return Arrays.stream(requests).map(info -> info != null ? info.itemStack : null).iterator();
        }

        @Override
        public int getSlotCount() {
            return requests.length;
        }

        @Override
        public @Nullable IAEItemStack getStack(int slot) {
            return requests[slot] != null ? requests[slot].itemStack : null;
        }

        @Override
        public void setStack(int slot, @Nullable IAEItemStack stack) {
            if (stack == null) {
                this.tile.updateStack(slot, null);
            } else {
                this.tile.updateStack(slot, stack.getItemStack());
            }
        }

        @Override
        public Stream<IAEItemStack> stream() {
            return Arrays.stream(requests).map(info -> info != null ? info.itemStack : null);
        }
    }
}
