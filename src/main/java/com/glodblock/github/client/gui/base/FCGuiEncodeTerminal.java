package com.glodblock.github.client.gui.base;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.client.gui.GuiItemMonitor;
import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFake;
import appeng.util.item.AEItemStack;

public abstract class FCGuiEncodeTerminal extends GuiItemMonitor {

    private static final String SUBSTITUTION_DISABLE = "0";
    private static final String SUBSTITUTION_ENABLE = "1";
    private static final String PRIORITY_DISABLE = "0";
    private static final String PRIORITY_ENABLE = "1";
    private static final String MODE_CRAFTING = "1";
    private static final String MODE_PROCESSING = "0";
    public FCContainerEncodeTerminal container;
    protected GuiTabButton tabCraftButton;
    protected GuiTabButton tabProcessButton;
    protected GuiImgButton substitutionsEnabledBtn;
    protected GuiImgButton substitutionsDisabledBtn;
    protected GuiFCImgButton fluidPrioritizedEnabledBtn;
    protected GuiFCImgButton fluidPrioritizedDisabledBtn;
    protected GuiImgButton encodeBtn;
    protected GuiImgButton invertBtn;
    protected GuiImgButton clearBtn;
    protected GuiImgButton doubleBtn;
    protected GuiImgButton beSubstitutionsEnabledBtn;
    protected GuiImgButton beSubstitutionsDisabledBtn;
    protected GuiFCImgButton combineEnableBtn;
    protected GuiFCImgButton combineDisableBtn;
    protected GuiFCImgButton autoFillPatternEnableBtn;
    protected GuiFCImgButton autoFillPatternDisableBtn;
    protected final GuiScrollbar processingScrollBar = new GuiScrollbar();

    public FCGuiEncodeTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te,
            final FCContainerEncodeTerminal c) {
        super(inventoryPlayer, te, c);
        this.container = (FCContainerEncodeTerminal) this.inventorySlots;
        c.setGui(this);
        setReservedSpace(81);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(
                this.autoFillPatternEnableBtn = new GuiFCImgButton(
                        this.guiLeft - 18,
                        this.offsetY,
                        "FILL_PATTERN",
                        "DO_FILL"));
        this.buttonList.add(
                this.autoFillPatternDisableBtn = new GuiFCImgButton(
                        this.guiLeft - 18,
                        this.offsetY,
                        "NOT_FILL_PATTERN",
                        "DONT_FILL"));
        this.offsetY += 20;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        } else if (this.tabCraftButton == btn || this.tabProcessButton == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.CraftMode",
                            this.tabProcessButton == btn ? MODE_CRAFTING : MODE_PROCESSING));
        } else if (this.encodeBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.Encode",
                            (isCtrlKeyDown() ? 1 : 0) << 1 | (isShiftKeyDown() ? 1 : 0)));
        } else if (this.clearBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("PatternTerminal.Clear", "1"));
        } else if (this.substitutionsEnabledBtn == btn || this.substitutionsDisabledBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.Substitute",
                            this.substitutionsEnabledBtn == btn ? SUBSTITUTION_DISABLE : SUBSTITUTION_ENABLE));
        } else if (this.fluidPrioritizedEnabledBtn == btn || this.fluidPrioritizedDisabledBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.Prioritize",
                            isShiftKeyDown() ? "2" : (container.prioritize ? PRIORITY_DISABLE : PRIORITY_ENABLE)));
        } else if (this.invertBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns("PatternTerminal.Invert", container.inverted ? "0" : "1"));
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.Combine",
                            this.combineDisableBtn == btn ? "1" : "0"));
        } else if (ModAndClassUtil.isDoubleButton && doubleBtn == btn) {
            final int eventButton = Mouse.getEventButton();
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.Double",
                            (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? "1" : "0") + (eventButton == 1 ? "1" : "0")));
        } else if (ModAndClassUtil.isBeSubstitutionsButton && beSubstitutionsDisabledBtn == btn) {
            FluidCraft.proxy.netHandler
                    .sendToServer(new CPacketFluidPatternTermBtns("PatternTerminal.beSubstitute", "1"));
        } else if (ModAndClassUtil.isBeSubstitutionsButton && beSubstitutionsEnabledBtn == btn) {
            FluidCraft.proxy.netHandler
                    .sendToServer(new CPacketFluidPatternTermBtns("PatternTerminal.beSubstitute", "0"));
        } else if (this.autoFillPatternDisableBtn == btn || this.autoFillPatternEnableBtn == btn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            "PatternTerminal.AutoFillerPattern",
                            this.autoFillPatternDisableBtn == btn ? "1" : "0"));
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        updateButton(this.tabCraftButton, this.container.isCraftingMode());
        updateButton(this.tabProcessButton, !this.container.isCraftingMode());
        updateButton(this.doubleBtn, !this.container.isCraftingMode());
        updateButton(this.substitutionsEnabledBtn, this.container.substitute);
        updateButton(this.substitutionsDisabledBtn, !this.container.substitute);
        updateButton(this.combineEnableBtn, !this.container.isCraftingMode() && this.container.combine);
        updateButton(this.combineDisableBtn, !this.container.isCraftingMode() && !this.container.combine);
        updateButton(this.beSubstitutionsEnabledBtn, this.container.beSubstitute);
        updateButton(this.beSubstitutionsDisabledBtn, !this.container.beSubstitute);
        updateButton(this.fluidPrioritizedEnabledBtn, this.container.prioritize);
        updateButton(this.fluidPrioritizedDisabledBtn, !this.container.prioritize);
        updateButton(this.autoFillPatternEnableBtn, this.container.autoFillPattern);
        updateButton(this.autoFillPatternDisableBtn, !this.container.autoFillPattern);
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    protected void updateButton(GuiButton button, boolean vis) {
        if (button != null) {
            button.visible = vis;
        }
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        if (s.isPlayerSide()) {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
        } else {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 3;
        }
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot0(s)) super.func_146977_a(s);
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotFake && !this.container.isCraftingMode()) {
            AEItemStack stack = AEItemStack.create(slot.getStack());
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return true;
            IAEItemStack fake = stack.copy();
            if (fake.getItemStack().getItem() instanceof ItemFluidPacket) {
                if (ItemFluidPacket.getFluidStack(stack) != null && ItemFluidPacket.getFluidStack(stack).amount > 0)
                    fake.setStackSize(ItemFluidPacket.getFluidStack(stack).amount);
            } else return true;
            aeRenderItem.setAeStack(fake);
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            aeRenderItem.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    stack.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
            return false;
        }
        return true;
    }
}
