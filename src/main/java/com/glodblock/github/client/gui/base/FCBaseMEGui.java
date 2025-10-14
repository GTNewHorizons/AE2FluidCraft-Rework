package com.glodblock.github.client.gui.base;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.client.gui.GuiLevelWireless;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;

import appeng.api.storage.StorageChannel;
import appeng.client.gui.AEBaseMEGui;
import appeng.core.sync.GuiBridge;

public abstract class FCBaseMEGui extends AEBaseMEGui {

    protected GuiFCImgButton FluidTerminal;
    protected GuiFCImgButton CraftingTerminal;
    protected GuiFCImgButton PatternTerminal;
    protected GuiFCImgButton EssentiaTerminal;
    protected GuiFCImgButton InterfaceTerminal;
    protected GuiFCImgButton LevelTerminal;
    protected GuiFCImgButton PatternTerminalEx;
    protected GuiFCImgButton magnetOff;
    protected GuiFCImgButton magnetInv;
    protected GuiFCImgButton magnetME;
    protected GuiFCImgButton magnetFilter;
    protected GuiFCImgButton restockEnableBtn;
    protected GuiFCImgButton restockDisableBtn;
    protected List<GuiFCImgButton> termBtns = new ArrayList<>();
    protected boolean drawSwitchGuiBtn;
    private StorageChannel channel = null;

    public FCBaseMEGui(final InventoryPlayer inventoryPlayer, Container container) {
        super(container);
        if (container instanceof FCBaseContainer) {
            Object target = ((FCBaseContainer) container).getTarget();
            if (target instanceof IWirelessTerminal
                    && ((IWirelessTerminal) target).getItemStack().getItem() instanceof ItemWirelessUltraTerminal) {
                this.drawSwitchGuiBtn = true;
                channel = ((IWirelessTerminal) target).getChannel();
            }
        }
    }

    protected WirelessMagnet.Mode getMagnetMode() {
        return ((FCBaseContainer) this.inventorySlots).mode;
    }

    protected boolean isRestock() {
        return ((FCBaseContainer) this.inventorySlots).restock;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    protected void initGuiDone() {
        if (drawSwitchGuiBtn) {
            drawSwitchGuiBtns();
        }
    }

    public abstract int getOffsetY();

    public abstract void setOffsetY(int y);

    protected void drawSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        if (!termBtns.isEmpty()) {
            this.termBtns.clear();
        }
        if (this.getMagnetMode() != null && this.channel == StorageChannel.ITEMS) {
            this.buttonList.add(
                    this.magnetOff = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "OFF"));
            this.buttonList.add(
                    this.magnetInv = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "INV"));
            this.buttonList.add(
                    this.magnetME = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "ME"));
            this.buttonList.add(
                    this.magnetFilter = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 104,
                            "MAGNET_CARD",
                            "FILTER"));
        }
        if (this.channel == StorageChannel.ITEMS) {
            this.buttonList.add(
                    this.restockEnableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 84,
                            "RESTOCK",
                            "ENABLE"));
            this.buttonList.add(
                    this.restockDisableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - 18,
                            this.guiTop + this.ySize - 84,
                            "RESTOCK",
                            "DISABLE"));
        }
        this.buttonList.add(
                this.InterfaceTerminal = new GuiFCImgButton(
                        this.guiLeft - 18,
                        this.getOffsetY(),
                        "INTERFACE_TEM",
                        "YES"));
        this.setOffsetY(this.getOffsetY() + 20);
        termBtns.add(this.InterfaceTerminal);
        if (!(this instanceof GuiLevelWireless)) {
            this.buttonList.add(
                    this.LevelTerminal = new GuiFCImgButton(this.guiLeft - 18, this.getOffsetY(), "LEVEL_TEM", "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.LevelTerminal);
        }
    }

    protected void addSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        this.buttonList.addAll(termBtns);
    }

    private boolean scheduleGuiResize;

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        super.mouseClicked(xCoord, yCoord, btn);
        if (scheduleGuiResize) {
            // in the GuiScreen class, the implementation of super.mouseClicked
            // ends up looping on the button list and execute the action for any
            // button below the mouse.
            // Therefore, if we initGui() the terminal in the actionPerformed method below
            // it will run the actionPerformed a second time for the new button
            // that will end up being below the mouse (if any) after the initGui()
            buttonList.clear();
            initGui();
            scheduleGuiResize = false;
        }
    }

    protected final void scheduleGuiResize() {
        scheduleGuiResize = true;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        if (isPortableCell()) return;
        if (getMagnetMode() != null && this.channel == StorageChannel.ITEMS) {
            GuiFCImgButton[] magnetButtons = { this.magnetOff, this.magnetInv, this.magnetME };
            for (int i = 0; i < magnetButtons.length; i++) {
                magnetButtons[i].visible = getMagnetMode().ordinal() == i;
            }
        }
        if (this.drawSwitchGuiBtn && this.channel == StorageChannel.ITEMS) { // Only ultra terminal
            this.restockDisableBtn.visible = !this.isRestock();
            this.restockEnableBtn.visible = this.isRestock();
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn instanceof GuiFCImgButton) {
            if (btn == this.CraftingTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiBridge.GUI_CRAFTING_TERMINAL);
            } else if (btn == this.PatternTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiBridge.GUI_PATTERN_TERMINAL);
            } else if (btn == this.InterfaceTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiBridge.GUI_INTERFACE_TERMINAL);
            } else if (btn == this.LevelTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_LEVEL_TERMINAL);
            } else if (btn == this.magnetOff || btn == this.magnetME || btn == this.magnetInv) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WirelessTerminal.MagnetMode", this.getMagnetMode().ordinal()));
            } else if (btn == this.magnetFilter) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WirelessTerminal.OpenMagnet", 0));
            } else if (btn == this.restockDisableBtn || btn == this.restockEnableBtn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidPatternTermBtns("WirelessTerminal.Stock", 1));
            }
        }
        super.actionPerformed(btn);
    }

    protected boolean isPortableCell() {
        return false;
    }
}
