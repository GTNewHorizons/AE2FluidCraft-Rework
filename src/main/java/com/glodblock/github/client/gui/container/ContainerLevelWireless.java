package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPrimaryGui;

import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;

public class ContainerLevelWireless extends ContainerLevelTerminal {

    public ContainerLevelWireless(InventoryPlayer ip, IWirelessTerminal monitorable) {
        super(ip, monitorable);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }

    @Override
    public PrimaryGui createPrimaryGui() {
        ContainerOpenContext context = getOpenContext();
        return new FluidPrimaryGui(
                GuiType.WIRELESS_LEVEL_TERMINAL,
                ItemAndBlockHolder.WIRELESS_LEVEL_TERM.stack(),
                context.getTile(),
                context.getSide());
    }
}
