/* Author: asdflj */
package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerMagnetFilter;
import com.glodblock.github.client.gui.widget.FCGuiBaseButton;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.util.NameConst;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;

public class GuiMagnetFilter extends AEBaseMEGui {

    protected FCGuiBaseButton listModeBtn;
    protected ContainerMagnetFilter cont;
    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/magnet_filter.png");

    protected Component components[] = new Component[3];
    protected GuiTabButton originalGuiBtn;
    protected GuiImgButton clearBtn;

    public GuiMagnetFilter(InventoryPlayer ip, ITerminalHost container) {
        super(new ContainerMagnetFilter(ip, container));
        this.xSize = 195;
        this.ySize = 214;
        this.cont = (ContainerMagnetFilter) this.inventorySlots;
    }

    private class Component {

        private GuiFCImgButton enable;
        private GuiFCImgButton disable;
        private String action;
        private boolean var;

        public Component(int x, int y, boolean var, String action) {
            this.enable = new GuiFCImgButton(x, y, "ENABLE_12x", "ENABLE", false);
            this.disable = new GuiFCImgButton(x, y, "DISABLE_12x", "DISABLE", false);
            this.var = var;
            this.action = action;
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
                this.guiLeft + 156,
                this.guiTop + 18,
                this.cont.nbt,
                "WirelessTerminal.magnet.NBT");
        this.components[1] = new Component(
                this.guiLeft + 156,
                this.guiTop + 31,
                this.cont.meta,
                "WirelessTerminal.magnet.Meta");
        this.components[2] = new Component(
                this.guiLeft + 156,
                this.guiTop + 44,
                this.cont.ore,
                "WirelessTerminal.magnet.Ore");

        this.buttonList.add(
                this.originalGuiBtn = new GuiTabButton(
                        this.guiLeft + this.xSize - 44,
                        this.guiTop - 4,
                        this.cont.getPortableCell().getItemStack(),
                        this.cont.getPortableCell().getItemStack().getDisplayName(),
                        itemRender));
        this.originalGuiBtn.setHideEdge(13); // GuiTabButton implementation //

        this.clearBtn = new GuiImgButton(this.guiLeft + 8, this.guiTop + 48, Settings.ACTIONS, ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.buttonList.add(this.clearBtn);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString(getGuiDisplayName(NameConst.i18n(NameConst.GUI_MAGNET_CARD)), 8, 6, 0x404040);
        this.fontRendererObj
                .drawString(getGuiDisplayName(NameConst.i18n(NameConst.GUI_MAGNET_CARD_NBT)), 61, 22, 0x404040);
        this.fontRendererObj
                .drawString(getGuiDisplayName(NameConst.i18n(NameConst.GUI_MAGNET_CARD_META)), 61, 34, 0x404040);
        this.fontRendererObj
                .drawString(getGuiDisplayName(NameConst.i18n(NameConst.GUI_MAGNET_CARD_ORE)), 61, 46, 0x404040);
        this.components[0].setVar(this.cont.nbt);
        this.components[1].setVar(this.cont.meta);
        this.components[2].setVar(this.cont.ore);
        for (Component c : components) {
            c.draw();
        }
        this.listModeBtn.displayString = NameConst.i18n(
                this.cont.listMode == WirelessMagnet.ListMode.WhiteList ? NameConst.GUI_MAGNET_CARD_WhiteList
                        : NameConst.GUI_MAGNET_CARD_BlackList);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
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
                            "WirelessTerminal.magnet.FilterMode",
                            this.cont.listMode != WirelessMagnet.ListMode.WhiteList));
        } else if (btn == this.originalGuiBtn) {
            InventoryHandler.switchGui(ItemWirelessUltraTerminal.readMode(this.cont.getPortableCell().getItemStack()));
        } else if (btn == this.clearBtn) {
            FluidCraft.proxy.netHandler
                    .sendToServer(new CPacketFluidPatternTermBtns("WirelessTerminal.magnet.clear", 1));
        }
        super.actionPerformed(btn);
    }
}
