package com.glodblock.github.nei;

import net.minecraft.client.gui.inventory.GuiContainer;

import com.glodblock.github.client.gui.GuiFluidMonitor;
import com.glodblock.github.client.gui.GuiItemMonitor;

import codechicken.nei.api.INEIGuiAdapter;

public class NEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (gui instanceof GuiItemMonitor) {
            return ((GuiItemMonitor) gui).hideItemPanelSlot(x, y, w, h);
        } else if (gui instanceof GuiFluidMonitor) {
            return ((GuiFluidMonitor) gui).hideItemPanelSlot(x, y, w, h);
        }
        return false;
    }
}
