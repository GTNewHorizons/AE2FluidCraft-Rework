package com.glodblock.github.client.gui;

import static com.glodblock.github.client.gui.container.ContainerLevelMaintainer.createLevelValues;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer.State;
import com.glodblock.github.common.tile.TileLevelMaintainer.TLMTags;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.network.CPacketLevelMaintainer.Action;
import com.glodblock.github.network.CPacketLevelTerminalCommands;
import com.glodblock.github.util.FCGuiColors;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import com.gtnewhorizon.gtnhlib.util.parsing.MathExpressionParser;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIDragClick;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cofh.core.render.CoFHFontRenderer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiLevelMaintainer extends AEBaseGui implements INEIGuiHandler {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private final ContainerLevelMaintainer cont;
    private final Component[] component = new Component[TileLevelMaintainer.REQ_COUNT];
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private FCGuiTextField input;
    private int lastWorkingTick;
    private int refreshTick;
    private final CoFHFontRenderer render;
    protected ItemStack icon = null;

    protected GuiType originalGui;
    protected Util.DimensionalCoordSide originalBlockPos;
    protected GuiTabButton originalGuiBtn;

    protected static final boolean isGTNHLibLoaded = Loader.isModLoaded("gtnhlib");
    protected static final Pattern numberPattern = isGTNHLibLoaded ? MathExpressionParser.EXPRESSION_PATTERN
            : Pattern.compile("^[0-9]+");

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
        this.refreshTick = 0;
        if (ipl.player.openContainer instanceof AEBaseContainer container) {
            var target = container.getTarget();
            if (target instanceof PartLevelTerminal terminal) {
                icon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
                originalGui = GuiType.LEVEL_TERMINAL;
                DimensionalCoord blockPos = new DimensionalCoord(terminal.getTile());
                originalBlockPos = new Util.DimensionalCoordSide(
                        blockPos.x,
                        blockPos.y,
                        blockPos.z,
                        blockPos.getDimension(),
                        terminal.getSide(),
                        "");
            } else if (target instanceof IWirelessTerminal terminal && terminal.isUniversal(target)) {
                icon = ItemAndBlockHolder.WIRELESS_ULTRA_TERM.stack();
                originalGui = ItemWirelessUltraTerminal.readMode(terminal.getItemStack());
                originalBlockPos = new Util.DimensionalCoordSide(
                        terminal.getInventorySlot(),
                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                        0,
                        ipl.player.worldObj.provider.dimensionId,
                        ForgeDirection.UNKNOWN,
                        "");
            } else if (target instanceof WirelessLevelTerminalInventory terminal) {
                icon = ItemAndBlockHolder.LEVEL_TERMINAL.stack();
                originalGui = GuiType.WIRELESS_LEVEL_TERMINAL;
                originalBlockPos = new Util.DimensionalCoordSide(
                        terminal.getInventorySlot(),
                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                        0,
                        ipl.player.worldObj.provider.dimensionId,
                        ForgeDirection.UNKNOWN,
                        "");

            }
        }

    }

    public void postUpdate(List<IAEItemStack> list) {
        for (Slot slot : this.cont.getRequestSlots()) {
            if (!slot.getHasStack()) {
                component[slot.getSlotIndex()].setState(State.None);
            }
        }
        for (IAEItemStack is : list) {
            NBTTagCompound data = is.getItemStack().getTagCompound();
            if (data == null) {
                if (AEConfig.instance.isFeatureEnabled(AEFeature.PacketLogging)) {
                    AELog.info("Received empty configuration: ", is);
                }
                continue;
            }
            long batch = data.getLong(TLMTags.Batch.tagName);
            long quantity = data.getLong(TLMTags.Quantity.tagName);
            int idx = data.getInteger(TLMTags.Index.tagName);
            boolean isEnable = data.getBoolean(TLMTags.Enable.tagName);
            State state = State.values()[data.getInteger(TLMTags.State.tagName)];
            component[idx].getQty().textField.setText(String.valueOf(quantity));
            component[idx].getBatch().textField.setText(String.valueOf(batch));
            component[idx].setEnable(isEnable);
            component[idx].setState(state);
        }
    }

    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.lastWorkingTick = this.refreshTick;
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
                    new FCGuiLineField(fontRendererObj, guiLeft + 47, guiTop + 33 + 19 * i, 125),
                    this.buttonList);
        }
        if (this.icon != null) {
            this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 151,
                    this.guiTop - 4,
                    this.icon,
                    this.icon.getDisplayName(),
                    itemRender);
            this.originalGuiBtn.setHideEdge(13);
            this.buttonList.add(originalGuiBtn);
        }
        FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.Refresh));
    }

    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        this.refreshTick++;
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
        int tick = this.refreshTick;
        int interval = 20;
        if (tick > lastWorkingTick + interval && this.input == null) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.Refresh));
            lastWorkingTick = this.refreshTick;
        }
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
    public void func_146977_a(final Slot s) {
        if (drawSlot0(s)) super.func_146977_a(s);
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotFake) {
            IAEItemStack stack = ((SlotFluidConvertingFake) slot).getAeStack();
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return true;
            IAEItemStack fake = stack.copy();
            if (this.component[slot.getSlotIndex()].getQty().textField.getText().matches("[0-9]+")) {
                fake.setStackSize(Long.parseLong(this.component[slot.getSlotIndex()].getQty().textField.getText()));
            } else {
                fake.setStackSize(0);
            }
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            aeRenderItem.setAeStack(fake);
            aeRenderItem.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    fake.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
            return false;
        }
        return true;
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        if (btn == 0) {
            if (input != null) {
                input.setFocused(false);
            }
            for (Component com : this.component) {
                FCGuiTextField textField = com.isMouseIn(xCoord, yCoord);
                if (textField != null) {
                    textField.setFocused(true);
                    this.input = textField;
                    super.mouseClicked(xCoord, yCoord, btn);
                    return;
                }
            }
            this.input = null;
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (this.input == null) {
            super.keyTyped(character, key);
            return;
        }
        if (!this.checkHotbarKeys(key)) {
            if (!((character == ' ') && this.input.getText().isEmpty())) {
                this.input.textboxKeyTyped(character, key);
            }
            super.keyTyped(character, key);
            if (!numberPattern.matcher(this.input.getText()).matches()) {
                this.input.setTextColor(0xFF0000);
            } else {
                this.input.setTextColor(0xFFFFFF);
            }
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                this.input.setFocused(false);
                this.input = null;
            }
        }
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (slot instanceof SlotFluidConvertingFake && this.cont.getPlayerInv().getItemStack() != null) {
            this.component[slot.getSlotIndex()].getQty().textField
                    .setText(String.valueOf(this.cont.getPlayerInv().getItemStack().stackSize));
            return;
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    public void updateAmount(int idx, int stackSize) {
        this.component[idx].getQty().textField.setText(String.valueOf(stackSize));
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == originalGuiBtn) {
            switchGui();
        } else {
            super.actionPerformed(btn);
            for (Component com : this.component) {
                if (com.sendToServer(btn)) {
                    break;
                }
            }
        }
    }

    public void switchGui() {
        CPacketLevelTerminalCommands message = new CPacketLevelTerminalCommands(
                CPacketLevelTerminalCommands.Action.BACK,
                originalBlockPos.x,
                originalBlockPos.y,
                originalBlockPos.z,
                originalBlockPos.getDimension(),
                originalBlockPos.getSide());
        if (originalGui != null) {
            message.setOriginalGui(originalGui.ordinal());
        }
        FluidCraft.proxy.netHandler.sendToServer(message);
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    private Rectangle getSlotArea(SlotFake slot) {
        return new Rectangle(guiLeft + slot.getX(), guiTop + slot.getY(), 16, 16);
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        if (draggedStack != null) {
            draggedStack.stackSize = 0;
        }
        for (int i = 0; i < this.cont.getRequestSlots().length; i++) {
            SlotFluidConvertingFake slot = this.cont.getRequestSlots()[i];
            if (getSlotArea(slot).contains(mouseX, mouseY) && draggedStack != null) {
                ItemStack itemStack = createLevelValues(draggedStack.copy());
                itemStack.getTagCompound().setInteger(TLMTags.Index.tagName, i);
                slot.putStack(itemStack);
                NetworkHandler.instance.sendToServer(new PacketNEIDragClick(itemStack, slot.getSlotIndex()));
                this.updateAmount(slot.getSlotIndex(), itemStack.stackSize);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return false;
    }

    private class Component {

        public boolean isEnable = true;
        private final Widget qty;
        private final Widget batch;
        private final GuiFCImgButton disable;
        private final GuiFCImgButton enable;
        private final GuiFCImgButton submit;
        private final FCGuiLineField line;
        private State state;

        @SuppressWarnings("unchecked")
        public Component(Widget qtyInput, Widget batchInput, GuiFCImgButton submitBtn, GuiFCImgButton enableBtn,
                GuiFCImgButton disableBtn, FCGuiLineField line, List buttonList) {
            this.qty = qtyInput;
            this.batch = batchInput;
            this.enable = enableBtn;
            this.disable = disableBtn;
            this.submit = submitBtn;
            this.line = line;
            this.state = State.None;
            buttonList.add(this.submit);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
        }

        public int getIndex() {
            return this.qty.idx;
        }

        public void setEnable(boolean enable) {
            this.isEnable = enable;
        }

        private boolean send(Widget widget) {
            if (((SlotFluidConvertingFake) cont.inventorySlots.get(widget.idx)).getHasStack()) {
                if (isGTNHLibLoaded) {
                    long value = (long) MathExpressionParser.parse(widget.textField.getText());
                    widget.textField.setText(String.valueOf(value));
                }
                if (!widget.textField.getText().isEmpty()
                        && numberPattern.matcher(widget.textField.getText()).matches()) {
                    String str = widget.textField.getText().replaceAll("^(0+)", "");
                    widget.textField.setText(str.isEmpty() ? "0" : str);
                    FluidCraft.proxy.netHandler.sendToServer(
                            new CPacketLevelMaintainer(widget.action, widget.idx, widget.textField.getText()));
                    widget.textField.setTextColor(0xFFFFFF);
                    return true;
                } else {
                    widget.textField.setTextColor(0xFF0000);
                    return false;
                }
            }
            return false;
        }

        protected boolean sendToServer(GuiButton btn) {
            boolean didSomething = false;
            if (this.submit == btn) {
                if (this.send(this.getQty())) this.send(this.getBatch());
                didSomething = true;
            } else if (this.enable == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.Enable, this.getIndex()));
                didSomething = true;
            } else if (this.disable == btn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(Action.Disable, this.getIndex()));
                didSomething = true;
            }
            return didSomething;
        }

        public FCGuiTextField isMouseIn(final int xCoord, final int yCoord) {
            if (this.qty.textField.isMouseIn(xCoord, yCoord)) return this.getQty().textField;
            if (this.batch.textField.isMouseIn(xCoord, yCoord)) return this.getBatch().textField;
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
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_ERROR_DESC));
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

        public void setState(State state) {
            this.state = state;
        }
    }

    private class Widget {

        public final int idx;
        public final Action action;
        private final FCGuiTextField textField;
        private final String tooltip;

        public Widget(FCGuiTextField textField, String tooltip, int idx, Action action) {
            this.textField = textField;
            this.textField.setEnableBackgroundDrawing(false);
            this.textField.setText("0");
            this.textField.setMaxStringLength(16); // this length is enough to be useful
            this.idx = idx;
            this.action = action;
            this.tooltip = tooltip;
        }

        public void draw() {
            if (isShiftKeyDown()) {
                this.setTooltip(render.wrapFormattedStringToWidth(NameConst.i18n(this.tooltip), xSize / 2));
            } else {
                this.setTooltip(
                        render.wrapFormattedStringToWidth(
                                NameConst.i18n(this.tooltip, "\n", false) + "\n"
                                        + NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE),
                                (int) Math.floor(xSize * 0.8)));
            }
            this.textField.drawTextBox();
        }

        public void setTooltip(String message) {
            this.textField.setMessage(message);
        }
    }
}
