package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.base.FCGuiAmount;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartFluidTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketCraftRequest;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.localization.GuiText;

public class GuiFluidCraftAmount extends FCGuiAmount {

    public GuiFluidCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerCraftAmount(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.amountBox.setText("1");
        this.amountBox.setSelectionPos(0);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.submit.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();
        try {
            this.submit.enabled = getAmount() > 0;
        } catch (final NumberFormatException e) {
            this.submit.enabled = false;
        }
        this.amountBox.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        try {
            if (btn == this.submit && this.submit.enabled) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketCraftRequest(getAmount(), isShiftKeyDown()));
            }
        } catch (final NumberFormatException e) {
            this.amountBox.setText("1");
        }
    }

    @Override
    protected void setOriginGUI(Object target) {
        if (target instanceof PartFluidPatternTerminal) {
            this.myIcon = new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL, 1);
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        } else if (target instanceof PartFluidPatternTerminalEx) {
            this.myIcon = new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL_EX, 1);
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        } else if (target instanceof PartFluidTerminal) {
            this.myIcon = new ItemStack(ItemAndBlockHolder.FLUID_TERM, 1);
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (target instanceof IWirelessTerminal && ((IWirelessTerminal) target).isUniversal(target)) {
            this.myIcon = new ItemStack(ItemAndBlockHolder.WIRELESS_ULTRA_TERM, 1);
            this.originalGui = ItemWirelessUltraTerminal.readMode(((IWirelessTerminal) target).getItemStack());
        } else if (target instanceof WirelessPatternTerminalInventory) {
            this.myIcon = new ItemStack(ItemAndBlockHolder.WIRELESS_PATTERN_TERM, 1);
            this.originalGui = GuiType.FLUID_TERMINAL;
        }
    }

    @Override
    protected String getBackground() {
        return "guis/craftAmt.png";
    }
}
