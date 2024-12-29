package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.base.FCGuiAmount;
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
import com.glodblock.github.network.CPacketCraftRequest;

import appeng.api.config.CraftingMode;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class GuiFluidCraftAmount extends FCGuiAmount {

    private GuiImgButton craftingMode;

    public GuiFluidCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerCraftAmount(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(
                this.craftingMode = new GuiImgButton(
                        this.guiLeft + 10,
                        this.guiTop + 53,
                        Settings.CRAFTING_MODE,
                        CraftingMode.STANDARD));

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
            if (btn == this.craftingMode) {
                GuiImgButton iBtn = ((GuiImgButton) btn);

                final Enum<?> cv = iBtn.getCurrentValue();
                final boolean backwards = Mouse.isButtonDown(1);
                final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

                iBtn.set(next);
            } else if (btn == this.submit && this.submit.enabled) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketCraftRequest(
                                getAmount(),
                                isShiftKeyDown(),
                                ((CraftingMode) this.craftingMode.getCurrentValue())));
            }
        } catch (final NumberFormatException e) {
            this.amountBox.setText("1");
        }
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

    @Override
    protected String getBackground() {
        return "guis/craftAmt.png";
    }
}
