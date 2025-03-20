package com.glodblock.github.common.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;

public class CreativeFluidCellInventory extends FluidCellInventory {

    public CreativeFluidCellInventory(ItemStack o, ISaveProvider container) throws AppEngException {
        super(o, container);
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        if (this.getCellFluids().findPrecise(input) != null) {
            return null;
        } else {
            return input;
        }
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }
        if (this.getCellFluids().findPrecise(request) != null) {
            return request.copy();
        } else {
            return null;
        }

    }

    @Override
    protected void loadCellFluids() {
        if (this.cellFluids == null) {
            this.cellFluids = AEApi.instance().storage().createFluidList();
        }
        this.cellFluids.resetStatus(); // clears totals and stuff.
        IInventory inv = this.cellType.getConfigInventory(this.cellItem);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack is = inv.getStackInSlot(i);
            FluidStack fs = Util.getFluidFromItem(is);
            if (fs == null) continue;
            IAEFluidStack iaeFluidStack = Util.FluidUtil.createAEFluidStack(fs);
            if (this.cellFluids.findPrecise(iaeFluidStack) == null) {
                iaeFluidStack.setStackSize(Integer.MAX_VALUE * 1_000_000L);
                this.cellFluids.add(iaeFluidStack);
            }
        }
    }
}
