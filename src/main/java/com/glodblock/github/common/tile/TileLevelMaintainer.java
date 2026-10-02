package com.glodblock.github.common.tile;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Future;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.ThaumicEnergisticsCrafting;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.CraftingMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.ILevelViewable;
import appeng.api.features.LevelItemInfo;
import appeng.api.features.LevelState;
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
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.IAEStackInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.IterationCounter;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileLevelMaintainer extends AENetworkTile
        implements IAEAppEngInventory, IGridTickable, ICraftingRequester, IPowerChannelState, ILevelViewable {

    public static final int REQ_COUNT = 5;
    public static final String NBT_REQUESTS = "Requests";
    public static final String NBT_STACK = "stack";
    public static final String NBT_QUANTITY = "quantity";
    public static final String NBT_BATCH = "batch";
    public static final String NBT_ENABLE = "enable";
    public static final String NBT_STATE = "state";
    public static final String NBT_LINK = "link";
    public static final String NBT_LITE_MODE = "lite_mode";
    public static final String NBT_MIN_TICK = "minTick";
    public static final String NBT_MAX_TICK = "maxTick";

    public final RequestInfo[] requests = new RequestInfo[REQ_COUNT];
    private final LevelMaintainerInventory inventory = new LevelMaintainerInventory(requests);
    private int firstRequest = 0;
    private final BaseActionSource source;
    private boolean isPowered = false;
    private boolean isLiteModeOverridden = false;
    private boolean isLiteMode = false;

    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        source = new MachineSource(this);
    }

    public IAEStackInventory getAEStackInventory() {
        return inventory;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(
                Arrays.stream(this.requests).filter(Objects::nonNull).map(info -> info.link).filter(Objects::nonNull)
                        .iterator());
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public IAEStack<?> injectCraftedItems(ICraftingLink link, IAEStack<?> stack, Actionable mode) {
        int idx = this.getRequestIndexByLink(link);

        try {
            if (getProxy().isActive()) {
                final IEnergyGrid energy = getProxy().getEnergy();
                final double power = Math.ceil((double) stack.getStackSize() / stack.getAmountPerUnit());

                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    IMEMonitor monitor = getProxy().getStorage().getMEMonitor(stack.getStackType());
                    if (monitor == null) return stack;

                    IAEStack<?> notInjected;
                    if (stack instanceof IAEItemStack ais && ItemFluidDrop.isFluidStack(ais)) {
                        notInjected = getProxy().getStorage().getFluidInventory()
                                .injectItems(ItemFluidDrop.getAeFluidStack(ais), mode, source);
                    } else {
                        notInjected = monitor.injectItems(stack, mode, source);
                    }

                    if (notInjected != null) {
                        if (idx != -1) {
                            this.updateState(idx, LevelState.Export);
                        }

                        return notInjected;
                    } else {
                        return null;
                    }
                }
            }
        } catch (GridAccessException e) {
            AELog.debug(e);
        }

        return stack;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        for (int i = 0; i < REQ_COUNT; i++) {
            if (requests[i] != null && link != null) {
                ICraftingLink prevLink = requests[i].link;
                if (prevLink != null && prevLink.getCraftingID().equals(link.getCraftingID())) {
                    this.updateLink(i, null);
                }
            }
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.worldObj == null) {
            return TickRateModulation.IDLE;
        }
        final long now = this.worldObj.getTotalWorldTime();
        if (!this.hasDueRequest(now)) {
            return TickRateModulation.IDLE;
        }
        if (!this.canDoBusWork()) {
            this.postponeAll(now);
            return TickRateModulation.IDLE;
        }
        return this.doWork(now);
    }

    private boolean hasDueRequest(final long now) {
        for (final RequestInfo request : this.requests) {
            if (request != null && now >= request.nextAttemptAt) {
                return true;
            }
        }
        return false;
    }

    private void postponeAll(final long now) {
        for (final RequestInfo request : this.requests) {
            if (request != null && now >= request.nextAttemptAt) {
                request.nextAttemptAt = now + Math.max(1, request.maxTick);
            }
        }
    }

    private void defer(final int idx, final long now, final long delay) {
        if (this.requests[idx] == null) return;
        this.requests[idx].nextAttemptAt = now + Math.max(1, delay);
    }

    private TickRateModulation doWork(final long now) {
        if (!getProxy().isActive() || !canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        try {
            final ICraftingGrid craftingGrid = getProxy().getCrafting();
            final CraftingGridCache craftingGridCache = (CraftingGridCache) craftingGrid;
            final IGrid grid = getProxy().getGrid();

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
            IAEStack<?> itemToBegin = null;
            int itemToBeginIdx = -1;

            for (int j = 0; j < REQ_COUNT; ++j) {
                int i = (firstRequest + j) % REQ_COUNT;
                if (requests[i] == null || now < requests[i].nextAttemptAt) continue;

                final long quantity = requests[i].quantity;
                final long batchSize = requests[i].batchSize;
                final boolean isEnable = requests[i].enable;

                if (!isEnable || quantity == 0 || batchSize == 0) {
                    this.updateState(i, LevelState.None);
                    this.defer(i, now, requests[i].maxTick);
                    continue;
                }

                IAEStack<?> craftItem = requests[i].stack.copy();
                craftItem.setStackSize(batchSize);
                IMEMonitor monitor = getProxy().getStorage().getMEMonitor(craftItem.getStackType());
                if (monitor == null) {
                    this.defer(i, now, requests[i].maxTick);
                    continue;
                }

                IAEStack<?> stackInStorage = monitor.getAvailableItem(craftItem, IterationCounter.fetchNewId());

                long stackSize = stackInStorage == null ? 0 : stackInStorage.getStackSize();

                boolean isDone = this.isDone(i);
                boolean isCraftable = stackInStorage != null && stackInStorage.isCraftable();
                boolean shouldCraft = isCraftable && stackSize < quantity;

                if (isDone) {
                    if (this.requests[i].state != LevelState.Idle) {
                        this.updateState(i, LevelState.Idle);
                    }
                    if (this.requests[i].link != null) {
                        this.updateLink(i, null);
                    }
                    if (!isCraftable) {
                        updateState(i, LevelState.NotFound);
                    }
                }

                if (allBusy || !isDone || !shouldCraft) {
                    this.defer(i, now, requests[i].maxTick);
                    continue;
                }

                if (craftingGrid.canEmitFor(stackInStorage)) {
                    this.defer(i, now, requests[i].maxTick);
                    continue;
                }

                if (craftingGrid.isRequesting(stackInStorage)) {
                    this.defer(i, now, requests[i].maxTick);
                    continue;
                }

                // do crafting
                Future<ICraftingJob> jobTask = requests[i].job;

                if (jobTask == null) {
                    if (itemToBegin == null) {
                        itemToBegin = craftItem;
                        itemToBeginIdx = i;
                    } else {
                        this.defer(i, now, 1);
                    }
                } else if (jobTask.isDone()) {
                    this.updateState(i, LevelState.Craft);
                    try {
                        ICraftingJob job = jobTask.get();
                        if (job != null) {
                            if (jobToSubmit == null) {
                                jobToSubmit = job;
                                jobToSubmitIdx = i;
                            } else {
                                this.defer(i, now, 1);
                            }
                        } else {
                            this.updateState(i, LevelState.Error);
                            this.defer(i, now, requests[i].maxTick);
                        }
                    } catch (Exception ignored) {
                        this.updateState(i, LevelState.Error);
                        this.defer(i, now, requests[i].maxTick);
                    }
                } else {
                    this.defer(i, now, requests[i].minTick);
                }

            }

            if (jobToSubmit != null) {
                // Finished calculating a request, try to submit it.
                ICraftingLink link = craftingGrid.submitJob(jobToSubmit, this, null, false, source);
                requests[jobToSubmitIdx].job = null;
                if (link != null) {
                    this.updateState(jobToSubmitIdx, LevelState.Craft);
                    this.updateLink(jobToSubmitIdx, link);
                } else {
                    this.updateState(jobToSubmitIdx, LevelState.CantCraft);
                }
                this.defer(jobToSubmitIdx, now, requests[jobToSubmitIdx].maxTick);
            } else if (itemToBegin != null) {
                // No jobs to submit, start calculating some item.
                requests[itemToBeginIdx].job = craftingGridCache.beginCraftingJob(
                        this.worldObj,
                        grid,
                        source,
                        itemToBegin,
                        CraftingMode.STANDARD,
                        isLiteMode(),
                        null);
                this.updateState(itemToBeginIdx, LevelState.Craft);
                this.defer(itemToBeginIdx, now, requests[itemToBeginIdx].minTick);

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
        final long now = this.worldObj == null ? 0L : this.worldObj.getTotalWorldTime();
        for (final RequestInfo request : this.requests) {
            if (request != null) {
                request.nextAttemptAt = now;
            }
        }
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
    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public boolean isActive() {
        return isPowered;
    }

    @Override
    public LevelItemInfo[] getLevelItemInfoList() {
        return Arrays.stream(this.requests).map(request -> {
            if (request == null) return null;
            return new LevelItemInfo(
                    request.getAEStack(),
                    request.getQuantity(),
                    request.getBatchSize(),
                    request.getState());
        }).toArray(LevelItemInfo[]::new);
    }

    public void updateQuantity(int idx, long size) {
        if (requests[idx] == null) return;
        requests[idx].quantity = size > 0 ? size : 0;
        this.markDue(idx);
        this.checkState(idx);
        this.saveChanges();
    }

    public void updateBatchSize(int idx, long size) {
        if (requests[idx] == null) return;
        requests[idx].batchSize = size > 0 ? size : 0;
        this.markDue(idx);
        this.checkState(idx);
        this.saveChanges();
    }

    public void updateStatus(int idx, boolean enable) {
        if (requests[idx] == null) return;
        requests[idx].enable = enable;
        this.markDue(idx);
        this.checkState(idx);
        this.saveChanges();
    }

    public void updateMaxTick(int idx, long ticks) {
        if (requests[idx] == null) return;
        final int value = clampTick(ticks, Config.levelMaintainerMaxTicks);
        if (requests[idx].maxTick == value) return;
        requests[idx].maxTick = value;
        if (requests[idx].minTick > value) {
            requests[idx].minTick = value;
        }
        this.markDue(idx);
        this.saveChanges();
    }

    private static int clampTick(long ticks, int fallback) {
        final long value = ticks > 0 ? ticks : fallback;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, value));
    }

    private void markDue(int idx) {
        if (requests[idx] == null || this.worldObj == null) return;
        requests[idx].nextAttemptAt = this.worldObj.getTotalWorldTime();
    }

    private void updateState(int idx, @NotNull LevelState state) {
        if (requests[idx] == null) return;
        if (requests[idx].state == state) return;
        requests[idx].state = state;
        this.saveChanges();
    }

    public void updateStack(int idx, @Nullable IAEStack<?> stack) {
        if (stack == null) {
            requests[idx] = null;
        } else {
            requests[idx] = new RequestInfo(stack, this);
        }
        this.markDue(idx);
        this.saveChanges();
    }

    private boolean getLiteModeDefault() {
        try {
            final ICraftingGrid craftingGrid = getProxy().getCrafting();
            if (craftingGrid instanceof CraftingGridCache cache) {
                return cache.getLiteCraftingDefault();
            }
        } catch (GridAccessException ignored) {}
        return false;
    }

    public boolean isLiteMode() {
        return this.isLiteModeOverridden ? this.isLiteMode : getLiteModeDefault();
    }

    public void setLiteMode(boolean liteMode) {
        this.isLiteModeOverridden = true;
        this.isLiteMode = liteMode;
        this.saveChanges();
    }

    public void toggleLiteMode() {
        this.setLiteMode(!this.isLiteMode());
    }

    public void clearLiteMode() {
        this.isLiteModeOverridden = false;
        this.saveChanges();
    }

    private void updateLink(int idx, @Nullable ICraftingLink link) {
        if (requests[idx] == null) return;
        requests[idx].link = link;
        this.saveChanges();
    }

    private void checkState(int idx) {
        if (!requests[idx].enable || requests[idx].quantity == 0 || requests[idx].batchSize == 0) {
            this.updateState(idx, LevelState.None);
        } else if (!this.isDone(idx)) {
            this.updateState(idx, LevelState.Craft);
        } else {
            this.updateState(idx, LevelState.Idle);
        }
    }

    public boolean isDone(int i) {
        if (requests[i] == null) {
            return true;
        }
        ICraftingLink link = requests[i].link;
        return link == null || link.isDone() || link.isCanceled();
    }

    private int getRequestIndexByLink(ICraftingLink targetLink) {
        for (int i = 0; i < REQ_COUNT; i++) {
            if (requests[i] != null && targetLink != null) {
                ICraftingLink link = requests[i].link;
                if (link != null && link.getCraftingID().equals(targetLink.getCraftingID())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBTEvent(NBTTagCompound data) {
        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            if (this.requests[i] != null) {
                tagList.appendTag(this.requests[i].writeToNBT(true));
            } else {
                tagList.appendTag(new NBTTagCompound());
            }
        }
        data.setTag(NBT_REQUESTS, tagList);
        if (this.isLiteModeOverridden) {
            data.setBoolean(NBT_LITE_MODE, this.isLiteMode);
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        if (data.hasKey(NBT_REQUESTS)) {
            NBTTagList tagList = data.getTagList(NBT_REQUESTS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                if (tag == null || !tag.hasKey(NBT_STACK)) {
                    this.requests[i] = null;
                } else if (this.requests[i] == null) {
                    try {
                        this.requests[i] = new RequestInfo(tag, this);
                    } catch (Exception ignored) {
                        this.requests[i] = null;
                    }
                } else {
                    this.requests[i].loadFromNBT(tag);
                }
            }
        } else if (data.hasKey("RequestStacks")) {
            // Migration from old NBT
            NBTTagList stacksTag = data.getCompoundTag("RequestStacks")
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

                ItemStack craftStack = ItemStack.loadItemStackFromNBT(itemTag.getCompoundTag("Stack"));
                craftStack = removeRecursion(craftStack);
                if (craftStack == null) continue;
                requests[i] = new RequestInfo(Platform.convertStack(AEItemStack.create(craftStack)), this);
                if (itemTag.hasKey("Enable")) {
                    requests[i].enable = itemTag.getBoolean("Enable");
                }
                if (itemTag.hasKey("Quantity")) {
                    requests[i].quantity = itemTag.getLong("Quantity");
                }
                if (itemTag.hasKey("Batch")) {
                    requests[i].batchSize = itemTag.getLong("Batch");
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
                this.requests[i] = new RequestInfo(Platform.convertStack(AEItemStack.create(stacks[i])), this);
                this.requests[i].batchSize = batches[i];
                this.requests[i].quantity = quantyties[i];
            }
        }
        if (data.hasKey(NBT_LITE_MODE)) {
            this.isLiteModeOverridden = true;
            this.isLiteMode = data.getBoolean(NBT_LITE_MODE);
        }
    }

    // Remove old format NBT data from ItemStack
    private ItemStack removeRecursion(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return itemStack;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag.hasKey("Stack") && tag.hasKey("Quantity")) {
            return removeRecursion(ItemStack.loadItemStackFromNBT(itemStack.getTagCompound().getCompoundTag("Stack")));
        }
        return itemStack;
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

    @Override
    public void uploadSettings(SettingsFrom from, NBTTagCompound compound) {
        super.uploadSettings(from, compound);
        NBTTagList tagList = compound.getTagList(NBT_REQUESTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            if (tag == null || !tag.hasKey(NBT_STACK)) {
                this.requests[i] = null;
            } else {
                try {
                    this.requests[i] = new RequestInfo(tag, this);
                    this.requests[i].state = LevelState.None;
                } catch (Exception ignored) {
                    this.requests[i] = null;
                }
            }
        }
        if (compound.hasKey(NBT_LITE_MODE)) {
            this.isLiteModeOverridden = true;
            this.isLiteMode = compound.getBoolean(NBT_LITE_MODE);
        } else {
            this.isLiteModeOverridden = false;
        }
        this.saveChanges();
    }

    @Override
    public NBTTagCompound downloadSettings(SettingsFrom from) {
        NBTTagCompound compound = super.downloadSettings(from);
        compound = compound == null ? new NBTTagCompound() : compound;
        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            if (this.requests[i] != null) {
                tagList.appendTag(this.requests[i].writeToNBT(false));
            } else {
                tagList.appendTag(new NBTTagCompound());
            }
        }
        compound.setTag(NBT_REQUESTS, tagList);
        if (isLiteModeOverridden) {
            compound.setBoolean(NBT_LITE_MODE, isLiteMode);
        }
        return compound;
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
    public @NotNull TileEntity getTile() {
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

    public static class RequestInfo {

        private final TileLevelMaintainer tile;
        private @NotNull IAEStack<?> stack;
        private long quantity;
        private long batchSize;
        private boolean enable;
        private int minTick;
        private int maxTick;
        private long nextAttemptAt;
        private LevelState state;
        @Nullable
        private Future<ICraftingJob> job;
        @Nullable
        private ICraftingLink link;

        public RequestInfo(@NotNull IAEStack<?> stack, TileLevelMaintainer tile) {
            this.tile = tile;
            this.stack = stack.copy();
            quantity = 0;
            batchSize = 0;
            enable = false;
            minTick = Config.levelMaintainerMinTicks;
            maxTick = Config.levelMaintainerMaxTicks;
            nextAttemptAt = 0;
            state = LevelState.None;
            link = null;
            job = null;
        }

        public RequestInfo(NBTTagCompound tag, TileLevelMaintainer tile) throws IllegalArgumentException {
            this.tile = tile;
            stack = Platform.readStackNBT(tag.getCompoundTag(NBT_STACK), true);
            if (stack == null) {
                throw new IllegalArgumentException("ItemStack cannot be null!");
            }

            // Migrate old aspect stack
            stack = ThaumicEnergisticsCrafting.convertItemAspectStack(stack);

            quantity = tag.getLong(NBT_QUANTITY);
            batchSize = tag.getLong(NBT_BATCH);
            enable = tag.getBoolean(NBT_ENABLE);
            minTick = readTick(tag, NBT_MIN_TICK, Config.levelMaintainerMinTicks);
            maxTick = readTick(tag, NBT_MAX_TICK, Config.levelMaintainerMaxTicks);
            if (maxTick < minTick) {
                maxTick = minTick;
            }
            nextAttemptAt = 0;
            state = LevelState.values()[tag.getInteger(NBT_STATE)];
            if (tag.hasKey(NBT_LINK)) {
                try {
                    this.link = AEApi.instance().storage().loadCraftingLink(tag.getCompoundTag(NBT_LINK), this.tile);
                } catch (Exception ignored) {
                    this.link = null;
                }
            }
            job = null;
        }

        private static int readTick(NBTTagCompound tag, String key, int fallback) {
            if (!tag.hasKey(key)) return fallback;
            return clampTick(tag.getInteger(key), fallback);
        }

        public void loadFromNBT(NBTTagCompound tag) {
            stack = Platform.readStackNBT(tag.getCompoundTag(NBT_STACK), true);

            // Migrate old aspect stack
            stack = ThaumicEnergisticsCrafting.convertItemAspectStack(stack);

            quantity = tag.getLong(NBT_QUANTITY);
            batchSize = tag.getLong(NBT_BATCH);
            enable = tag.getBoolean(NBT_ENABLE);
            minTick = readTick(tag, NBT_MIN_TICK, Config.levelMaintainerMinTicks);
            maxTick = readTick(tag, NBT_MAX_TICK, Config.levelMaintainerMaxTicks);
            if (maxTick < minTick) {
                maxTick = minTick;
            }
            nextAttemptAt = 0;
            state = LevelState.values()[tag.getInteger(NBT_STATE)];
            if (tag.hasKey(NBT_LINK)) {
                try {
                    this.link = AEApi.instance().storage().loadCraftingLink(tag.getCompoundTag(NBT_LINK), this.tile);
                } catch (Exception ignored) {
                    this.link = null;
                }
            }
        }

        public NBTTagCompound writeToNBT(boolean includeLink) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound stackTag = new NBTTagCompound();
            Platform.writeStackNBT(stack, stackTag, true);
            tag.setTag(NBT_STACK, stackTag);
            tag.setLong(NBT_QUANTITY, quantity);
            tag.setLong(NBT_BATCH, batchSize);
            tag.setBoolean(NBT_ENABLE, enable);
            tag.setInteger(NBT_MIN_TICK, minTick);
            tag.setInteger(NBT_MAX_TICK, maxTick);
            tag.setInteger(NBT_STATE, state.ordinal());
            if (this.link != null && includeLink) {
                NBTTagCompound linkTag = new NBTTagCompound();
                this.link.writeToNBT(linkTag);
                tag.setTag(NBT_LINK, linkTag);
            }
            return tag;
        }

        @NotNull
        public IAEStack<?> getAEStack() {
            return this.stack;
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

        public int getMinTick() {
            return minTick;
        }

        public int getMaxTick() {
            return maxTick;
        }

        public LevelState getState() {
            return state;
        }
    }

    private class LevelMaintainerInventory extends IAEStackInventory {

        public LevelMaintainerInventory(RequestInfo[] requests) {
            super(null, requests.length, StorageName.NONE);
        }

        @Override
        public int getSizeInventory() {
            return requests.length;
        }

        @Override
        public IAEStack<?> getAEStackInSlot(int n) {
            if (requests[n] != null) {
                return requests[n].stack;
            }
            return null;
        }

        @Override
        public void putAEStackInSlot(int slot, IAEStack<?> aes) {
            updateStack(slot, aes);
            super.putAEStackInSlot(slot, aes);
        }

        @Override
        public void markDirty() {

        }
    }
}
