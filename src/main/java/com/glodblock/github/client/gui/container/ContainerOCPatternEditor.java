package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.common.tile.TileOCPatternEditor;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;

public class ContainerOCPatternEditor extends AEBaseContainer {

    public ContainerOCPatternEditor(InventoryPlayer ipl, TileOCPatternEditor tile) {
        super(ipl, tile);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                addSlotToContainer(
                        new SlotRestrictedInput(
                                SlotRestrictedInput.PlacableItemType.PATTERN,
                                tile.getInternalInventory(),
                                j * 4 + i,
                                55 + i * 18,
                                17 + j * 18,
                                ipl));
            }
        }
        bindPlayerInventory(ipl, 0, 104);
    }
}
