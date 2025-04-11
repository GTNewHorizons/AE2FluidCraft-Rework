package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidLevelEmitter;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.util.NameConst;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.core.AEConfig;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiFluidLevelEmitter extends GuiUpgradeable {

    private MEGuiTextField amountTextField;

    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;

    private GuiButton setButton;

    private boolean isMul = false;

    public GuiFluidLevelEmitter(final InventoryPlayer inventoryPlayer, final PartFluidLevelEmitter te) {
        super(new ContainerFluidLevelEmitter(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.amountTextField = new MEGuiTextField(90, 12);
        this.amountTextField.x = this.guiLeft + 39;
        this.amountTextField.y = this.guiTop + 44;
        this.amountTextField.setFocused(true);
        ((ContainerFluidLevelEmitter) this.inventorySlots).setTextField(this.amountTextField);
        this.validateText();
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + 8,
                Settings.REDSTONE_EMITTER,
                RedstoneMode.LOW_SIGNAL);

        final int a = AEConfig.instance.levelByStackAmounts(0);
        final int b = AEConfig.instance.levelByStackAmounts(1);
        final int c = AEConfig.instance.levelByStackAmounts(2);
        final int d = AEConfig.instance.levelByStackAmounts(3);

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 63, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 63, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 63, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 63, 38, 20, "-" + d));

        this.buttonList.add(
                this.setButton = new GuiButton(
                        0,
                        this.guiLeft + 134,
                        this.guiTop + 40,
                        28,
                        20,
                        GuiText.Set.getLocal()));

        this.buttonList.add(this.redstoneMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                getGuiDisplayName(NameConst.i18n(NameConst.GUI_FLUID_LEVEL_EMITTER)),
                8,
                6,
                GuiColors.UpgradableTitle.getColor());
        this.fontRendererObj.drawString(
                GuiText.inventory.getLocal(),
                8,
                this.ySize - 96 + 3,
                GuiColors.UpgradableInventory.getColor());
        this.redstoneMode.set(this.cvb.getRedStoneMode());

        if (isShiftKeyDown() && !isMul) {
            for (GuiButton btn : this.buttonList) {
                if (btn instanceof GuiButton) {
                    btn.displayString += "000";
                }
            }
            isMul = true;
        } else if (!isShiftKeyDown() && isMul) {
            for (GuiButton btn : this.buttonList) {
                if (btn instanceof GuiButton) {
                    btn.displayString = btn.displayString.substring(0, btn.displayString.lastIndexOf("000"));
                }
            }
            isMul = false;
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.amountTextField.drawTextBox();

        // Without these two lines, the liquid slot will not draw properly.
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindTexture(this.getBackground());
    }

    @Override
    protected void handleButtonVisibility() {}

    @Override
    protected String getBackground() {
        return "guis/lvlemitter.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        if (btn == this.setButton && this.setButton.enabled) {
            final long amount = this.getAmountLong();
            this.amountTextField.setText(Long.toString(amount));

            FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(amount, 0));
        } else {
            final boolean isPlus = btn == this.plus1 || btn == this.plus10
                    || btn == this.plus100
                    || btn == this.plus1000;
            final boolean isMinus = btn == this.minus1 || btn == this.minus10
                    || btn == this.minus100
                    || btn == this.minus1000;

            if (isPlus || isMinus) {
                long result = addOrderAmount(this.getQty(btn));
                this.amountTextField.setText(Long.toString(result));
            }
        }
    }

    private long addOrderAmount(final int i) {
        long resultL = getAmountLong();

        if (resultL == 1 && i > 1) {
            resultL = 0;
        }

        resultL += i;
        if (resultL < 1) {
            resultL = 1;
        }
        return resultL;
    }

    private long getAmountLong() {
        String out = this.amountTextField.getText();
        double resultD = Calculator.conversion(out);

        if (resultD <= 0 || Double.isNaN(resultD)) {
            return 0;
        } else {
            return (long) ArithHelper.round(resultD, 0);
        }
    }

    private void validateText() {
        String text = this.amountTextField.getText();
        double resultD = Calculator.conversion(text);
        this.setButton.enabled = !Double.isNaN(resultD);
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        this.amountTextField.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                this.actionPerformed(this.setButton);
            } else {
                boolean typedTextbox = this.amountTextField.textboxKeyTyped(character, key);
                if (typedTextbox) {
                    this.validateText();
                } else {
                    super.keyTyped(character, key);
                }
            }
        }
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }

    public void update(Integer key, IAEFluidStack value) {
        ((ContainerFluidLevelEmitter) this.cvb).getBus().setFluidInSlot(key, value);
    }
}
