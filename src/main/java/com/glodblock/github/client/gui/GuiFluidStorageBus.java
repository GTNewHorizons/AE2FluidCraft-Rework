package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidStorageBus;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.NameConst;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;

public class GuiFluidStorageBus extends GuiUpgradeable {

    private GuiImgButton rwMode;
    private GuiTabButton priority;
    private GuiImgButton partition;
    private GuiImgButton clear;

    public GuiFluidStorageBus(InventoryPlayer inventoryPlayer, PartFluidStorageBus te) {
        super(new ContainerFluidStorageBus(inventoryPlayer, te));
        this.ySize = 251;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addButtons() {
        this.clear = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE);
        this.partition = new GuiImgButton(this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH);
        this.rwMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + 48,
                Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.priority = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                2 + 4 * 16,
                GuiText.Priority.getLocal(),
                itemRender);

        this.buttonList.add(this.priority);
        this.buttonList.add(this.rwMode);
        this.buttonList.add(this.partition);
        this.buttonList.add(this.clear);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                this.getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_STORAGE_BUS)),
                8,
                6,
                GuiColors.StorageBusTitle.getColor());
        this.fontRendererObj.drawString(
                GuiText.inventory.getLocal(),
                8,
                this.ySize - 96 + 3,
                GuiColors.StorageBusInventory.getColor());

        if (this.rwMode != null) {
            this.rwMode.set(((ContainerFluidStorageBus) this.cvb).getReadWriteMode());
        }
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        final boolean backwards = Mouse.isButtonDown(1);
        if (btn == this.partition) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("StorageBus.Action", "Partition"));
        } else if (btn == this.clear) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("StorageBus.Action", "Clear"));
        } else if (btn == this.priority) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.PRIORITY));
        } else if (btn == this.rwMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.rwMode.getSetting(), backwards));
        }
    }

    public void update(int id, IAEFluidStack stack) {
        ((ContainerFluidStorageBus) this.cvb).bus.setFluidInSlot(id, stack);
    }
}
