package com.glodblock.github.common.storage;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class FluidVoidStorageCellInventory extends FluidCellInventory {

    public FluidVoidStorageCellInventory(ItemStack o, ISaveProvider container) throws AppEngException {
        super(o, container);
    }

    @Override
    public boolean canHoldNewFluid() {
        return true;
    }

    @Override
    public long getTotalBytes() {
        return 0;
    }

    @Override
    public long getFreeBytes() {
        return 0;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getTotalFluidTypes() {
        return 63;
    }

    @Override
    public long getStoredFluidCount() {
        return 0;
    }

    @Override
    public long getStoredFluidTypes() {
        return 0;
    }

    @Override
    public long getRemainingFluidTypes() {
        return 63;
    }

    @Override
    public long getRemainingFluidCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getUnusedFluidCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        return 1;
    }

    @Override
    protected void loadCellFluids() {}

    @Override
    protected IItemList<IAEFluidStack> getCellFluids() {
        return null;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        return null;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        return null;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out, int iteration) {
        return out;
    }

    @Override
    public IAEFluidStack getAvailableItem(@Nonnull IAEFluidStack request, int iteration) {
        return null;
    }

    @Override
    public List<IAEFluidStack> getContents() {
        return null;
    }
}
