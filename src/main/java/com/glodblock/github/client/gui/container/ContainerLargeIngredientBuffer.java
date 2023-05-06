package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.common.tile.TileLargeIngredientBuffer;

import appeng.container.slot.SlotNormal;

public class ContainerLargeIngredientBuffer extends ContainerIngredientBuffer {

    public ContainerLargeIngredientBuffer(InventoryPlayer ipl, TileLargeIngredientBuffer tile) {
        super(ipl, tile);
    }

    protected void addSlots(IInventory inv) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotNormal(inv, i * 9 + j, 8 + 18 * j, 72 + 18 * i));
            }
        }
    }
}
