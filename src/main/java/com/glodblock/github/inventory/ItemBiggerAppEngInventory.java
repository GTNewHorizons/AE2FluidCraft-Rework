package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import com.glodblock.github.inventory.gui.GuiType;

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
        boolean isCraft = Platform.openNbtData(is).getBoolean("craftingMode");
        boolean isCraftingTerm = Platform.openNbtData(is).getString("mode_main")
                .equals(GuiType.WIRELESS_CRAFTING_TERMINAL.toString());
        boolean isExPattern = Platform.openNbtData(is).getString("mode_main")
                .equals(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL_EX.toString());
        if (!isCraftingTerm && !isExPattern && isCraft) {
            for (int x = 0; x < this.getSizeInventory(); x++) {
                final ItemStack is = this.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
        this.writeToNBT(Platform.openNbtData(is), this.name);
    }

}
