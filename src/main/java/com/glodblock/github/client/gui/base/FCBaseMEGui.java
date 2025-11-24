package com.glodblock.github.client.gui.base;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

import appeng.client.gui.AEBaseGui;

public abstract class FCBaseMEGui extends AEBaseGui {

    public FCBaseMEGui(final InventoryPlayer inventoryPlayer, Container container) {
        super(container);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    private boolean scheduleGuiResize;

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        super.mouseClicked(xCoord, yCoord, btn);
        if (scheduleGuiResize) {
            // in the GuiScreen class, the implementation of super.mouseClicked
            // ends up looping on the button list and execute the action for any
            // button below the mouse.
            // Therefore, if we initGui() the terminal in the actionPerformed method below
            // it will run the actionPerformed a second time for the new button
            // that will end up being below the mouse (if any) after the initGui()
            buttonList.clear();
            initGui();
            scheduleGuiResize = false;
        }
    }

    protected final void scheduleGuiResize() {
        scheduleGuiResize = true;
    }
}
