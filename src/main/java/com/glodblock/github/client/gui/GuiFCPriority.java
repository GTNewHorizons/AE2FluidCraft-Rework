package com.glodblock.github.client.gui;

import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.client.gui.base.FCGuiAmount;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;

import appeng.container.implementations.ContainerPriority;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPriorityHost;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiFCPriority extends FCGuiAmount {

    public GuiFCPriority(final InventoryPlayer inventoryPlayer, final IPriorityHost te) {
        super(new ContainerPriority(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.submit.enabled = false;
        this.submit.visible = false;
        this.buttonList.remove(this.submit);
        ((ContainerPriority) this.inventorySlots).setTextField(this.amountBox);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.Priority.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.amountBox.drawTextBox();
    }

    @Override
    protected void addQty(final int i) {
        try {
            this.amountBox.setText(Long.toString(getAmount() + i));
        } catch (final NumberFormatException ignore) {}
        try {
            NetworkHandler.instance
                    .sendToServer(new PacketValueConfig("PriorityHost.Priority", String.valueOf(getAmount())));
        } catch (IOException e) {
            AELog.debug(e);
        }
    }

    @Override
    protected void setOriginGUI(Object target) {
        if (target instanceof IDualHost) {
            this.myIcon = ItemAndBlockHolder.INTERFACE.stack();
            this.originalGui = GuiType.DUAL_INTERFACE;
        } else if (target instanceof PartFluidStorageBus) {
            this.myIcon = ItemAndBlockHolder.FLUID_STORAGE_BUS.stack();
            this.originalGui = GuiType.FLUID_STORAGE_BUS;
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        super.keyTyped(character, key);
        try {
            NetworkHandler.instance
                    .sendToServer(new PacketValueConfig("PriorityHost.Priority", String.valueOf(getAmount())));
        } catch (IOException e) {
            AELog.debug(e);
        }
    }

    @Override
    protected int getAmount() {
        try {
            String out = this.amountBox.getText();
            double result = Calculator.conversion(out);
            if (Double.isNaN(result)) {
                return 0;
            } else {
                return (int) ArithHelper.round(result, 0);
            }
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    protected String getBackground() {
        return "guis/priority.png";
    }
}
