package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerMagnetFilter;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.NameConst;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.GuiSub;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.MEGuiTextField;

public class GuiMagnetFilter extends GuiSub {

    protected FCGuiBaseButton listModeBtn;
    protected ContainerMagnetFilter cont;
    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/magnet_filter.png");

    protected Component[] components = new Component[4];
    protected MEGuiTextField oreDict;
    protected GuiImgButton clearBtn;

    public GuiMagnetFilter(InventoryPlayer ip, ITerminalHost container) {
        super(new ContainerMagnetFilter(ip, container));
        this.xSize = 176;
        this.ySize = 214;
        this.cont = (ContainerMagnetFilter) this.inventorySlots;
        oreDict = new MEGuiTextField(149, 12, NameConst.i18n(NameConst.TT_MAGNET_CARD_OREDICT));
    }

    private class Component {

        private final GuiFCImgButton enable;
        private final GuiFCImgButton disable;
        private final CPacketFluidPatternTermBtns.Command action;
        private boolean var;

        public Component(int x, int y, boolean var, CPacketFluidPatternTermBtns.Command action) {
            this.enable = new GuiFCImgButton(x, y, "ENABLE_12x", "ENABLE", false);
            this.disable = new GuiFCImgButton(x, y, "DISABLE_12x", "DISABLE", false);
            this.var = var;
            this.action = action;
            enable.setThreeFourths(true);
            disable.setThreeFourths(true);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
        }

        public boolean sameBtn(GuiButton btn) {
            return btn == this.enable || btn == this.disable;
        }

        public boolean getVar() {
            return this.var;
        }

        public void setVar(boolean var) {
            this.var = var;
        }

        public void send() {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns(this.action, !this.getVar()));
        }

        public void draw() {
            if (var) {
                this.enable.visible = true;
                this.disable.visible = false;
            } else {
                this.enable.visible = false;
                this.disable.visible = true;
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.buttonList.add(
                this.listModeBtn = new FCGuiBaseButton(
                        0,
                        this.guiLeft + 86,
                        this.guiTop + 4,
                        64,
                        14,
                        NameConst.i18n(
                                this.cont.listMode == WirelessMagnet.ListMode.WhiteList
                                        ? NameConst.GUI_MAGNET_CARD_WhiteList
                                        : NameConst.GUI_MAGNET_CARD_BlackList)));
        this.components[0] = new Component(
                this.guiLeft + 157,
                this.guiTop + 18,
                this.cont.nbt,
                CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_NBT);
        this.components[1] = new Component(
                this.guiLeft + 157,
                this.guiTop + 31,
                this.cont.meta,
                CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_META);
        this.components[2] = new Component(
                this.guiLeft + 157,
                this.guiTop + 44,
                this.cont.ore,
                CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_ORE);
        this.components[3] = new Component(
                this.guiLeft + 157,
                this.guiTop + 112,
                this.cont.oreDict,
                CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_OREDICT_STATE);

        oreDict.x = this.guiLeft + 7;
        oreDict.y = this.guiTop + 112;
        oreDict.setText(this.cont.oreDictFilter);

        this.clearBtn = new GuiImgButton(this.guiLeft + 7, this.guiTop + 48, Settings.ACTIONS, ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.buttonList.add(this.clearBtn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        handleTooltip(mouseX, mouseY, oreDict);
        super.drawScreen(mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString(NameConst.i18n(NameConst.GUI_MAGNET_CARD), 8, 6, 0x404040);
        this.fontRendererObj.drawString(NameConst.i18n(NameConst.GUI_MAGNET_CARD_NBT), 61, 22, 0x404040);
        this.fontRendererObj.drawString(NameConst.i18n(NameConst.GUI_MAGNET_CARD_META), 61, 34, 0x404040);
        this.fontRendererObj.drawString(NameConst.i18n(NameConst.GUI_MAGNET_CARD_ORE), 61, 46, 0x404040);
        this.components[0].setVar(this.cont.nbt);
        this.components[1].setVar(this.cont.meta);
        this.components[2].setVar(this.cont.ore);
        this.components[3].setVar(this.cont.oreDict);
        for (Component c : components) {
            c.draw();
        }
        this.listModeBtn.displayString = NameConst.i18n(
                this.cont.listMode == WirelessMagnet.ListMode.WhiteList ? NameConst.GUI_MAGNET_CARD_WhiteList
                        : NameConst.GUI_MAGNET_CARD_BlackList);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
        oreDict.drawTextBox();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (oreDict.isFocused() && (Keyboard.KEY_NUMPADENTER == keyCode || Keyboard.KEY_RETURN == keyCode)) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_OREDICT_FILTER,
                            oreDict.getText()));
        }

        if (!oreDict.textboxKeyTyped(typedChar, keyCode)) super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        oreDict.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        for (Component c : components) {
            if (c.sameBtn(btn)) {
                c.send();
                break;
            }
        }
        if (btn == this.listModeBtn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(
                            CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_MODE,
                            this.cont.listMode != WirelessMagnet.ListMode.WhiteList));
        } else if (btn == this.clearBtn) {
            FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(CPacketFluidPatternTermBtns.Command.MAGNET_FILTER_CLEAR, 1));
        }
        super.actionPerformed(btn);
    }
}
