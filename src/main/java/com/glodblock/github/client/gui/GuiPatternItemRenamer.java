package com.glodblock.github.client.gui;

import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartFluidTerminal;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.network.SPacketPatternItemRenamer;

import appeng.api.storage.ITerminalHost;

public class GuiPatternItemRenamer extends appeng.client.gui.implementations.GuiPatternItemRenamer {

    private GuiType originalGui;

    public GuiPatternItemRenamer(InventoryPlayer ip, ITerminalHost p) {
        super(ip, p);
    }

    @Override
    protected void setOriginGUI(Object target) {
        if (target instanceof PartFluidPatternTerminal) {
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        } else if (target instanceof PartFluidPatternTerminalEx) {
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        } else if (target instanceof PartFluidTerminal) {
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (target instanceof PartLevelTerminal) {
            originalGui = GuiType.LEVEL_TERMINAL;
        } else if (target instanceof IWirelessTerminal terminal && terminal.isUniversal(target)) {
            this.originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
        } else if (target instanceof WirelessPatternTerminalInventory) {
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (target instanceof WirelessLevelTerminalInventory) {
            originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            FluidCraft.proxy.netHandler
                    .sendToServer(new SPacketPatternItemRenamer(originalGui, getText(), getValueIndex()));
        } else super.keyTyped(character, key);
    }
}
