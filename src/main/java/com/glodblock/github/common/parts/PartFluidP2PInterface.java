package com.glodblock.github.common.parts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.Util;
import com.google.common.collect.ImmutableSet;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelStatic;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.item.AEFluidStack;

public class PartFluidP2PInterface extends PartP2PTunnelStatic<PartFluidP2PInterface>
        implements IGridTickable, IStorageMonitorable, IInventoryDestination, IDualHost, ISidedInventory,
        IAEAppEngInventory, ITileStorageMonitorable, IPriorityHost, IInterfaceHost {

    private final DualityInterface duality = new DualityInterface(this.getProxy(), this) {

        @Override
        public void updateCraftingList() {
            if (!isOutput()) {
                super.updateCraftingList();
                try {
                    for (PartFluidP2PInterface p2p : getOutputs()) p2p.duality.updateCraftingList();
                } catch (GridAccessException e) {
                    // ?
                }
            } else {
                PartFluidP2PInterface p2p = getInput();
                if (p2p != null) {
                    this.craftingList = p2p.duality.craftingList;

                    try {
                        this.gridProxy.getGrid()
                                .postEvent(new MENetworkCraftingPatternChange(this, this.gridProxy.getNode()));
                    } catch (final GridAccessException e) {
                        // :P
                    }
                }
            }
        }

        @Override
        public int getInstalledUpgrades(Upgrades u) {
            if (isOutput() && u == Upgrades.PATTERN_CAPACITY) return -1;
            return super.getInstalledUpgrades(u);
        }
    };
    private final DualityFluidInterface dualityFluid = new DualityFluidInterface(this.getProxy(), this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 6);
    private final BaseActionSource ownActionSource = new MachineSource(this);

    public PartFluidP2PInterface(ItemStack is) {
        super(is);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        dualityFluid.onChannelStateChange(c);
        duality.notifyNeighbors();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        dualityFluid.onPowerStateChange(c);
        duality.notifyNeighbors();
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        dualityFluid.gridChanged();
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        AppEngInternalInventory patterns = (AppEngInternalInventory) duality.getPatterns();
        if (super.onPartActivate(player, pos)) {
            ArrayList<ItemStack> drops = new ArrayList<>();
            for (int i = 0; i < patterns.getSizeInventory(); i++) {
                if (patterns.getStackInSlot(i) == null) continue;
                drops.add(patterns.getStackInSlot(i));
            }
            final IPart tile = this.getHost().getPart(this.getSide());
            if (tile instanceof PartFluidP2PInterface dualTile) {
                DualityInterface newDuality = dualTile.duality;
                // Copy interface storage, upgrades, and settings over
                UpgradeInventory upgrades = (UpgradeInventory) duality.getInventoryByName("upgrades");
                dualTile.duality.getStorage();
                UpgradeInventory newUpgrade = (UpgradeInventory) newDuality.getInventoryByName("upgrades");
                for (int i = 0; i < upgrades.getSizeInventory(); ++i) {
                    newUpgrade.setInventorySlotContents(i, upgrades.getStackInSlot(i));
                }
                IInventory storage = duality.getStorage();
                IInventory newStorage = newDuality.getStorage();
                for (int i = 0; i < storage.getSizeInventory(); ++i) {
                    newStorage.setInventorySlotContents(i, storage.getStackInSlot(i));
                }
                IConfigManager config = duality.getConfigManager();
                config.getSettings().forEach(
                        setting -> newDuality.getConfigManager().putSetting(setting, config.getSetting(setting)));
            }
            TileEntity te = getTileEntity();
            Platform.spawnDrops(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, drops);

            return true;
        }

        if (player.isSneaking()) {
            return false;
        }

        if (Platform.isServer()) {
            InventoryHandler.openGui(
                    player,
                    this.getHost().getTile().getWorldObj(),
                    new BlockPos(this.getHost().getTile()),
                    Objects.requireNonNull(this.getSide()),
                    GuiType.DUAL_INTERFACE);
        }

        return true;
    }

    @Override
    public IIcon getTypeTexture() {
        return ItemAndBlockHolder.INTERFACE.getBlockTextureFromSide(0);
    }

    @Override
    public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {
        return duality.getMonitorable(side, src, this);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        TickingRequest item = duality.getTickingRequest(node);
        TickingRequest fluid = dualityFluid.getTickingRequest(node);
        return new TickingRequest(
                Math.min(item.minTickRate, fluid.minTickRate),
                Math.max(item.maxTickRate, fluid.maxTickRate),
                item.isSleeping && fluid.isSleeping,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        TickRateModulation item = duality.tickingRequest(node, ticksSinceLastCall);
        TickRateModulation fluid = dualityFluid.tickingRequest(node, ticksSinceLastCall);
        if (item.ordinal() >= fluid.ordinal()) {
            return item;
        } else {
            return fluid;
        }
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return duality.getInstalledUpgrades(u);
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return duality.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return duality.getFluidInventory();
    }

    @Override
    public int getPriority() {
        return duality.getPriority();
    }

    @Override
    public void setPriority(int newValue) {
        duality.setPriority(newValue);
    }

    @Override
    public void onTunnelNetworkChange() {
        duality.updateCraftingList();
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation op, ItemStack removedStack,
            ItemStack newStack) {
        duality.onChangeInventory(inv, slot, op, removedStack, newStack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return duality.canInsert(stack);
    }

    @Override
    public int getSizeInventory() {
        return duality.getStorage().getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        return duality.getStorage().getStackInSlot(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return duality.getStorage().decrStackSize(index, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return duality.getStorage().getStackInSlotOnClosing(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        duality.getStorage().setInventorySlotContents(index, stack);
    }

    @Override
    public IInventory getInventoryByName(String name) {
        return duality.getInventoryByName(name);
    }

    @Override
    public IConfigManager getConfigManager() {
        return duality.getConfigManager();
    }

    @Override
    public IIcon getBreakingTexture() {
        return getItemStack().getIconIndex();
    }

    @Override
    public String getInventoryName() {
        return duality.getStorage().getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return duality.getStorage().hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return duality.getStorage().getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        duality.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return duality.getStorage().isUseableByPlayer(player);
    }

    @Override
    public void openInventory() {
        duality.getStorage().openInventory();
    }

    @Override
    public void closeInventory() {
        duality.getStorage().closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return duality.getStorage().isItemValidForSlot(index, stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return duality.getAccessibleSlotsFromSide(side);
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack itemStack, int p_102007_3_) {
        return true;
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return true;
    }

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    private IEnergyGrid getEnergyGrid() {
        try {
            return getProxy().getGrid().getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        IEnergyGrid energyGrid = getEnergyGrid();
        if (energyGrid == null || fluidGrid == null || resource == null) return 0;
        int ori = resource.amount;
        IAEFluidStack remove;
        if (doFill) {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.MODULATE, ownActionSource);
        } else {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, ownActionSource);
        }
        return remove == null ? ori : (int) (ori - remove.getStackSize());
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return dualityFluid.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return dualityFluid.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return dualityFluid.getTankInfo(from);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        getTileEntity().markDirty();
        dualityFluid.onFluidInventoryChanged(inv, slot);
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return dualityFluid.getInternalFluid();
    }

    @Override
    public DualityFluidInterface getDualityFluid() {
        return dualityFluid;
    }

    @Override
    public AppEngInternalAEInventory getConfig() {
        Util.mirrorFluidToPacket(config, dualityFluid.getConfig());
        return config;
    }

    @Override
    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(
                    id,
                    ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
            dualityFluid.getConfig().setFluidInSlot(id, dualityFluid.getStandardFluid(fluid));
        }
    }

    @Override
    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            dualityFluid.getInternalFluid().setFluidInSlot(id, fluid);
        }
    }

    @Override
    public DualityInterface getInterfaceDuality() {
        return duality;
    }

    @Override
    public EnumSet<ForgeDirection> getTargets() {
        return EnumSet.of(this.getSide());
    }

    @Override
    public TileEntity getTileEntity() {
        return super.getHost().getTile();
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        duality.provideCrafting(craftingTracker);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        return duality.pushPattern(patternDetails, table);
    }

    @Override
    public boolean isBusy() {
        return duality.isBusy();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return duality.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        return duality.injectCraftedItems(link, items, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        duality.jobStateChange(link);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        duality.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        duality.writeToNBT(data);
    }

    @Override
    public NBTTagCompound getMemoryCardData() {
        final NBTTagCompound output = super.getMemoryCardData();
        this.duality.getConfigManager().writeToNBT(output);
        return output;
    }

    @Override
    public void pasteMemoryCardData(PartP2PTunnel newTunnel, NBTTagCompound data) throws GridAccessException {
        this.duality.getConfigManager().readFromNBT(data);
        super.pasteMemoryCardData(newTunnel, data);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.duality.initialize();
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        super.getDrops(drops, wrenched);
        duality.addDrops(drops);
    }

    @Override
    public boolean shouldDisplay() {
        return IInterfaceHost.super.shouldDisplay() && !isOutput();
    }
}
