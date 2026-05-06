package com.glodblock.github.client.gui.container;

import static com.glodblock.github.loader.ItemAndBlockHolder.DROP;
import static com.glodblock.github.loader.ItemAndBlockHolder.PACKET;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.inventory.slot.FCSlotRestrictedInput;

import appeng.container.AEBaseContainer;

public class ContainerFluidPacketDecoder extends AEBaseContainer {

    public ContainerFluidPacketDecoder(InventoryPlayer ipl, TileFluidPacketDecoder tile) {
        super(ipl, tile);
        addSlotToContainer(
                new FCSlotRestrictedInput(List.of(PACKET.stack(), DROP.stack()), tile.getInventory(), 0, 80, 35, ipl));
        bindPlayerInventory(ipl, 0, 84);
    }
}
