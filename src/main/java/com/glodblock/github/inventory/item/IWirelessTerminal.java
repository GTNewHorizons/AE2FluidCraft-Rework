package com.glodblock.github.inventory.item;

import net.minecraft.item.ItemStack;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGridHost;
import appeng.api.networking.security.IActionHost;
import appeng.tile.inventory.IAEAppEngInventory;

public interface IWirelessTerminal
        extends IFluidPortableCell, IViewCellStorage, IAEAppEngInventory, IGridHost, IActionHost {

    default boolean isUniversal(Object holder) {
        if (holder instanceof IGuiItemObject) {
            ItemStack stack = ((IGuiItemObject) holder).getItemStack();
            return stack != null && stack.getItem() instanceof ItemWirelessUltraTerminal;
        }
        return false;
    }

}
