package com.glodblock.github.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.base.FCGuiEncodeTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.PatternBeSubstitution;
import appeng.api.config.PatternSlotConfig;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.helpers.InventoryAction;

public class GuiFluidPatternTerminalEx extends FCGuiEncodeTerminal {

    public GuiFluidPatternTerminalEx(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te, new ContainerFluidPatternTerminalEx(inventoryPlayer, te));
        processingScrollBar.setHeight(70).setWidth(7).setLeft(6).setRange(0, 1, 1);
        processingScrollBar.setTexture(FluidCraft.MODID, "gui/pattern3.png", 242, 0);
    }

    public GuiFluidPatternTerminalEx(final InventoryPlayer inventoryPlayer, final IWirelessTerminal te) {
        super(inventoryPlayer, te, new ContainerFluidPatternExWireless(inventoryPlayer, te));
        processingScrollBar.setHeight(70).setWidth(7).setLeft(6).setRange(0, 1, 1);
        processingScrollBar.setTexture(FluidCraft.MODID, "gui/pattern3.png", 242, 0);
    }

    @Override
    public void initGui() {
        super.initGui();

        this.substitutionsEnabledBtn = new GuiImgButton(
                this.guiLeft + 97,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ItemSubstitution.ENABLED);
        this.substitutionsEnabledBtn.setHalfSize(true);
        this.buttonList.add(this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new GuiImgButton(
                this.guiLeft + 97,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ItemSubstitution.DISABLED);
        this.substitutionsDisabledBtn.setHalfSize(true);
        this.buttonList.add(this.substitutionsDisabledBtn);

        this.fluidPrioritizedEnabledBtn = new GuiFCImgButton(
                this.guiLeft + 97,
                this.guiTop + this.ySize - 114,
                "FORCE_PRIO",
                "DO_PRIO");
        this.fluidPrioritizedEnabledBtn.setHalfSize(true);
        this.buttonList.add(this.fluidPrioritizedEnabledBtn);

        this.fluidPrioritizedDisabledBtn = new GuiFCImgButton(
                this.guiLeft + 97,
                this.guiTop + this.ySize - 114,
                "NOT_PRIO",
                "DONT_PRIO");
        this.fluidPrioritizedDisabledBtn.setHalfSize(true);
        this.buttonList.add(this.fluidPrioritizedDisabledBtn);

        invertBtn = new GuiImgButton(
                this.guiLeft + 87,
                this.guiTop + this.ySize - 153,
                Settings.ACTIONS,
                container.inverted ? PatternSlotConfig.C_4_16 : PatternSlotConfig.C_16_4);
        invertBtn.setHalfSize(true);
        this.buttonList.add(this.invertBtn);

        this.clearBtn = new GuiImgButton(
                this.guiLeft + 87,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.buttonList.add(this.clearBtn);

        this.encodeBtn = new GuiImgButton(
                this.guiLeft + 147,
                this.guiTop + this.ySize - 142,
                Settings.ACTIONS,
                ActionItems.ENCODE);
        this.buttonList.add(this.encodeBtn);

        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton(
                    this.guiLeft + 97,
                    this.guiTop + this.ySize - 153,
                    Settings.ACTIONS,
                    ActionItems.DOUBLE);
            this.doubleBtn.setHalfSize(true);
            this.buttonList.add(this.doubleBtn);
        }

        this.combineEnableBtn = new GuiFCImgButton(
                this.guiLeft + 87,
                this.guiTop + this.ySize - 114,
                "FORCE_COMBINE",
                "DO_COMBINE");
        this.combineEnableBtn.setHalfSize(true);
        this.buttonList.add(this.combineEnableBtn);

        this.combineDisableBtn = new GuiFCImgButton(
                this.guiLeft + 87,
                this.guiTop + this.ySize - 114,
                "NOT_COMBINE",
                "DONT_COMBINE");
        this.combineDisableBtn.setHalfSize(true);
        this.buttonList.add(this.combineDisableBtn);
        if (ModAndClassUtil.isBeSubstitutionsButton) {
            this.beSubstitutionsEnabledBtn = new GuiImgButton(
                    this.guiLeft + 87,
                    this.guiTop + this.ySize - 103,
                    Settings.ACTIONS,
                    PatternBeSubstitution.ENABLED);
            this.beSubstitutionsEnabledBtn.setHalfSize(true);
            this.buttonList.add(this.beSubstitutionsEnabledBtn);

            this.beSubstitutionsDisabledBtn = new GuiImgButton(
                    this.guiLeft + 87,
                    this.guiTop + this.ySize - 103,
                    Settings.ACTIONS,
                    PatternBeSubstitution.DISABLED);
            this.beSubstitutionsDisabledBtn.setHalfSize(true);
            this.buttonList.add(this.beSubstitutionsDisabledBtn);
        }
        processingScrollBar.setTop(this.ySize - 164);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal(NameConst.GUI_FLUID_PATTERN_TERMINAL_EX),
                8,
                this.ySize - 96 + 2 - getReservedSpace(),
                4210752);
        this.processingScrollBar.draw(this);
    }

    @Override
    protected String getBackground() {
        return container.inverted ? "gui/pattern4.png" : "gui/pattern3.png";
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        final int offset = container.inverted ? 18 * -3 : 0;
        substitutionsEnabledBtn.xPosition = this.guiLeft + 97 + offset;
        substitutionsDisabledBtn.xPosition = this.guiLeft + 97 + offset;
        beSubstitutionsEnabledBtn.xPosition = this.guiLeft + 97 + offset;
        beSubstitutionsDisabledBtn.xPosition = this.guiLeft + 97 + offset;
        fluidPrioritizedEnabledBtn.xPosition = this.guiLeft + 97 + offset;
        fluidPrioritizedDisabledBtn.xPosition = this.guiLeft + 97 + offset;
        doubleBtn.xPosition = this.guiLeft + 97 + offset;
        clearBtn.xPosition = this.guiLeft + 87 + offset;
        invertBtn.xPosition = this.guiLeft + 87 + offset;
        combineEnableBtn.xPosition = this.guiLeft + 87 + offset;
        combineDisableBtn.xPosition = this.guiLeft + 87 + offset;
        processingScrollBar.setCurrentScroll(container.activePage);
        super.drawScreen(mouseX, mouseY, btn);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, xCoord - this.guiLeft, yCoord - this.guiTop);
        if (btn == 2 && doubleBtn.mousePressed(this.mc, xCoord, yCoord)) { //
            InventoryAction action = InventoryAction.SET_PATTERN_MULTI;

            final CPacketInventoryAction p = new CPacketInventoryAction(action, 0, 0);
            FluidCraft.proxy.netHandler.sendToServer(p);
        } else super.mouseClicked(xCoord, yCoord, btn);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, x - this.guiLeft, y - this.guiTop);
        super.mouseClickMove(x, y, c, d);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        final int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight;

            if (this.processingScrollBar.contains(x - this.guiLeft, y - this.guiTop)) {
                final int currentScroll = this.processingScrollBar.getCurrentScroll();
                this.processingScrollBar.wheel(wheel);

                if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
                    changeActivePage();
                }
            }
        }
    }

    private void changeActivePage() {
        FluidCraft.proxy.netHandler.sendToServer(
                new CPacketFluidPatternTermBtns(
                        "PatternTerminal.ActivePage",
                        String.valueOf(this.processingScrollBar.getCurrentScroll())));
    }
}
