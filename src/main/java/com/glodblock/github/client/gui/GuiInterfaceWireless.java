package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.client.gui.container.ContainerInterfaceWireless;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.client.gui.implementations.GuiInterfaceTerminal;

public class GuiInterfaceWireless extends GuiInterfaceTerminal {

    protected GuiFCImgButton FluidTerminal;
    protected GuiFCImgButton CraftingTerminal;
    protected GuiFCImgButton PatternTerminal;
    protected GuiFCImgButton EssentiaTerminal;
    protected GuiFCImgButton InterfaceTerminal;
    protected GuiFCImgButton LevelTerminal;
    protected GuiFCImgButton PatternTerminalEx;

    public GuiInterfaceWireless(final InventoryPlayer inventoryPlayer, final IWirelessTerminal te) {
        super(new ContainerInterfaceWireless(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        drawSwitchGuiBtns();
    }

    protected void drawSwitchGuiBtns() {
        this.buttonList.add(this.CraftingTerminal = new GuiFCImgButton(this.guiLeft - 18, offsetY, "CRAFT_TEM", "YES"));
        offsetY += 20;

        this.buttonList
                .add(this.PatternTerminal = new GuiFCImgButton(this.guiLeft - 18, offsetY, "PATTERN_TEM", "YES"));
        offsetY += 20;

        this.buttonList
                .add(this.PatternTerminalEx = new GuiFCImgButton(this.guiLeft - 18, offsetY, "PATTERN_EX_TEM", "YES"));
        offsetY += 20;

        this.buttonList.add(this.FluidTerminal = new GuiFCImgButton(this.guiLeft - 18, offsetY, "FLUID_TEM", "YES"));
        offsetY += 20;

        this.buttonList.add(this.LevelTerminal = new GuiFCImgButton(this.guiLeft - 18, offsetY, "LEVEL_TEM", "YES"));
        offsetY += 20;

        if (ModAndClassUtil.ThE) {
            this.buttonList
                    .add(this.EssentiaTerminal = new GuiFCImgButton(this.guiLeft - 18, offsetY, "ESSENTIA_TEM", "YES"));
            offsetY += 20;
        }
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

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn instanceof GuiFCImgButton) {
            if (btn == this.FluidTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_TERMINAL);
            } else if (btn == this.CraftingTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_CRAFTING_TERMINAL);
            } else if (btn == this.EssentiaTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_ESSENTIA_TERMINAL);
            } else if (btn == this.PatternTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
            } else if (btn == this.InterfaceTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_INTERFACE_TERMINAL);
            } else if (btn == this.LevelTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_LEVEL_TERMINAL);
            } else if (btn == this.PatternTerminalEx) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL_EX);
            }
        }

        super.actionPerformed(btn);

        if (btn == this.terminalStyleBox) {
            scheduleGuiResize();
        }
    }
}
