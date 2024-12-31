package com.glodblock.github.common.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.MEMonitorIFluidHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.IterationCounter;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.prioitylist.PrecisePriorityList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartFluidStorageBus extends PartUpgradeable
        implements IGridTickable, ICellContainer, IMEMonitorHandlerReceiver<IAEFluidStack>, IPriorityHost {

    private final BaseActionSource source;
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);
    private int priority = 0;
    private boolean cached = false;
    private MEMonitorIFluidHandler monitor = null;
    private MEInventoryHandler<IAEFluidStack> handler = null;
    private int handlerHash = 0;
    private boolean wasActive = false;
    private byte resetCacheLogic = 0;
    /**
     * used to read changes once when the list of extractable items was changed
     */
    private boolean readOncePass = false;

    public PartFluidStorageBus(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.getConfigManager().registerSetting(Settings.STICKY_MODE, YesNo.NO);
        this.source = new MachineSource(this);
        if (is.getTagCompound() != null) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag.hasKey("priority")) {
                priority = tag.getInteger("priority");
                // if we don't do this, the tag will stick forever to the storage bus, as it's never cleaned up,
                // even when the item is broken with a pickaxe
                this.is.setTagCompound(null);
            }
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        if (type == PartItemStack.Wrench) {
            final NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("priority", priority);
            final ItemStack copy = this.is.copy();
            copy.setTagCompound(tag);
            return copy;
        }
        return super.getItemStack(type);
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.updateStatus();
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged changedChannels) {
        this.updateStatus();
    }

    protected void updateStatus() {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
                this.getHost().markForUpdate();
            } catch (final GridAccessException e) {
                // NO-OP
            }
        }
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void updateSetting(final IConfigManager manager, @SuppressWarnings("rawtypes") final Enum settingName,
            @SuppressWarnings("rawtypes") final Enum newValue) {
        this.resetCache(true);
        this.getHost().markForSave();
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
        this.resetCache(true);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.priority = data.getInteger("priority");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
        data.setInteger("priority", this.priority);
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return super.getInventoryByName(name);
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.config) {
            this.resetCache(true);
        }
    }

    protected void resetCache(final boolean fullReset) {
        if (this.getHost() == null || this.getHost().getTile() == null
                || this.getHost().getTile().getWorldObj() == null
                || this.getHost().getTile().getWorldObj().isRemote) {
            return;
        }

        if (fullReset) {
            this.resetCacheLogic = 2;
        } else if (this.resetCacheLogic < 2) {
            this.resetCacheLogic = 1;
        }

        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (final GridAccessException ignore) {}
    }

    protected void resetCache() {
        final boolean fullReset = this.resetCacheLogic == 2;
        this.resetCacheLogic = 0;

        final MEInventoryHandler<IAEFluidStack> in = this.getInternalHandler();
        IItemList<IAEFluidStack> before = AEApi.instance().storage().createFluidList();
        if (in != null) {
            before = in.getAvailableItems(before, IterationCounter.fetchNewId());
        }

        this.cached = false;
        if (fullReset) {
            this.handlerHash = 0;
        }

        final MEInventoryHandler<IAEFluidStack> out = this.getInternalHandler();
        if (this.monitor != null) {
            this.monitor.onTick();
        }

        IItemList<IAEFluidStack> after = AEApi.instance().storage().createFluidList();

        if (in != out) {
            if (out != null) {
                after = out.getAvailableItems(after, IterationCounter.fetchNewId());
            }
            Platform.postListChanges(before, after, this, this.source);
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.handler == verificationToken;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change,
            final BaseActionSource source) {
        try {
            if (this.getProxy().isActive()) {
                if (!this.readOncePass) {
                    AccessRestriction currentAccess = (AccessRestriction) this.getConfigManager()
                            .getSetting(Settings.ACCESS);
                    if (!currentAccess.hasPermission(AccessRestriction.READ)) {
                        return;
                    }
                }
                Iterable<IAEFluidStack> filteredChanges = this.filterChanges(change, this.readOncePass);
                this.readOncePass = false;
                if (filteredChanges == null) return;
                this.getProxy().getStorage()
                        .postAlterationOfStoredItems(StorageChannel.FLUIDS, filteredChanges, this.source);
            }
        } catch (final GridAccessException ignore) {}
    }

    /**
     * Filters the changes to only include fluids that pass the handlers extract filter. Will return null if none of the
     * changes match the filter.
     */
    @Nullable
    private Iterable<IAEFluidStack> filterChanges(final Iterable<IAEFluidStack> change, final boolean readOncePass) {
        if (readOncePass) {
            return change;
        }

        if (this.handler != null && this.handler.isExtractFilterActive()
                && !this.handler.getExtractPartitionList().isEmpty()) {
            List<IAEFluidStack> filteredChanges = new ArrayList<>();
            Predicate<IAEFluidStack> extractFilterCondition = this.handler.getExtractFilterCondition();
            for (final IAEFluidStack changedFluid : change) {
                if (extractFilterCondition.test(changedFluid)) {
                    filteredChanges.add(changedFluid);
                }
            }
            return filteredChanges.isEmpty() ? null : Collections.unmodifiableList(filteredChanges);
        }
        return change;
    }

    @Override
    public void onListUpdate() {
        // NO-OP
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void onNeighborChanged() {
        this.resetCache(false);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        if (player.isSneaking()) {
            return false;
        }
        if (Platform.isServer()) {
            InventoryHandler.openGui(
                    player,
                    this.getHost().getTile().getWorldObj(),
                    new BlockPos(this.getHost().getTile()),
                    Objects.requireNonNull(this.getSide()),
                    GuiType.FLUID_STORAGE_BUS);
        }
        return true;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.StorageBus.getMin(), TickRates.StorageBus.getMax(), monitor == null, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.resetCacheLogic != 0) {
            this.resetCache();
        }
        if (this.monitor != null) {
            return this.monitor.onTick();
        }
        return TickRateModulation.SLEEP;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MEInventoryHandler<IAEFluidStack> getInternalHandler() {
        if (this.cached) {
            return this.handler;
        }

        final boolean wasSleeping = this.monitor == null;

        this.cached = true;
        final TileEntity self = this.getHost().getTile();
        final TileEntity target = new BlockPos(self).getOffSet(this.getSide()).getTileEntity();
        final int newHandlerHash = Platform.generateTileHash(target);
        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash) {
            return this.handler;
        }
        this.handlerHash = newHandlerHash;
        this.handler = null;
        this.monitor = null;
        this.readOncePass = true;
        if (target != null) {
            final IExternalStorageHandler esh = AEApi.instance().registries().externalStorage()
                    .getHandler(target, this.getSide().getOpposite(), StorageChannel.FLUIDS, this.source);
            if (esh != null) {
                final IMEInventory<?> inv = esh
                        .getInventory(target, this.getSide().getOpposite(), StorageChannel.FLUIDS, this.source);
                if (inv instanceof final MEMonitorIFluidHandler h) {
                    h.setMode((StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER));
                    h.setActionSource(new MachineSource(this));
                    this.monitor = h;
                }
                if (inv != null) {
                    this.handler = new MEInventoryHandler(inv, StorageChannel.FLUIDS);
                    AccessRestriction currentAccess = (AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS);
                    this.handler.setBaseAccess(currentAccess);
                    this.handler.setWhitelist(
                            this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST
                                    : IncludeExclude.WHITELIST);
                    this.handler.setSticky(this.getInstalledUpgrades(Upgrades.STICKY) > 0);
                    this.handler.setPriority(this.priority);
                    // only READ since READ_WRITE would break compat of existing storage buses
                    // could use a new setting that is applied via button or a card too
                    this.handler.setIsExtractFilterActive(currentAccess == AccessRestriction.READ);

                    if (inv instanceof IMEMonitor) {
                        ((IBaseMonitor) inv).addListener(this, this.handler);
                    }
                    final IItemList<IAEFluidStack> priorityList = AEApi.instance().storage().createFluidList();

                    final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
                    for (int x = 0; x < this.config.getSizeInventory() && x < slotsToUse; x++) {
                        final IAEItemStack is = this.config.getAEStackInSlot(x);
                        if (is != null) priorityList.add(AEFluidStack.create(ItemFluidPacket.getFluidStack(is)));
                    }
                    PrecisePriorityList partitionList = new PrecisePriorityList(priorityList);
                    this.handler.setPartitionList(partitionList);
                    this.handler.setExtractPartitionList(partitionList);
                }
            }
        }

        // update sleep state...
        if (wasSleeping != (this.monitor == null)) {
            try {
                final ITickManager tm = this.getProxy().getTick();
                if (this.monitor == null) {
                    tm.sleepDevice(this.getProxy().getNode());
                } else {
                    tm.wakeDevice(this.getProxy().getNode());
                }
            } catch (final GridAccessException ignore) {}
        }

        try {
            // force grid to update handlers...
            ((GridStorageCache) this.getProxy().getGrid().getCache(IStorageGrid.class)).cellUpdate(null);
        } catch (final GridAccessException ignore) {}

        return this.handler;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(final StorageChannel channel) {
        if (channel == StorageChannel.FLUIDS) {
            final IMEInventoryHandler<IAEFluidStack> out = this.getInternalHandler();
            if (out != null) {
                return Collections.singletonList(out);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.resetCache(true);
    }

    @Override
    public void saveChanges(@SuppressWarnings("rawtypes") final IMEInventory cellInventory) {
        // NO-OP
    }

    public AppEngInternalAEInventory getConfig() {
        return this.config;
    }

    public void setFluidInSlot(int id, IAEFluidStack fluid) {
        ItemStack tmp = ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack());
        this.config.setInventorySlotContents(id, tmp);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageBack.getIcon(),
                FCPartsTexture.PartFluidStorageBus.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon());

        rh.setBounds(3, 3, 15, 13, 13, 16);
        rh.renderInventoryBox(renderer);

        rh.setBounds(2, 2, 14, 14, 14, 15);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 12, 11, 11, 14);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
            final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageBack.getIcon(),
                FCPartsTexture.PartFluidStorageBus.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon());

        rh.setBounds(3, 3, 15, 13, 13, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(2, 2, 14, 14, 14, 15);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageBack.getIcon(),
                FCPartsTexture.PartFluidStorageBus.getIcon(),
                CableBusTextures.PartStorageSides.getIcon(),
                CableBusTextures.PartStorageSides.getIcon());

        rh.setBounds(5, 5, 12, 11, 11, 13);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorBack.getIcon(),
                FCPartsTexture.PartFluidStorageBus.getIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon());

        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderBlock(x, y, z, renderer);

        this.renderLights(x, y, z, rh, renderer);
    }
}
