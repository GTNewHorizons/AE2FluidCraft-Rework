package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import com.glodblock.github.client.gui.container.ContainerCraftingWireless;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;

public class GuiFluidCraftingWireless extends GuiItemMonitor {

    private GuiImgButton clearBtn;

    public GuiFluidCraftingWireless(final InventoryPlayer inventoryPlayer, final IWirelessTerminal te) {
        super(inventoryPlayer, te, new ContainerCraftingWireless(inventoryPlayer, te));
        this.setReservedSpace(73);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        } else {
            super.actionPerformed(btn);
            if (this.clearBtn == btn) {
                Slot s = null;
                final Container c = this.inventorySlots;
                for (final Object j : c.inventorySlots) {
                    if (j instanceof SlotCraftingMatrix) {
                        s = (Slot) j;
                    }
                }
                if (s != null) {
                    final PacketInventoryAction p = new PacketInventoryAction(
                            InventoryAction.MOVE_REGION,
                            s.slotNumber,
                            0);
                    NetworkHandler.instance.sendToServer(p);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.buttonList.add(
                this.clearBtn = new GuiImgButton(
                        this.guiLeft + 92,
                        this.guiTop + this.ySize - 156,
                        Settings.ACTIONS,
                        ActionItems.STASH));
        this.clearBtn.setHalfSize(true);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        this.fontRendererObj.drawString(
                GuiText.CraftingTerminal.getLocal(),
                8,
                this.ySize - 96 + 1 - this.getReservedSpace(),
                GuiColors.CraftingTerminalTitle.getColor());
    }

    @Override
    protected String getBackground() {
        return "gui/crafting.png";
    }

}
