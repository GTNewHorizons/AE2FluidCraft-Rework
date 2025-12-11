package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.util.Platform;

public class ItemBiggerAppEngInventory extends BiggerAppEngInventory {

    private final ItemStack is;
    private final String name;

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size) {
        super(null, size);
        this.name = name;
        this.is = is;
        this.readFromNBT(Platform.openNbtData(is), name);
    }

    public void setCraftingMode(boolean mode) {
        Platform.openNbtData(is).setBoolean("craftingMode", mode);
    }

    public boolean getCraftingMode(boolean mode) {
        return Platform.openNbtData(is).getBoolean("craftingMode");
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
    }

}
