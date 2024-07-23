package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiOptimizePatterns;
import appeng.core.localization.GuiText;

public class GuiFluidOptimizePatterns extends GuiOptimizePatterns {

    private GuiType originalGui;

    public GuiFluidOptimizePatterns(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        if (te instanceof PartFluidPatternTerminal) {
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        } else if (te instanceof PartFluidPatternTerminalEx) {
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        } else if (te instanceof PartLevelTerminal) {
            originalGui = GuiType.LEVEL_TERMINAL;
        } else if (te instanceof IWirelessTerminal terminal && terminal.isUniversal(te)) {
            this.originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
        } else if (te instanceof WirelessPatternTerminalInventory) {
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (te instanceof WirelessLevelTerminalInventory) {
            originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.displayString.equals(GuiText.Cancel.getLocal())) {
            InventoryHandler.switchGui(originalGui);
            return;
        }
        super.actionPerformed(btn);
    }
}
