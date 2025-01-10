package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

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
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketPatternMultiSet;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiAmount;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerPatternMulti;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.helpers.Reflected;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiPatternMulti extends GuiAmount {

    private static final int DEFAULT_VALUE = 0;
    private GuiImgButton symbolSwitch;
    protected GuiType originalGui;

    @Reflected
    public GuiPatternMulti(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerPatternMulti(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(
                this.symbolSwitch = new GuiImgButton(
                        this.guiLeft + 22,
                        this.guiTop + 53,
                        Settings.ACTIONS,
                        ActionItems.MULTIPLY));

        this.amountTextField.xPosition = this.guiLeft + 50;
        this.amountTextField.setText(String.valueOf(DEFAULT_VALUE));
        this.amountTextField.setSelectionPos(0);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj
                .drawString(GuiText.SelectAmount.getLocal(), 8, 6, GuiColors.CraftAmountSelectAmount.getColor());
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.nextBtn.displayString = GuiText.Set.getLocal();

        try {
            int resultI = getAmount();

            this.symbolSwitch.set(resultI >= 0 ? ActionItems.MULTIPLY : ActionItems.DIVIDE);
            this.nextBtn.enabled = resultI < -1 || resultI > 1;
        } catch (final NumberFormatException e) {
            this.nextBtn.enabled = false;
        }

        this.amountTextField.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        try {

            if (btn == this.nextBtn && btn.enabled) {
                int resultI = getAmount();
                if (resultI > 1 || resultI < -1)
                    FluidCraft.proxy.netHandler.sendToServer(new CPacketPatternMultiSet(this.originalGui, resultI));
            }
        } catch (final NumberFormatException e) {
            // nope..
            this.amountTextField.setText(String.valueOf(DEFAULT_VALUE));
        }

        if (btn == this.symbolSwitch) {
            int resultI = -getAmount();
            this.amountTextField.setText(Integer.toString(resultI));
        }

    }

    @Override
    protected int getAmount() {
        String out = this.amountTextField.getText();

        double resultD = Calculator.conversion(out);

        if (Double.isNaN(resultD)) {
            return DEFAULT_VALUE;
        } else {
            return (int) ArithHelper.round(resultD, 0);
        }
    }

    @Override
    protected int addOrderAmount(final int i) {
        return i + getAmount();
    }

    @Override
    protected String getBackground() {
        return "guis/patternMulti.png";
    }

    @Override
    protected void setOriginGUI(Object target) {
        if (target instanceof PartFluidPatternTerminal) {
            this.myIcon = ItemAndBlockHolder.FLUID_TERMINAL.stack();
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        } else if (target instanceof PartFluidPatternTerminalEx) {
            this.myIcon = ItemAndBlockHolder.FLUID_TERMINAL_EX.stack();
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        } else if (target instanceof PartFluidTerminal) {
            this.myIcon = ItemAndBlockHolder.FLUID_TERM.stack();
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (target instanceof PartLevelTerminal) {
            myIcon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
            originalGui = GuiType.LEVEL_TERMINAL;
        } else if (target instanceof IWirelessTerminal terminal && terminal.isUniversal(target)) {
            this.myIcon = ItemAndBlockHolder.WIRELESS_ULTRA_TERM.stack();
            this.originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
        } else if (target instanceof WirelessPatternTerminalInventory) {
            this.myIcon = ItemAndBlockHolder.WIRELESS_PATTERN_TERM.stack();
            this.originalGui = GuiType.FLUID_TERMINAL;
        } else if (target instanceof WirelessLevelTerminalInventory) {
            myIcon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
            originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
        }
    }
}
