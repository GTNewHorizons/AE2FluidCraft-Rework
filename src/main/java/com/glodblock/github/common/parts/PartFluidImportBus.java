package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseImportBus;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.item.AEFluidStack;

public class PartFluidImportBus extends PartBaseImportBus<IAEFluidStack> {

    public PartFluidImportBus(ItemStack is) {
        super(is);
    }

    @Override
    public IIcon getFaceIcon() {
        return FCPartsTexture.PartFluidImportBus.getIcon();
    }

    @Override
    protected Object getTarget() {
        final TileEntity self = this.getHost().getTile();
        final TileEntity target = this.getTileEntity(
                self,
                self.xCoord + this.getSide().offsetX,
                self.yCoord + this.getSide().offsetY,
                self.zCoord + this.getSide().offsetZ);

        return target instanceof IFluidHandler handler ? handler : null;
    }

    @Override
    public int calculateAmountToSend() {
        double amount = 1000D;
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 4:
                amount = amount * 1.5;
            case 3:
                amount = amount * 2;
            case 2:
                amount = amount * 4;
            case 1:
                amount = amount * 8;
        }
        switch (this.getInstalledUpgrades(Upgrades.SUPERSPEED)) {
            case 4:
                amount = amount * 8;
            case 3:
                amount = amount * 12;
            case 2:
                amount = amount * 16;
            case 1:
                amount = amount * 32;
        }
        return (int) Math.floor(amount);
    }

    @Override
    protected IMEMonitor<IAEFluidStack> getMonitor() {
        try {
            return this.getProxy().getStorage().getFluidInventory();
        } catch (final GridAccessException e) {
            return null;
        }
    }

    @Override
    protected int getAdaptorFlags() {
        return InventoryAdaptor.DEFAULT & ~InventoryAdaptor.ALLOW_ITEMS;
    }

    @Override
    protected boolean importStuff(final Object myTarget, final IAEFluidStack whatToImport,
            final IMEMonitor<IAEFluidStack> inv, final IEnergySource energy, final FuzzyMode fzMode) {
        final IFluidHandler fh = (IFluidHandler) myTarget;

        FluidTankInfo[] tanksInfo = fh.getTankInfo(this.getSide().getOpposite());
        if (tanksInfo == null) return true;

        int maxDrain = this.calculateAmountToSend();

        for (FluidTankInfo tankInfo : tanksInfo) {
            if (tankInfo.fluid == null) continue;

            FluidStack fluidStack = new FluidStack(tankInfo.fluid, Math.min(tankInfo.fluid.amount, maxDrain));
            fluidStack = fh.drain(this.getSide().getOpposite(), fluidStack, false);
            if (this.filterEnabled() && !this.isInFilter(fluidStack)) continue;

            final AEFluidStack aeFluidStack = AEFluidStack.create(fluidStack);
            if (aeFluidStack != null) {
                final IAEFluidStack notInserted = inv.injectItems(aeFluidStack, Actionable.MODULATE, this.mySrc);

                if (notInserted != null && notInserted.getStackSize() > 0) {
                    aeFluidStack.decStackSize(notInserted.getStackSize());
                }

                FluidStack drained = fh.drain(this.getSide().getOpposite(), aeFluidStack.getFluidStack(), true);
                if (drained.amount > 0) {
                    this.worked = true;
                }

                maxDrain -= drained.amount;
                if (maxDrain <= 0) break;
            }
        }

        return true;
        return true;
    }

    @Override
    protected boolean doOreDict(final Object myTarget, IMEMonitor<IAEFluidStack> inv, IEnergyGrid energy,
            FuzzyMode fzMode) {
        return false;
    }

    @Override
    protected int getPowerMultiplier() {
        return 1000;
    }

    private boolean isInFilter(FluidStack fluid) {
        final IAEStackInventory inv = this.getAEInventoryByName(StorageName.NONE);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            final IAEFluidStack stack = (IAEFluidStack) inv.getAEStackInSlot(i);
            if (stack != null && stack.getFluidStack().equals(fluid)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterEnabled() {
        final IAEStackInventory inv = this.getAEInventoryByName(StorageName.NONE);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            final IAEFluidStack stack = (IAEFluidStack) inv.getAEStackInSlot(i);
            if (stack != null) {
                return true;
            }
        }
        return false;
    }

    // legacy
    @Override
    public void readFromNBT(NBTTagCompound extra) {
        super.readFromNBT(extra);

        final IAEStackInventory config = this.getAEInventoryByName(StorageName.NONE);
        for (int i = 0; i < config.getSizeInventory(); i++) {
            final IAEStack<?> stack = config.getAEStackInSlot(i);
            if (stack instanceof IAEItemStack ais) {
                config.putAEStackInSlot(i, Util.getAEFluidFromItem(ais.getItemStack()));
            }
        }
    }

    @Override
    public IAEStackType<IAEFluidStack> getStackType() {
        return FLUID_STACK_TYPE;
    }
}
