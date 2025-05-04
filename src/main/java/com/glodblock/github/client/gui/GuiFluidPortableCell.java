package com.glodblock.github.client.gui;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.client.gui.container.ContainerFluidPortableCell;
import com.glodblock.github.inventory.item.IFluidPortableCell;
import com.glodblock.github.inventory.item.IWirelessTerminal;

import appeng.container.slot.AppEngSlot;

public class GuiFluidPortableCell extends GuiFluidTerminal {

    public GuiFluidPortableCell(final InventoryPlayer inventoryPlayer, final IFluidPortableCell te) {
        super(inventoryPlayer, te, new ContainerFluidPortableCell(inventoryPlayer, te));
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    protected int getMaxRows() {
        if (this.container.getHost() instanceof IWirelessTerminal) {
            return super.getMaxRows();
        } else {
            return 3;
        }
    }

    @Override
    protected boolean isPortableCell() {
        return true;
    }
}
