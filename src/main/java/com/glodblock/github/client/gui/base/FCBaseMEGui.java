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
import com.glodblock.github.client.gui.GuiInterfaceTerminalWireless;
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
    protected GuiFCImgButton PatternTerminaleEx;
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

    @SuppressWarnings("unchecked")
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
                    this.PatternTerminaleEx = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "PATTERN_EX_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.PatternTerminaleEx);
        }
        if (!(this instanceof GuiFluidPortableCell)) {
            this.buttonList.add(
                    this.FluidTerminal = new GuiFCImgButton(this.guiLeft - 18, this.getOffsetY(), "FLUID_TEM", "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.FluidTerminal);
        }
        if (!(this instanceof GuiInterfaceTerminalWireless)) {
            this.buttonList.add(
                    this.InterfaceTerminal = new GuiFCImgButton(
                            this.guiLeft - 18,
                            this.getOffsetY(),
                            "INTERFACE_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.InterfaceTerminal);
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
            } else if (btn == this.PatternTerminaleEx) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL_EX);
            }
        }
        super.actionPerformed(btn);
    }
}
