package com.glodblock.github.client.gui;

import java.util.*;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftConfirm;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.network.CPacketSwitchGuis;

public class GuiFluidCraftConfirm extends GuiCraftConfirm {

    private GuiType OriginalGui;

    public GuiFluidCraftConfirm(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);
        if (te instanceof PartFluidPatternTerminal) {
            this.OriginalGui = GuiType.FLUID_PATTERN_TERMINAL;
        } else if (te instanceof PartFluidPatternTerminalEx) {
            this.OriginalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        } else if (te instanceof IWirelessTerminal && ((IWirelessTerminal) te).isUniversal(te)) {
            this.OriginalGui = ItemWirelessUltraTerminal.readMode(((IWirelessTerminal) te).getItemStack());
        } else if (te instanceof WirelessPatternTerminalInventory) {
            this.OriginalGui = GuiType.FLUID_TERMINAL;
        }
    }

    @Override
    public void switchToOriginalGUI() {
        FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(this.OriginalGui));
    }
}
