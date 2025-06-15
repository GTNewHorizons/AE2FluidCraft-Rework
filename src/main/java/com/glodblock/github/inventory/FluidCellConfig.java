package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.items.contents.CellConfig;

public class FluidCellConfig extends CellConfig {

    public FluidCellConfig(ItemStack is) {
        super(is);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack newItemStack) {
        if (newItemStack != null && !(newItemStack.getItem() instanceof ItemFluidPacket)) {
            ItemStack fluidPacket = ItemFluidPacket.newDisplayStack(Util.getFluidFromItem(newItemStack));
            if (fluidPacket != null) {
                super.setInventorySlotContents(slot, fluidPacket);
                return;
            }
        }
        super.setInventorySlotContents(slot, newItemStack);
    }
}
