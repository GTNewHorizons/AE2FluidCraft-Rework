package com.glodblock.github.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.network.CPacketLevelMaintainer.Action;
import com.glodblock.github.util.FCGuiColors;
import com.glodblock.github.util.NameConst;

import appeng.api.features.LevelState;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.client.gui.GuiSub;
import appeng.client.gui.slots.VirtualMEPhantomSlot;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.core.localization.GuiText;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiLevelMaintainer extends GuiSub {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private static final int MAX_TICK_VALUE = 9999;
    private static final int PANEL_EDGE = 164;
    private static final int PANEL_BODY = 165;
    private static final int PANEL_BORDER = 166;
    private static final int PANEL_INTERIOR = 152;
    private static final int PANEL_TAIL = 168;
    private static final int PANEL_TAIL_W = 8;
    private static final int PANEL_FILL = 171;
    private static final int PANEL_SPLIT = 129;
    private static final int INV_X = 7;
    private static final int INV_W = 162;
    private static final int TICK_EXT = ContainerLevelMaintainer.PANEL_WIDENING;
    private static final int INV_SHIFT = ContainerLevelMaintainer.PLAYER_INV_OFFSET_X;
    private static final int TICK_FIELD_X = 154;
    private static final int TICK_FIELD_Y = 19;
    private static final int TICK_FIELD_W = 36;
    private static final int TICK_FIELD_H = 14;
    private static final int TICK_CELL_H = 12;
    private static final int SUBMIT_X = 190;
    private static final int SUBMIT_CELL_X = SUBMIT_X + 2;
    private static final int SUBMIT_CELL_W = 12;
    private final ContainerLevelMaintainer cont;
    private final Component[] component = new Component[TileLevelMaintainer.REQ_COUNT];
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private Widget focusedWidget;
    private Component editing;
    private final FontRenderer render;
    private GuiToggleButton liteMode;

    public GuiLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(new ContainerLevelMaintainer(ipl, tile));
        this.cont = (ContainerLevelMaintainer) inventorySlots;
        this.xSize = PANEL_TAIL + TICK_EXT + PANEL_TAIL_W;
        this.ySize = 214;
        this.render = new FontRenderer(
                Minecraft.getMinecraft().gameSettings,
                TEX_BG,
                Minecraft.getMinecraft().getTextureManager(),
                true);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.editing = null;
        this.focusedWidget = null;

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            VirtualMEPhantomSlot slot = new VirtualMEPhantomSlot(
                    27,
                    20 + i * 19,
                    this.cont.getTile().getAEStackInventory(),
                    i,
                    GuiLevelMaintainer::acceptType);
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
                    new GuiFCImgButton(guiLeft + SUBMIT_X, guiTop + 17 + 19 * i, "SUBMIT", "SUBMIT", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "ENABLE", "ENABLE", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "DISABLE", "DISABLE", false),
                    new FCGuiTextField(
                            this.fontRendererObj,
                            guiLeft + TICK_FIELD_X,
                            guiTop + TICK_FIELD_Y + 19 * i,
                            TICK_FIELD_W,
                            TICK_FIELD_H),
                    new FCGuiLineField(
                            fontRendererObj,
                            guiLeft + 47,
                            guiTop + 33 + 19 * i,
                            120 + ContainerLevelMaintainer.PANEL_WIDENING),
                    this.buttonList,
                    this.cont);
        }
        this.buttonList.add(
                this.liteMode = new GuiToggleButton(
                        guiLeft - 18,
                        guiTop + 2,
                        178,
                        194,
                        GuiText.CraftingModeLite.getLocal(),
                        NameConst.TT_LEVEL_MAINTAINER_LITE_CRAFT_DESC));
        this.liteMode.setState(false);
    }

    @Override
    public void initPrimaryGuiButton() {
        this.originalGuiBtn = new GuiTabButton(
                this.guiLeft + this.xSize - 25,
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
            com.getTickField().handleTooltip(mouseX, mouseY, this);
            com.getLine().handleTooltip(mouseX, mouseY, this);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        final int invH = ySize - PANEL_SPLIT;

        drawTexturedModalRect(offsetX, offsetY, 0, 0, PANEL_EDGE + 1, PANEL_SPLIT);
        drawTexturedModalRect(offsetX, offsetY + PANEL_SPLIT, 0, PANEL_SPLIT, INV_X, invH);

        for (int i = 0; i < TICK_EXT + 2; i++) {
            drawTexturedModalRect(offsetX + PANEL_BODY + i, offsetY, PANEL_INTERIOR, 0, 1, PANEL_SPLIT);
            drawTexturedModalRect(offsetX + PANEL_BODY + i, offsetY + PANEL_SPLIT, PANEL_FILL, PANEL_SPLIT, 1, invH);
        }
        for (int i = 0; i < INV_SHIFT; i++) {
            drawTexturedModalRect(offsetX + INV_X + i, offsetY + PANEL_SPLIT, PANEL_FILL, PANEL_SPLIT, 1, invH);
        }

        drawTexturedModalRect(offsetX + PANEL_TAIL + TICK_EXT - 2, offsetY, PANEL_BORDER, 0, 2, PANEL_SPLIT);

        drawTexturedModalRect(offsetX + PANEL_TAIL + TICK_EXT, offsetY, PANEL_TAIL, 0, PANEL_TAIL_W, ySize);
        drawTexturedModalRect(offsetX + PANEL_TAIL + TICK_EXT, offsetY + PANEL_SPLIT, PANEL_FILL, PANEL_SPLIT, 1, invH);

        drawTexturedModalRect(offsetX + INV_X + INV_SHIFT, offsetY + PANEL_SPLIT, INV_X, PANEL_SPLIT, INV_W, invH);

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            this.component[i].draw();
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(
                getGuiDisplayName(NameConst.i18n(NameConst.GUI_LEVEL_MAINTAINER)),
                8,
                6,
                FCGuiColors.guiTextColorGray.getColor());
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        if (this.editing != null) {
            if (this.editing.isOverTickField(xCoord, yCoord)) {
                if (btn == 0) {
                    this.editing.getTickField().mouseClicked(xCoord, yCoord, btn);
                }
                return;
            }
            this.endTickEdit(true);
        }
        if (btn == 0) {
            for (Component com : this.component) {
                if (com.isOverTickField(xCoord, yCoord)) {
                    this.beginTickEdit(com);
                    com.getTickField().mouseClicked(xCoord, yCoord, btn);
                    return;
                }
                Widget textField = com.isMouseIn(xCoord, yCoord);
                if (textField != null) {
                    this.focusWidget(textField);
                    super.mouseClicked(xCoord, yCoord, btn);
                    return;
                }
            }
            this.focusWidget(null);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (this.editing != null) {
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                this.endTickEdit(true);
            } else if (key == Keyboard.KEY_ESCAPE) {
                this.endTickEdit(false);
            } else if (character < ' ' || Character.isDigit(character)) {
                final FCGuiTextField field = this.editing.getTickField();
                if (field != null) {
                    field.textboxKeyTyped(character, key);
                }
            }
            return;
        }
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
                this.focusWidget(null);
            }

            if (key == Keyboard.KEY_TAB) {
                this.focusAdjacentWidget(isShiftKeyDown());
            }
        }
    }

    private void focusWidget(@Nullable Widget widget) {
        if (this.focusedWidget != null) {
            this.focusedWidget.textField.setFocused(false);
        }
        this.focusedWidget = widget;
        if (this.focusedWidget != null) {
            this.focusedWidget.textField.setFocused(true);
        }
    }

    private void focusAdjacentWidget(boolean backwards) {
        this.focusedWidget.validate();
        this.focusWidget(this.getAdjacentWidget(backwards));
    }

    private Widget getAdjacentWidget(boolean backwards) {
        int index = this.focusedWidget.componentIndex;
        boolean isBatch = this.focusedWidget.action == Action.Batch;

        if (backwards) {
            if (isBatch) {
                return this.component[index].getQty();
            }
            return this.component[(index + TileLevelMaintainer.REQ_COUNT - 1) % TileLevelMaintainer.REQ_COUNT]
                    .getBatch();
        }

        if (isBatch) {
            return this.component[(index + 1) % TileLevelMaintainer.REQ_COUNT].getQty();
        }
        return this.component[index].getBatch();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        for (Component com : this.component) {
            if (com.sendToServer(btn)) {
                return;
            }
        }
        if (btn == this.liteMode) {
            if (isShiftKeyDown()) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.ClearLiteMode));
            } else {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.ToggleLiteMode));
            }
            return;
        }
        super.actionPerformed(btn);
    }

    private void beginTickEdit(final Component com) {
        this.endTickEdit(true);
        this.focusWidget(null);
        this.editing = com;
        com.beginEdit();
    }

    private void endTickEdit(final boolean commit) {
        if (this.editing == null) {
            return;
        }
        final Component com = this.editing;
        this.editing = null;
        if (commit) {
            com.commitEdit();
        } else {
            com.cancelEdit();
        }
    }

    public void updateComponent(int index, long quantity, long batchSize, boolean isEnabled, LevelState state,
            int minTick, int maxTick) {
        if (index < 0 || index >= TileLevelMaintainer.REQ_COUNT) return;
        component[index].setEnable(isEnabled);
        component[index].setState(state);
        component[index].setTicks(minTick, maxTick);
        component[index].getQty().textField.setText(String.valueOf(quantity));
        component[index].getBatch().textField.setText(String.valueOf(batchSize));
        component[index].getQty().validate();
        component[index].getBatch().validate();
    }

    public void updateComponent(int index, LevelState state) {
        if (index < 0 || index >= TileLevelMaintainer.REQ_COUNT) return;
        component[index].setState(state);
    }

    public void updateComponent(boolean isLiteMode) {
        this.liteMode.setState(isLiteMode);
    }

    private static boolean acceptType(VirtualMEPhantomSlot slot, IAEStackType<?> type, int mouseButton) {
        return true;
    }

    private static long parseTick(final String text) {
        if (text == null) {
            return -1;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void setupTickField(final FCGuiTextField field) {
        field.setMaxStringLength(4);
        field.setEnableBackgroundDrawing(false);
        field.setTextColor(FCGuiColors.guiTextColorInput.getColor());
        field.setVisible(true);
    }

    private static void blurTickField(final FCGuiTextField field) {
        field.setFocused(false);
    }

    private class Component {

        public boolean isEnable = false;
        private final Widget qty;
        private final Widget batch;
        private final GuiFCImgButton disable;
        private final GuiFCImgButton enable;
        private final GuiFCImgButton submit;
        private final FCGuiTextField tickField;
        private final FCGuiLineField line;
        private LevelState state;
        private int minTick = Config.levelMaintainerMinTicks;
        private int maxTick = Config.levelMaintainerMaxTicks;
        private boolean editing;
        private final ContainerLevelMaintainer container;

        public Component(Widget qtyInput, Widget batchInput, GuiFCImgButton submitBtn, GuiFCImgButton enableBtn,
                GuiFCImgButton disableBtn, FCGuiTextField tickField, FCGuiLineField line, List<GuiButton> buttonList,
                ContainerLevelMaintainer container) {
            this.qty = qtyInput;
            this.batch = batchInput;
            this.enable = enableBtn;
            this.disable = disableBtn;
            this.submit = submitBtn;
            this.tickField = tickField;
            this.line = line;
            this.state = LevelState.None;
            this.container = container;
            setupTickField(this.tickField);
            buttonList.add(this.submit);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
            this.applyTicks();
        }

        public int getIndex() {
            return this.qty.componentIndex;
        }

        public void setEnable(boolean enable) {
            this.isEnable = enable;
        }

        public void setTicks(int minTick, int maxTick) {
            this.minTick = Math.max(1, minTick);
            this.maxTick = Math.max(1, Math.max(minTick, maxTick));
            this.applyTicks();
        }

        private void applyTicks() {
            if (!this.editing) {
                this.tickField.setText(String.valueOf(this.maxTick));
            }
        }

        public FCGuiTextField getTickField() {
            return this.tickField;
        }

        public boolean isOverTickField(final int x, final int y) {
            return this.tickField.isMouseIn(x, y);
        }

        public void beginEdit() {
            this.editing = true;
            this.tickField.setText(String.valueOf(this.maxTick));
            this.tickField.setCursorPositionEnd();
            this.tickField.setFocused(true);
        }

        public void cancelEdit() {
            this.editing = false;
            blurTickField(this.tickField);
            this.applyTicks();
        }

        public void commitEdit() {
            final long value = parseTick(this.tickField.getText());
            this.editing = false;
            blurTickField(this.tickField);
            if (value > 0) {
                this.applyTick(value);
            }
            this.applyTicks();
        }

        private void applyTick(final long value) {
            if (this.getStack() == null) return;
            final int v = (int) Math.max(1, Math.min(MAX_TICK_VALUE, value));
            this.setTicks(Math.min(this.minTick, v), v);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.SetMaxTick, this.getIndex(), v));
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
            final boolean hasStack = this.getStack() != null;
            if (!hasStack && (this.minTick != Config.levelMaintainerMinTicks
                    || this.maxTick != Config.levelMaintainerMaxTicks)) {
                this.setTicks(Config.levelMaintainerMinTicks, Config.levelMaintainerMaxTicks);
            }

            final int cellY = guiTop + TICK_FIELD_Y + 19 * this.getIndex();
            GuiFCImgButton.drawCell(guiLeft + TICK_FIELD_X, cellY, TICK_FIELD_W, TICK_CELL_H);
            GuiFCImgButton.drawCell(guiLeft + SUBMIT_CELL_X, cellY, SUBMIT_CELL_W, TICK_CELL_H);
            this.tickField.setVisible(true);
            if (this.editing) {
                this.tickField.setTextColor(
                        parseTick(this.tickField.getText()) <= 0 ? FCGuiColors.guiLevelMaintainerError.getColor()
                                : FCGuiColors.guiTextColorInput.getColor());
            } else {
                this.tickField.setTextColor(FCGuiColors.guiTextColorInput.getColor());
            }
            final String tickTitle = NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_TICK_MAX, "\n", false);
            final String tickBody = this.editing ? NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_TICK_EDIT)
                    : StatCollector.translateToLocal(NameConst.TT_LEVEL_MAINTAINER_TICK_MAX + ".hint");
            this.tickField.setMessage(
                    render.wrapFormattedStringToWidth(tickTitle + "\n" + tickBody, (int) Math.floor(xSize * 0.8)));
            this.tickField.drawTextBox();
            this.qty.draw();
            this.batch.draw();
            ArrayList<String> message = new ArrayList<>();
            message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_TITLE) + "\n");
            switch (this.state) {
                case Idle -> {
                    this.line.setColor(FCGuiColors.stateIdle.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_IDLE));
                }
                case Craft -> {
                    this.line.setColor(FCGuiColors.stateCraft.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_LINK));
                }
                case Export -> {
                    this.line.setColor(FCGuiColors.stateExport.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT));
                }
                case Error -> {
                    this.line.setColor(FCGuiColors.stateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_ERROR));
                }
                case NotFound -> {
                    this.line.setColor(FCGuiColors.stateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_NOT_FOUND));
                }
                case CantCraft -> {
                    this.line.setColor(FCGuiColors.stateError.getColor());
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT) + " "
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CANT_CRAFT));
                }
                default -> {
                    this.line.setColor(FCGuiColors.stateNone.getColor());
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
            this.textField.setTextColor(FCGuiColors.guiTextColorInput.getColor());
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
                this.textField.setTextColor(FCGuiColors.guiLevelMaintainerError.getColor());
            } else {
                this.amount = (long) ArithHelper.round(result, 0);
                this.textField.setTextColor(FCGuiColors.guiTextColorInput.getColor());
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
