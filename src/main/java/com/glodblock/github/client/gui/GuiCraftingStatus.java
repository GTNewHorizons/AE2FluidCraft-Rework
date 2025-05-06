package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartFluidTerminal;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessFluidTerminalInventory;
import com.glodblock.github.inventory.item.WirelessInterfaceTerminalInventory;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.Ae2ReflectClient;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.helpers.InventoryAction;

public class GuiCraftingStatus extends appeng.client.gui.implementations.GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private final ITerminalHost host;

    public GuiCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        host = te;
    }

    @Override
    public void initGui() {
        if (host instanceof PartFluidPatternTerminal)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.FLUID_TERMINAL.stack());
        else if (host instanceof PartFluidPatternTerminalEx)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.FLUID_TERMINAL_EX.stack());
        else if (host instanceof PartFluidTerminal)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.FLUID_TERM.stack());
        else if (host instanceof PartLevelTerminal)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.LEVEL_TERMINAL.stack());
        else if (host instanceof IWirelessTerminal terminal && terminal.isUniversal(host))
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.WIRELESS_ULTRA_TERM.stack());
        else if (host instanceof WirelessFluidTerminalInventory)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.WIRELESS_FLUID_TERM.stack());
        else if (host instanceof WirelessPatternTerminalInventory)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.WIRELESS_PATTERN_TERM.stack());
        else if (host instanceof WirelessInterfaceTerminalInventory)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.WIRELESS_INTERFACE_TERM.stack());
        else if (host instanceof WirelessLevelTerminalInventory)
            Ae2ReflectClient.rewriteIcon(this, ItemAndBlockHolder.WIRELESS_LEVEL_TERM.stack());
        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == originalGuiBtn) {
            if (host instanceof PartFluidPatternTerminal) InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
            else if (host instanceof PartFluidPatternTerminalEx)
                InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL_EX);
            else if (host instanceof PartFluidTerminal) InventoryHandler.switchGui(GuiType.FLUID_TERMINAL);
            else if (host instanceof PartLevelTerminal) InventoryHandler.switchGui(GuiType.LEVEL_TERMINAL);
            else if (host instanceof IWirelessTerminal terminal && terminal.isUniversal(host))
                InventoryHandler.switchGui(ItemWirelessUltraTerminal.readMode(terminal.getItemStack()));
            else if (host instanceof WirelessFluidTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_TERMINAL);
            else if (host instanceof WirelessPatternTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
            else if (host instanceof WirelessInterfaceTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_INTERFACE_TERMINAL);
            else if (host instanceof WirelessLevelTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_LEVEL_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        IAEItemStack hoveredAEStack = getHoveredAEStack();
        if (hoveredAEStack != null && btn == 2) {
            ((AEBaseContainer) inventorySlots).setTargetStack(hoveredAEStack);
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketInventoryAction(
                            InventoryAction.AUTO_CRAFT,
                            Ae2ReflectClient.getInventorySlots(this).size(),
                            0,
                            hoveredAEStack));
            return;
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }
}
