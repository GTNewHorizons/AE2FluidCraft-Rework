package com.glodblock.github.client.gui.base;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

import com.glodblock.github.client.gui.GuiEssentiaTerminal;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.GuiFluidPatternExWireless;
import com.glodblock.github.client.gui.GuiFluidPatternWireless;
import com.glodblock.github.client.gui.GuiFluidPortableCell;
import com.glodblock.github.client.gui.GuiInterfaceWireless;
import com.glodblock.github.client.gui.GuiLevelWireless;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.client.gui.AEBaseMEGui;

public abstract class FCBaseMEGui extends AEBaseMEGui {

    protected GuiFCImgButton FluidTerminal;
    protected GuiFCImgButton CraftingTerminal;
    protected GuiFCImgButton PatternTerminal;
    protected GuiFCImgButton EssentiaTerminal;
    protected GuiFCImgButton InterfaceTerminal;
    protected GuiFCImgButton LevelTerminal;
    protected GuiFCImgButton PatternTerminalEx;
    protected List<GuiFCImgButton> termBtns = new ArrayList<>();
    protected boolean drawSwitchGuiBtn;

    public FCBaseMEGui(final InventoryPlayer inventoryPlayer, Container container) {
        super(container);
        if (container instanceof FCBaseContainer) {
            Object target = ((FCBaseContainer) container).getTarget();
            if (target instanceof IWirelessTerminal
                    && ((IWirelessTerminal) target).getItemStack().getItem() instanceof ItemWirelessUltraTerminal) {
                this.drawSwitchGuiBtn = true;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    protected void initGuiDone() {
        if (drawSwitchGuiBtn) {
            drawSwitchGuiBtns();
        }
    }

    public abstract int getOffsetY();

    public abstract void setOffsetY(int y);

    protected void drawSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        if (!termBtns.isEmpty()) {
            this.termBtns.clear();
        }
        if (!(this instanceof GuiFluidCraftingWireless)) {
            this.buttonList.add(
                    this.CraftingTerminal = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "CRAFT_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.CraftingTerminal);
        }
        if (!(this instanceof GuiFluidPatternWireless)) {
            this.buttonList.add(
                    this.PatternTerminal = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "PATTERN_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.PatternTerminal);
        }
        if (!(this instanceof GuiFluidPatternExWireless)) {
            this.buttonList.add(
                    this.PatternTerminalEx = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "PATTERN_EX_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.PatternTerminalEx);
        }
        if (!(this instanceof GuiFluidPortableCell)) {
            this.buttonList.add(
                    this.FluidTerminal = new GuiFCImgButton(this.guiLeft - 18, this.getOffsetY(), "FLUID_TEM", "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.FluidTerminal);
        }
        if (!(this instanceof GuiInterfaceWireless)) {
            this.buttonList.add(
                    this.InterfaceTerminal = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "INTERFACE_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.InterfaceTerminal);
        }
        if (!(this instanceof GuiLevelWireless)) {
            this.buttonList.add(
                    this.LevelTerminal = new GuiFCImgButton(this.guiLeft - 18, this.getOffsetY(), "LEVEL_TEM", "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.LevelTerminal);
        }
        if (ModAndClassUtil.ThE && !(this instanceof GuiEssentiaTerminal)) {
            this.buttonList.add(
                    this.EssentiaTerminal = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "ESSENTIA_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.EssentiaTerminal);
        }
    }

    protected void addSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        this.buttonList.addAll(termBtns);
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
    }
}
