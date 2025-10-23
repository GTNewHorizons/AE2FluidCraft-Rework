package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.IGuiSub;
import appeng.client.gui.implementations.GuiLevelEmitter;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.IContainerSubGui;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.ILevelEmitter;

public class GuiLevelEmitterProxy extends GuiLevelEmitter implements IGuiSub {

    protected final IContainerSubGui container;
    protected GuiTabButton originalGuiBtn;;

    public GuiLevelEmitterProxy(final InventoryPlayer inventoryPlayer, final ILevelEmitter te) {
        super(inventoryPlayer, te);
        this.container = (IContainerSubGui) inventorySlots;
        this.container.setGuiLink(this);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (container.getPrimaryGuiIcon() != null) initPrimaryGuiButton();
    }

    public void initPrimaryGuiButton() {
        this.buttonList.add(
                this.originalGuiBtn = new GuiTabButton(
                        this.guiLeft + this.xSize - 57,
                        this.guiTop,
                        container.getPrimaryGuiIcon(),
                        container.getPrimaryGuiIcon().getDisplayName(),
                        itemRender));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        if (btn == this.originalGuiBtn) {
            NetworkHandler.instance.sendToServer(new PacketSwitchGuis());
        }
    }
}
