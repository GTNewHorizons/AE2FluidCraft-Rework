package com.glodblock.github.inventory.item;

import net.minecraft.inventory.IInventory;

import appeng.api.implementations.guiobjects.IGuiItemObject;

public interface IItemTerminal extends IGuiItemObject {

    IInventory getInventoryByName(final String name);

    boolean getSyncData();

    void setSyncData(boolean sync);

    default void saveSettings() {};

}
