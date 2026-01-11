package com.glodblock.github.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.network.CPacketLevelMaintainer.Action;
import com.glodblock.github.util.FCGuiColors;
import com.glodblock.github.util.NameConst;

import appeng.api.features.LevelState;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.GuiSub;
import appeng.client.gui.slots.VirtualMEPhantomSlot;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;
import cofh.core.render.CoFHFontRenderer;

public class GuiLevelMaintainer extends GuiSub {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private final ContainerLevelMaintainer cont;
    private final Component[] component = new Component[TileLevelMaintainer.REQ_COUNT];
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private Widget focusedWidget;
    private final CoFHFontRenderer render;

    public GuiLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(new ContainerLevelMaintainer(ipl, tile));
        this.cont = (ContainerLevelMaintainer) inventorySlots;
        this.xSize = 195;
        this.ySize = 214;
        this.render = new CoFHFontRenderer(
                Minecraft.getMinecraft().gameSettings,
                TEX_BG,
                Minecraft.getMinecraft().getTextureManager(),
                true);
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            VirtualMEPhantomSlot slot = new VirtualMEPhantomSlot(
                    27,
                    20 + i * 19,
                    this.cont.getTile().getAEStackInventory(),
                    i);
            slot.setShowAmount(true);
            slot.setShowAmountAlways(true);
            this.registerVirtualSlots(slot);
        }

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            component[i] = new Component(
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 46, guiTop + 19 + 19 * i, 52, 14),
                            NameConst.TT_LEVEL_MAINTAINER_REQUEST_SIZE,
                            i,
                            Action.Quantity),
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 100, guiTop + 19 + 19 * i, 52, 14),
                            NameConst.TT_LEVEL_MAINTAINER_BATCH_SIZE,
                            i,
                            Action.Batch),
                    new GuiFCImgButton(guiLeft + 105 + 47, guiTop + 17 + 19 * i, "SUBMIT", "SUBMIT", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "ENABLE", "ENABLE", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "DISABLE", "DISABLE", false),
                    new FCGuiLineField(fontRendererObj, guiLeft + 47, guiTop + 33 + 19 * i, 120),
                    this.buttonList,
                    this.cont);
        }
    }

    @Override
    public void initPrimaryGuiButton() {
        this.originalGuiBtn = new GuiTabButton(
                this.guiLeft + 151,
                this.guiTop - 4,
                this.cont.getPrimaryGuiIcon(),
                this.cont.getPrimaryGuiIcon().getDisplayName(),
                itemRender);
        this.originalGuiBtn.setHideEdge(13);
        this.buttonList.add(originalGuiBtn);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        for (Component com : this.component) {
            com.getQty().textField.handleTooltip(mouseX, mouseY, this);
            com.getBatch().textField.handleTooltip(mouseX, mouseY, this);
            com.getLine().handleTooltip(mouseX, mouseY, this);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            this.component[i].draw();
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(NameConst.i18n(NameConst.GUI_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        if (btn == 0) {
            if (focusedWidget != null) {
                focusedWidget.textField.setFocused(false);
            }
            for (Component com : this.component) {
                Widget textField = com.isMouseIn(xCoord, yCoord);
                if (textField != null) {
                    textField.textField.setFocused(true);
                    this.focusedWidget = textField;
                    super.mouseClicked(xCoord, yCoord, btn);
                    return;
                }
            }
            this.focusedWidget = null;
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (this.focusedWidget == null) {
            super.keyTyped(character, key);
            return;
        }
        if (!this.checkHotbarKeys(key)) {
            if (!((character == ' ') && this.focusedWidget.textField.getText().isEmpty())) {
                this.focusedWidget.textField.textboxKeyTyped(character, key);
            }
            super.keyTyped(character, key);

            this.focusedWidget.validate();

            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                this.component[this.focusedWidget.componentIndex].submit();
                this.focusedWidget.textField.setFocused(false);
                this.focusedWidget = null;
            }
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        for (Component com : this.component) {
            if (com.sendToServer(btn)) {
                return;
            }
        }
        super.actionPerformed(btn);
    }

    public void updateComponent(int index, long quantity, long batchSize, boolean isEnabled, LevelState state) {
        if (index < 0 || index >= TileLevelMaintainer.REQ_COUNT) return;
        component[index].setEnable(isEnabled);
        component[index].setState(state);
        component[index].getQty().textField.setText(String.valueOf(quantity));
        component[index].getBatch().textField.setText(String.valueOf(batchSize));
        component[index].getQty().validate();
        component[index].getBatch().validate();
    }

    public void updateComponent(int index, LevelState state) {
        if (index < 0 || index >= TileLevelMaintainer.REQ_COUNT) return;
        component[index].setState(state);
    }

    private class Component {

        public boolean isEnable = false;
        private final Widget qty;
        private final Widget batch;
        private final GuiFCImgButton disable;
        private final GuiFCImgButton enable;
        private final GuiFCImgButton submit;
        private final FCGuiLineField line;
        private LevelState state;
        private final ContainerLevelMaintainer container;

        public Component(Widget qtyInput, Widget batchInput, GuiFCImgButton submitBtn, GuiFCImgButton enableBtn,
                GuiFCImgButton disableBtn, FCGuiLineField line, List<GuiButton> buttonList,
                ContainerLevelMaintainer container) {
            this.qty = qtyInput;
            this.batch = batchInput;
            this.enable = enableBtn;
            this.disable = disableBtn;
            this.submit = submitBtn;
            this.line = line;
            this.state = LevelState.None;
            this.container = container;
            buttonList.add(this.submit);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
        }

        public int getIndex() {
            return this.qty.componentIndex;
        }

        public void setEnable(boolean enable) {
            this.isEnable = enable;
        }

        public IAEStack<?> getStack() {
            return this.container.getTile().getAEStackInventory().getAEStackInSlot(this.getIndex());
        }

        private void send(Widget widget) {
            if (this.getStack() != null && widget.getAmount() != null) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketLevelMaintainer(widget.action, widget.componentIndex, widget.getAmount()));
            }
        }

        public void submit() {
            this.sendToServer(this.submit);
        }

        protected boolean sendToServer(GuiButton btn) {
            boolean didSomething = false;
            if (this.submit == btn) {
                final Widget qty = this.getQty();
                final Widget batch = this.getBatch();
                qty.validate();
                batch.validate();
                if (qty.getAmount() != null) {
                    this.send(qty);
                    qty.textField.setText(String.valueOf(qty.getAmount()));
                }
                if (batch.getAmount() != null) {
                    this.send(batch);
                    batch.textField.setText(String.valueOf(batch.getAmount()));
                }

                didSomething = true;
            } else if (this.enable == btn) {
                this.setEnable(false);
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.Enable, this.getIndex()));
                didSomething = true;
            } else if (this.disable == btn) {
                if (this.getStack() != null) {
                    this.setEnable(true);
                    FluidCraft.proxy.netHandler
                            .sendToServer(new CPacketLevelMaintainer(Action.Disable, this.getIndex()));
                    didSomething = true;
                }
            }
            return didSomething;
        }

        public Widget isMouseIn(final int xCoord, final int yCoord) {
            if (this.qty.textField.isMouseIn(xCoord, yCoord)) return this.getQty();
            if (this.batch.textField.isMouseIn(xCoord, yCoord)) return this.getBatch();
            return null;
        }

        public Widget getQty() {
            return this.qty;
        }

        public Widget getBatch() {
            return this.batch;
        }

        public FCGuiLineField getLine() {
            return this.line;
        }

        public void draw() {
            this.qty.draw();
            this.batch.draw();
            ArrayList<String> message = new ArrayList<>();
            message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_TITLE) + "\n");
            switch (this.state) {
                case Idle -> {
                    this.line.setColor(FCGuiColors.StateIdle.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_IDLE));
                }
                case Craft -> {
                    this.line.setColor(FCGuiColors.StateCraft.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_LINK));
                }
                case Export -> {
                    this.line.setColor(FCGuiColors.StateExport.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT));
                }
                case Error -> {
                    this.line.setColor(FCGuiColors.StateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_ERROR));
                }
                case NotFound -> {
                    this.line.setColor(FCGuiColors.StateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_NOT_FOUND));
                }
                case CantCraft -> {
                    this.line.setColor(FCGuiColors.StateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CANT_CRAFT));
                }
                default -> {
                    this.line.setColor(FCGuiColors.StateNone.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_NONE));
                }
            }
            message.add("");
            if (isShiftKeyDown()) {
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_IDLE));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_IDLE_DESC) + "\n");
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_LINK));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_LINK_DESC) + "\n");
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT_DESC) + "\n");
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_ERROR));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_ERROR_DESC) + "\n");
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_NOT_FOUND));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_NOT_FOUND_DESC) + "\n");
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CANT_CRAFT));
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CANT_CRAFT_DESC));
            } else {
                message.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
            }
            this.line.setMessage(
                    render.wrapFormattedStringToWidth(String.join("\n", message), (int) Math.floor(xSize * 0.8)));
            this.line.drawTextBox();
            if (this.isEnable) {
                this.enable.visible = true;
                this.disable.visible = false;
            } else {
                this.enable.visible = false;
                this.disable.visible = true;
            }
        }

        public void setState(LevelState state) {
            this.state = state;
        }
    }

    private class Widget {

        public final int componentIndex;
        public final Action action;
        public final FCGuiTextField textField;
        private final String tooltip;
        private Long amount;

        public Widget(FCGuiTextField textField, String tooltip, int componentIndex, Action action) {
            this.textField = textField;
            this.textField.setEnableBackgroundDrawing(false);
            this.textField.setText("0");
            this.textField.setMaxStringLength(16); // this length is enough to be useful
            this.componentIndex = componentIndex;
            this.action = action;
            this.tooltip = tooltip;
        }

        public void draw() {
            String current = amount != null
                    ? StatCollector.translateToLocal(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                            + NumberFormat.getNumberInstance().format(amount)
                            + "\n"
                    : "";
            if (isShiftKeyDown()) {
                this.setTooltip(
                        render.wrapFormattedStringToWidth(
                                StatCollector.translateToLocal(this.tooltip) + "\n"
                                        + current
                                        + "\n"
                                        + StatCollector.translateToLocal(this.tooltip + ".hint"),
                                xSize / 2));
            } else {
                this.setTooltip(
                        render.wrapFormattedStringToWidth(
                                NameConst.i18n(this.tooltip, "\n", false) + "\n"
                                        + current
                                        + NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE),
                                (int) Math.floor(xSize * 0.8)));
            }
            this.textField.drawTextBox();
        }

        public void setTooltip(String message) {
            this.textField.setMessage(message);
        }

        public void validate() {
            final double result = Calculator.conversion(this.textField.getText());
            if (Double.isNaN(result) || result < 0) {
                this.amount = null;
                this.textField.setTextColor(0xFF0000);
            } else {
                this.amount = (long) ArithHelper.round(result, 0);
                this.textField.setTextColor(0xFFFFFF);
            }

            IAEStack<?> stack = component[this.componentIndex].getStack();
            if (stack != null) {
                Long amount = component[this.componentIndex].getQty().getAmount();
                stack.setStackSize(amount != null ? amount : 0);
            }
        }

        @Nullable
        public Long getAmount() {
            return this.amount;
        }
    }
}
