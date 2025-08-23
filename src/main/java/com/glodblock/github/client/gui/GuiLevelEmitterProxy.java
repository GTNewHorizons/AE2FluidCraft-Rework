package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketLevelTerminalCommands;
import com.glodblock.github.util.Util;

import appeng.api.util.DimensionalCoord;
import appeng.client.gui.implementations.GuiLevelEmitter;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.parts.automation.PartLevelEmitter;

public class GuiLevelEmitterProxy extends GuiLevelEmitter {

    protected GuiType originalGui;
    protected Util.DimensionalCoordSide originalBlockPos;
    protected ItemStack originalGuiIcon;
    protected GuiTabButton originalGuiBtn;

    public GuiLevelEmitterProxy(final InventoryPlayer inventoryPlayer, final PartLevelEmitter te) {
        super(inventoryPlayer, te);

        if (inventoryPlayer.player.openContainer instanceof AEBaseContainer container) {
            var target = container.getTarget();
            if (target instanceof PartLevelTerminal terminal) {
                originalGuiIcon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
                originalGui = GuiType.LEVEL_TERMINAL;
                DimensionalCoord blockPos = new DimensionalCoord(terminal.getTile());
                originalBlockPos = new Util.DimensionalCoordSide(
                        blockPos.x,
                        blockPos.y,
                        blockPos.z,
                        blockPos.getDimension(),
                        terminal.getSide(),
                        "");
            } else if (target instanceof IWirelessTerminal terminal && terminal.isUniversal(target)) {
                originalGuiIcon = ItemAndBlockHolder.WIRELESS_ULTRA_TERM.stack();
                originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
                originalBlockPos = new Util.DimensionalCoordSide(
                        terminal.getInventorySlot(),
                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                        0,
                        inventoryPlayer.player.worldObj.provider.dimensionId,
                        ForgeDirection.UNKNOWN,
                        "");
            } else if (target instanceof WirelessLevelTerminalInventory terminal) {
                originalGuiIcon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
                originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
                originalBlockPos = new Util.DimensionalCoordSide(
                        terminal.getInventorySlot(),
                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                        0,
                        inventoryPlayer.player.worldObj.provider.dimensionId,
                        ForgeDirection.UNKNOWN,
                        "");

            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (this.originalGuiIcon != null) {
            this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 151,
                    this.guiTop - 4,
                    this.originalGuiIcon,
                    this.originalGuiIcon.getDisplayName(),
                    itemRender);
            this.originalGuiBtn.setHideEdge(13);
            this.buttonList.add(originalGuiBtn);
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn == this.originalGuiBtn) {
            CPacketLevelTerminalCommands message = new CPacketLevelTerminalCommands(
                    CPacketLevelTerminalCommands.Action.BACK,
                    originalBlockPos.x,
                    originalBlockPos.y,
                    originalBlockPos.z,
                    originalBlockPos.getDimension(),
                    originalBlockPos.getSide());
            message.setOriginalGui(originalGui.ordinal());
            FluidCraft.proxy.netHandler.sendToServer(message);
        } else {
            super.actionPerformed(btn);
        }
    }
}
