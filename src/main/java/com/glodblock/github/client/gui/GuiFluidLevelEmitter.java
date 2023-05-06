package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidLevelEmitter;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.NameConst;

import appeng.api.config.*;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.render.AppEngRenderItem;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;

public class GuiFluidLevelEmitter extends GuiUpgradeable {

    private GuiNumberBox level;
    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;
    private boolean isMul = false;

    public GuiFluidLevelEmitter(final InventoryPlayer inventoryPlayer, final PartFluidLevelEmitter te) {
        super(new ContainerFluidLevelEmitter(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.level = new GuiNumberBox(this.fontRendererObj, 24, 43, 79, this.fontRendererObj.FONT_HEIGHT, Long.class);
        this.level.setEnableBackgroundDrawing(false);
        this.level.setMaxStringLength(16);
        this.level.setTextColor(GuiColors.LevelEmitterValue.getColor());
        this.level.setVisible(true);
        this.level.setFocused(true);
        ((ContainerFluidLevelEmitter) this.inventorySlots).setTextField(this.level);
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

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d));

        this.buttonList.add(this.redstoneMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                NameConst.i18n(NameConst.GUI_FLUID_LEVEL_EMITTER),
                8,
                6,
                GuiColors.UpgradableTitle.getColor());
        this.fontRendererObj.drawString(
                GuiText.inventory.getLocal(),
                8,
                this.ySize - 96 + 3,
                GuiColors.UpgradableInventory.getColor());
        this.redstoneMode.set(this.cvb.getRedStoneMode());
        this.level.drawTextBox();
        if (isShiftKeyDown() && !isMul) {
            for (Object btn : this.buttonList) {
                if (btn instanceof GuiButton) {
                    ((GuiButton) btn).displayString += "000";
                }
            }
            isMul = true;
        } else if (!isShiftKeyDown() && isMul) {
            for (Object btn : this.buttonList) {
                if (btn instanceof GuiButton) {
                    ((GuiButton) btn).displayString = ((GuiButton) btn).displayString
                            .substring(0, ((GuiButton) btn).displayString.lastIndexOf("000"));
                }
            }
            isMul = false;
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
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
        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10
                || btn == this.minus100
                || btn == this.minus1000;

        if (isPlus || isMinus) {
            this.addQty(this.getQty(btn));
        }
    }

    private void addQty(final long i) {
        try {
            String Out = this.level.getText();

            boolean Fixed = false;
            while (Out.startsWith("0") && Out.length() > 1) {
                Out = Out.substring(1);
                Fixed = true;
            }

            if (Fixed) {
                this.level.setText(Out);
            }

            if (Out.isEmpty()) {
                Out = "0";
            }

            long result = Long.parseLong(Out);
            result += i;
            if (result < 0) {
                result = 0;
            }

            this.level.setText(Out = Long.toString(result));

            FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(result, 0));
        } catch (final NumberFormatException e) {
            // nope..
            this.level.setText("0");
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if ((key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit(character))
                    && this.level.textboxKeyTyped(character, key)) {
                try {
                    String Out = this.level.getText();

                    boolean Fixed = false;
                    while (Out.startsWith("0") && Out.length() > 1) {
                        Out = Out.substring(1);
                        Fixed = true;
                    }

                    if (Fixed) {
                        this.level.setText(Out);
                    }

                    if (Out.isEmpty()) {
                        Out = "0";
                    }
                    FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(Long.parseLong(Out), 0));
                } catch (final NumberFormatException e) {
                    AELog.debug(e);
                }
            } else {
                super.keyTyped(character, key);
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
