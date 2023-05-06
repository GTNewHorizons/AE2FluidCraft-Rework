package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerDualInterface;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.api.config.*;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.helpers.IInterfaceHost;

public class GuiDualInterface extends GuiUpgradeable {

    private GuiTabButton priority;
    private GuiTabButton switcher;
    private GuiImgButton BlockMode;
    private GuiToggleButton interfaceMode;
    private GuiImgButton insertionMode;
    private GuiImgButton sidelessMode;
    private GuiImgButton advancedBlockingMode;
    private final IInterfaceHost host;

    public GuiDualInterface(InventoryPlayer inventoryPlayer, IInterfaceHost te) {
        super(new ContainerDualInterface(inventoryPlayer, te));
        this.host = te;
        this.ySize = 211;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addButtons() {
        this.priority = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                2 + 4 * 16,
                GuiText.Priority.getLocal(),
                itemRender);
        this.buttonList.add(this.priority);

        this.switcher = new GuiTabButton(
                this.guiLeft + 132,
                this.guiTop,
                host instanceof PartFluidInterface ? ItemAndBlockHolder.FLUID_INTERFACE.stack()
                        : ItemAndBlockHolder.INTERFACE.stack(),
                StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"),
                itemRender);
        this.buttonList.add(this.switcher);

        this.BlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO);
        this.buttonList.add(this.BlockMode);

        this.interfaceMode = new GuiToggleButton(
                this.guiLeft - 18,
                this.guiTop + 26,
                84,
                85,
                GuiText.InterfaceTerminal.getLocal(),
                GuiText.InterfaceTerminalHint.getLocal());
        this.buttonList.add(this.interfaceMode);

        this.insertionMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + 44,
                Settings.INSERTION_MODE,
                InsertionMode.DEFAULT);
        this.buttonList.add(this.insertionMode);

        this.advancedBlockingMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + 62,
                Settings.ADVANCED_BLOCKING_MODE,
                AdvancedBlockingMode.DEFAULT);
        this.buttonList.add(this.advancedBlockingMode);

        if (isTile()) {
            this.sidelessMode = new GuiImgButton(
                    this.guiLeft - 18,
                    this.guiTop + 80,
                    Settings.SIDELESS_MODE,
                    SidelessMode.SIDELESS);
            this.buttonList.add(this.sidelessMode);
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        if (this.BlockMode != null) {
            this.BlockMode.set(((ContainerInterface) this.cvb).getBlockingMode());
        }

        if (this.interfaceMode != null) {
            this.interfaceMode.setState(((ContainerInterface) this.cvb).getInterfaceTerminalMode() == YesNo.YES);
        }
        if (this.insertionMode != null) {
            this.insertionMode.set(((ContainerInterface) this.cvb).getInsertionMode());
        }
        if (this.sidelessMode != null) {
            this.sidelessMode.set(((ContainerDualInterface) this.cvb).getSidelessMode());
        }
        if (this.advancedBlockingMode != null) {
            this.advancedBlockingMode.set(((ContainerDualInterface) this.cvb).getAdvancedBlockingMode());
        }
        this.fontRendererObj.drawString(
                getGuiDisplayName(StatCollector.translateToLocal(NameConst.GUI_FLUID_INTERFACE)),
                8,
                6,
                4210752);
    }

    @Override
    protected String getBackground() {
        if (!ModAndClassUtil.isBigInterface) return "guis/interface.png";
        switch (((ContainerInterface) this.cvb).getPatternCapacityCardsInstalled()) {
            case 1:
                return "guis/interface2.png";
            case 2:
                return "guis/interface3.png";
            case 3:
                return "guis/interface4.png";
        }
        return "guis/interface.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        final boolean backwards = Mouse.isButtonDown(1);
        if (btn == null) {
            return;
        }
        if (btn == this.priority) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.PRIORITY));
        }

        if (btn == this.switcher) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.DUAL_INTERFACE_FLUID));
        }

        if (btn == this.interfaceMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(Settings.INTERFACE_TERMINAL, backwards));
        }

        if (btn == this.BlockMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.BlockMode.getSetting(), backwards));
        }
        if (btn == this.insertionMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.insertionMode.getSetting(), backwards));
        }
        if (btn == this.sidelessMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.sidelessMode.getSetting(), backwards));
        }
        if (btn == this.advancedBlockingMode) {
            NetworkHandler.instance
                    .sendToServer(new PacketConfigButton(this.advancedBlockingMode.getSetting(), backwards));
        }
    }

    private boolean isPart() {
        return this.host instanceof PartFluidInterface;
    }

    private boolean isTile() {
        return this.host instanceof TileFluidInterface;
    }
}
