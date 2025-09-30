package com.glodblock.github.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;

public class OptionalFluidSlotFake extends OptionalSlotFake {

    public OptionalFluidSlotFake(final IInventory inv, final IOptionalSlotHost containerBus, final int idx, final int x,
            final int y, final int offX, final int offY, final int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
    }

    @Override
    public void putStack(ItemStack is) {
        if (is == null) super.putStack(null);
        FluidStack fluidStack = Util.getFluidFromItem(is);
        if (fluidStack != null) {
            super.putStack(ItemFluidPacket.newStack(fluidStack));
        }
    }
}
