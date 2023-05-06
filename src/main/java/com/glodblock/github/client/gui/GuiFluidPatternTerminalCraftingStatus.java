package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartFluidTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessFluidTerminalInventory;
import com.glodblock.github.inventory.item.WirelessInterfaceTerminalInventory;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2ReflectClient;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private final ITerminalHost host;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        host = te;
    }

    @Override
    public void initGui() {
        if (host instanceof PartFluidPatternTerminal)
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL, 1));
        else if (host instanceof PartFluidPatternTerminalEx)
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL_EX, 1));
        else if (host instanceof PartFluidTerminal) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERM, 1));
        } else if (host instanceof IWirelessTerminal && ((IWirelessTerminal) host).isUniversal(host)) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_ULTRA_TERM, 1));
        } else if (host instanceof WirelessFluidTerminalInventory) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_FLUID_TERM, 1));
        } else if (host instanceof WirelessPatternTerminalInventory) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_PATTERN_TERM, 1));
        } else if (host instanceof WirelessInterfaceTerminalInventory) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_INTERFACE_TERM, 1));
        }
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
            else if (host instanceof IWirelessTerminal && ((IWirelessTerminal) host).isUniversal(host)) InventoryHandler
                    .switchGui(ItemWirelessUltraTerminal.readMode(((IWirelessTerminal) host).getItemStack()));
            else if (host instanceof WirelessFluidTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_TERMINAL);
            else if (host instanceof WirelessPatternTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
            else if (host instanceof WirelessInterfaceTerminalInventory)
                InventoryHandler.switchGui(GuiType.WIRELESS_INTERFACE_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }
}
