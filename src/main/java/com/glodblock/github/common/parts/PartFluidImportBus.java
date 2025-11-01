package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.util.BlockPos;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseImportBus;
import appeng.tile.inventory.IAEStackInventory;
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
        TileEntity self = this.getHost().getTile();
        return this.getTileEntity(self, (new BlockPos(self)).getOffSet(this.getSide()));
    }

    private TileEntity getTileEntity(final TileEntity self, final BlockPos pos) {
        final World w = self.getWorldObj();

        if (w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
            return w.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        }

        return null;
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
    protected boolean importStuff(final Object myTarget, final IAEFluidStack whatToImport,
            final IMEMonitor<IAEFluidStack> inv, final IEnergySource energy, final FuzzyMode fzMode) {
        if (myTarget instanceof IFluidHandler fh) {
            FluidTankInfo[] tanksInfo = fh.getTankInfo(this.getSide().getOpposite());
            if (tanksInfo == null) return false;

            int maxDrain = this.calculateAmountToSend();
            boolean drained = false;

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

                    fh.drain(this.getSide().getOpposite(), aeFluidStack.getFluidStack(), true);
                    maxDrain -= aeFluidStack.getFluidStack().amount;
                    drained = true;
                }
            }

            return drained;
        }

        return false;
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
}
