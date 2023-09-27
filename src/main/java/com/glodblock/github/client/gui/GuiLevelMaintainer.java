package com.glodblock.github.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.NameConst;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.render.AppEngRenderItem;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIDragClick;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cofh.core.render.CoFHFontRenderer;
import cpw.mods.fml.common.Optional;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiLevelMaintainer extends AEBaseGui implements INEIGuiHandler {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private final ContainerLevelMaintainer cont;
    private final Component[] component = new Component[TileLevelMaintainer.REQ_COUNT];
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private FCGuiTextField input;
    private int lastWorkingTick;
    private int refreshTick;
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
        this.refreshTick = 0;
    }

    public void postUpdate(List<IAEItemStack> list) {
        for (Slot slot : this.cont.getRequestSlots()) {
            if (!slot.getHasStack()) {
                component[slot.getSlotIndex()].setState(TileLevelMaintainer.State.Nothing);
            }
        }
        for (IAEItemStack is : list) {
            NBTTagCompound data = is.getItemStack().getTagCompound();
            long size = data.getLong("Batch");
            int i = data.getInteger("Index");
            boolean isEnable = data.getBoolean("Enable");
            TileLevelMaintainer.State state = TileLevelMaintainer.State.values()[data.getInteger("State")];
            component[i].getQty().textField.setText(String.valueOf(is.getStackSize()));
            component[i].getBatch().textField.setText(String.valueOf(size));
            component[i].setEnable(isEnable);
            component[i].setState(state);
        }
    }

    public void initGui() {
        super.initGui();
        this.lastWorkingTick = this.refreshTick;
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            component[i] = new Component(
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 46, guiTop + 19 + 19 * i, 52, 14),
                            NameConst.TT_LEVEL_MAINTAINER_REQUEST_SIZE,
                            i,
                            "TileLevelMaintainer.Quantity"),
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 100, guiTop + 19 + 19 * i, 52, 14),
                            NameConst.TT_LEVEL_MAINTAINER_BATCH_SIZE,
                            i,
                            "TileLevelMaintainer.Batch"),
                    new GuiFCImgButton(guiLeft + 105 + 47, guiTop + 17 + 19 * i, "SUBMIT", "SUBMIT", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "ENABLE", "ENABLE", false),
                    new GuiFCImgButton(guiLeft + 9, guiTop + 20 + 19 * i, "DISABLE", "DISABLE", false),
                    new FCGuiLineField(fontRendererObj, guiLeft + 47, guiTop + 33 + 19 * i, 125),
                    this.buttonList);
        }
        FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer("TileLevelMaintainer.refresh"));
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
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer("TileLevelMaintainer.refresh"));
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
            stackSizeRenderer.setAeStack(fake);
            stackSizeRenderer.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    stack.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
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
            if (!this.input.getText().matches("^[0-9]+")) {
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
        super.actionPerformed(btn);
        for (Component com : this.component) {
            if (com.sendToServer(btn)) {
                break;
            }
        }
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
        for (SlotFluidConvertingFake slot : this.cont.getRequestSlots()) {
            if (getSlotArea(slot).contains(mouseX, mouseY)) {
                slot.putStack(draggedStack);
                NetworkHandler.instance.sendToServer(new PacketNEIDragClick(draggedStack, slot.getSlotIndex()));
                if (draggedStack != null) {
                    this.updateAmount(slot.getSlotIndex(), draggedStack.stackSize);
                    draggedStack.stackSize = 0;
                }
                return true;
            }
        }
        if (draggedStack != null) {
            draggedStack.stackSize = 0;
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
        private TileLevelMaintainer.State state;

        public Component(Widget qtyInput, Widget batchInput, GuiFCImgButton submitBtn, GuiFCImgButton enableBtn,
                GuiFCImgButton disableBtn, FCGuiLineField line, List buttonList) {
            this.qty = qtyInput;
            this.batch = batchInput;
            this.enable = enableBtn;
            this.disable = disableBtn;
            this.submit = submitBtn;
            this.line = line;
            this.state = TileLevelMaintainer.State.Nothing;
            buttonList.add(this.submit);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
        }

        public int getIndex() {
            return this.getQty().idx;
        }

        public void setEnable(boolean enable) {
            this.isEnable = enable;
        }

        private boolean send(Widget widget) {
            if (((SlotFluidConvertingFake) cont.inventorySlots.get(widget.idx)).getHasStack()) {
                if (!widget.textField.getText().isEmpty() && widget.textField.getText().matches("^[0-9]+")) {
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
                FluidCraft.proxy.netHandler
                        .sendToServer(new CPacketLevelMaintainer("TileLevelMaintainer.Enable", this.getIndex()));
                didSomething = true;
            } else if (this.disable == btn) {
                FluidCraft.proxy.netHandler
                        .sendToServer(new CPacketLevelMaintainer("TileLevelMaintainer.Disable", this.getIndex()));
                didSomething = true;
            }
            return didSomething;
        }

        public FCGuiTextField isMouseIn(final int xCoord, final int yCoord) {
            if (this.getQty().textField.isMouseIn(xCoord, yCoord)) return this.getQty().textField;
            if (this.getBatch().textField.isMouseIn(xCoord, yCoord)) return this.getBatch().textField;
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
            this.getQty().draw();
            this.getBatch().draw();
            ArrayList<String> message = new ArrayList<>();
            message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_TITLE) + "\n");
            switch (this.state) {
                case Idling -> {
                    this.line.setColor(0xFF55FF55);
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT)
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_IDLE));
                }
                case Crafting -> {
                    this.line.setColor(0xFFFFFF55);
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT)
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_LINK));
                }
                case Exporting -> {
                    this.line.setColor(0xFFAA00AA);
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT)
                                    + NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT));
                }
                default -> {
                    this.line.setColor(0);
                    message.add(
                            NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_CURRENT)
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
                message.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_EXPORT_DESC));
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

        public void setState(TileLevelMaintainer.State state) {
            this.state = state;
        }
    }

    private class Widget {

        public final int idx;
        public final String action;
        private final FCGuiTextField textField;
        private final String tooltip;

        public Widget(FCGuiTextField textField, String tooltip, int idx, String action) {
            this.textField = textField;
            this.textField.setEnableBackgroundDrawing(false);
            this.textField.setText("0");
            this.textField.setMaxStringLength(10); // this length is enough to useful
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
