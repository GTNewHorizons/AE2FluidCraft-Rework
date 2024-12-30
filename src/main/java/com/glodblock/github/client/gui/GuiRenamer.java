package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerRenamer;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessInterfaceTerminalInventory;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketRenamer;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;

public class GuiRenamer extends AEBaseGui implements IDropToFillTextField {

    protected final MEGuiTextField textField;

    protected final ITerminalHost host;
    protected GuiTabButton originalGuiBtn;

    protected ItemStack icon = null;

    protected GuiType originalGui;

    public GuiRenamer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(new ContainerRenamer(ip, monitorable));
        this.host = monitorable;
        this.xSize = 256;

        this.textField = new MEGuiTextField(230, 12);
        this.textField.setMaxStringLength(32);
    }

    @Override
    public void initGui() {
        super.initGui();
        FluidCraft.proxy.netHandler.sendToServer(new CPacketRenamer(CPacketRenamer.Action.GET_TEXT));
        if (host instanceof PartLevelTerminal) {
            icon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
            originalGui = GuiType.LEVEL_TERMINAL;
        } else if (host instanceof IWirelessTerminal terminal && terminal.isUniversal(host)) {
            icon = ItemAndBlockHolder.WIRELESS_ULTRA_TERM.stack();
            originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
        } else if (host instanceof WirelessLevelTerminalInventory) {
            icon = ItemAndBlockHolder.WIRELESS_LEVEL_TERM.stack();
            originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
        } else if (host instanceof WirelessInterfaceTerminalInventory) {
            icon = ItemAndBlockHolder.WIRELESS_INTERFACE_TERM.stack();
            originalGui = GuiType.WIRELESS_INTERFACE_TERMINAL;
        }
        if (this.icon != null) {
            this.buttonList.add(
                    this.originalGuiBtn = new GuiTabButton(
                            this.guiLeft + 231,
                            this.guiTop - 4,
                            this.icon,
                            this.icon.getDisplayName(),
                            itemRender));
            this.originalGuiBtn.setHideEdge(13);
        }
        this.textField.x = this.guiLeft + 12;
        this.textField.y = this.guiTop + 35;
        this.textField.setFocused(true);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString(GuiText.Renamer.getLocal(), 12, 8, GuiColors.RenamerTitle.getColor());
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/renamer.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.textField.drawTextBox();
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        this.textField.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketRenamer(this.textField.getText()));
        } else if (!this.textField.textboxKeyTyped(character, key)) {
            super.keyTyped(character, key);
        }
    }

    public boolean isOverTextField(final int mousex, final int mousey) {
        return textField.isMouseIn(mousex, mousey);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == originalGuiBtn) {
            switchGui();
        } else {
            super.actionPerformed(button);
        }
    }

    public void switchGui() {
        if (originalGui != null) {
            InventoryHandler.switchGui(originalGui);
        }
    }

    public void setTextFieldValue(final String displayName, final int mousex, final int mousey, final ItemStack stack) {
        textField.setText(displayName);
    }

    public void postUpdate(String text) {
        this.textField.setText(text);
    }
}
