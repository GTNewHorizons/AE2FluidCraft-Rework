package com.glodblock.github.client.gui.base;

import static com.glodblock.github.util.NameConst.TT_FLUID_TERMINAL_AMOUNT;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiEssentiaTerminal;
import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.GuiFluidMonitor;
import com.glodblock.github.client.gui.GuiFluidPatternExWireless;
import com.glodblock.github.client.gui.GuiFluidPatternWireless;
import com.glodblock.github.client.gui.GuiFluidPortableCell;
import com.glodblock.github.client.gui.GuiInterfaceWireless;
import com.glodblock.github.client.gui.GuiLevelWireless;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.client.gui.container.base.FCContainerMonitor;
import com.glodblock.github.client.gui.widget.GuiFCImgButton;
import com.glodblock.github.common.item.ItemMagnetCard;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.CPacketFluidTerminalBtns;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.me.SlotME;
import appeng.container.slot.SlotFake;
import appeng.core.AEConfig;
import appeng.util.Platform;

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
    protected GuiFCImgButton dataSyncEnableBtn;
    protected GuiFCImgButton dataSyncDisableBtn;
    protected List<GuiFCImgButton> termBtns = new ArrayList<>();
    protected boolean drawSwitchGuiBtn;
    private boolean hasMagnetCard = false;
    private StorageChannel channel = null;
    private final FCBaseContainer container;
    protected final int buttonOffset = 18;

    public FCBaseMEGui(final InventoryPlayer inventoryPlayer, Container container) {
        super(container);
        this.container = (FCBaseContainer) container;
        if (this.container.getTarget() instanceof IWirelessTerminal iwt
                && iwt.getItemStack().getItem() instanceof ItemWirelessUltraTerminal) {
            this.drawSwitchGuiBtn = true;
            this.hasMagnetCard = Util.Wireless.hasMagnetCard(iwt.getItemStack());
            this.channel = iwt.getChannel();
        }
    }

    @Override
    public List<String> handleItemTooltip(final ItemStack stack, final int mouseX, final int mouseY,
            final List<String> currentToolTip) {
        if (this instanceof GuiFluidMonitor) {
            if (stack != null && !isShiftKeyDown()) {
                final Slot s = this.getSlot(mouseX, mouseY);
                final boolean isSlotME = s instanceof SlotME;
                if (isSlotME || s instanceof SlotFake) {
                    final int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

                    IAEItemStack myStack = null;

                    try {
                        myStack = Platform.getAEStackInSlot(s);
                    } catch (final Throwable ignore) {}

                    if (myStack != null) {
                        if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1)) {
                            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                                    .format(myStack.getStackSize());
                            final String format = I18n.format(TT_FLUID_TERMINAL_AMOUNT, formattedAmount);
                            currentToolTip.add("\u00a77" + format);
                        }
                    }
                }
            }
            return currentToolTip;
        } else {
            return super.handleItemTooltip(stack, mouseX, mouseY, currentToolTip);
        }

    }

    protected ItemMagnetCard.Mode getMagnetMode() {
        if (this.hasMagnetCard) {
            return this.container.mode;
        }
        return null;
    }

    protected boolean isRestock() {
        return this.container.restock;
    }

    protected boolean isSyncData() {
        return this.container.sync;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    protected void setSyncState() {
        if (this.container instanceof FCContainerMonitor && !isPortableCell()) {
            this.buttonList.add(
                    this.dataSyncEnableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 44,
                            "DATA_SYNC",
                            "ENABLE"));
            this.buttonList.add(
                    this.dataSyncDisableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 44,
                            "DATA_SYNC",
                            "DISABLE"));
        }
    }

    protected void initGuiDone() {
        this.setSyncState();
        if (drawSwitchGuiBtn) {
            drawSwitchGuiBtns();
        }
    }

    public abstract int getOffsetY();

    public abstract void setOffsetY(int y);

    @SuppressWarnings("unchecked")
    protected void drawSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        if (!termBtns.isEmpty()) {
            this.termBtns.clear();
        }
        if (this.getMagnetMode() != null && this.channel == StorageChannel.ITEMS) {
            this.buttonList.add(
                    this.magnetOff = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "OFF"));
            this.buttonList.add(
                    this.magnetInv = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "INV"));
            this.buttonList.add(
                    this.magnetME = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 124,
                            "MAGNET_CARD",
                            "ME"));
            this.buttonList.add(
                    this.magnetFilter = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 104,
                            "MAGNET_CARD",
                            "FILTER"));
        }
        if (this.channel == StorageChannel.ITEMS) {
            this.buttonList.add(
                    this.restockEnableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 84,
                            "RESTOCK",
                            "ENABLE"));
            this.buttonList.add(
                    this.restockDisableBtn = new GuiFCImgButton(
                            this.guiLeft + this.xSize - buttonOffset,
                            this.guiTop + this.ySize - 84,
                            "RESTOCK",
                            "DISABLE"));
        }
        if (!(this instanceof GuiFluidCraftingWireless)) {
            this.buttonList.add(
                    this.CraftingTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "CRAFT_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.CraftingTerminal);
        }
        if (!(this instanceof GuiFluidPatternWireless)) {
            this.buttonList.add(
                    this.PatternTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "PATTERN_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.PatternTerminal);
        }
        if (!(this instanceof GuiFluidPatternExWireless)) {
            this.buttonList.add(
                    this.PatternTerminalEx = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "PATTERN_EX_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.PatternTerminalEx);
        }
        if (!(this instanceof GuiFluidPortableCell)) {
            this.buttonList.add(
                    this.FluidTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "FLUID_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.FluidTerminal);
        }
        if (!(this instanceof GuiInterfaceWireless)) {
            this.buttonList.add(
                    this.InterfaceTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "INTERFACE_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.InterfaceTerminal);
        }
        if (!(this instanceof GuiLevelWireless)) {
            this.buttonList.add(
                    this.LevelTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "LEVEL_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.LevelTerminal);
        }
        if (ModAndClassUtil.ThE && !(this instanceof GuiEssentiaTerminal)) {
            this.buttonList.add(
                    this.EssentiaTerminal = new GuiFCImgButton(
                            this.guiLeft - buttonOffset,
                            this.getOffsetY(),
                            "ESSENTIA_TEM",
                            "YES"));
            this.setOffsetY(this.getOffsetY() + 20);
            termBtns.add(this.EssentiaTerminal);
        }
    }

    @SuppressWarnings("unchecked")
    protected void addSwitchGuiBtns() {
        if (!drawSwitchGuiBtn) return;
        this.buttonList.addAll(termBtns);
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
        this.dataSyncDisableBtn.visible = !isSyncData();
        this.dataSyncEnableBtn.visible = isSyncData();
        if (this.drawSwitchGuiBtn && this.channel == StorageChannel.ITEMS) { // Only ultra terminal
            this.restockDisableBtn.visible = !this.isRestock();
            this.restockEnableBtn.visible = this.isRestock();
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn instanceof GuiFCImgButton) {
            if (btn == this.FluidTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_TERMINAL);
            } else if (btn == this.CraftingTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_CRAFTING_TERMINAL);
            } else if (btn == this.EssentiaTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_ESSENTIA_TERMINAL);
            } else if (btn == this.PatternTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
            } else if (btn == this.InterfaceTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_INTERFACE_TERMINAL);
            } else if (btn == this.LevelTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_LEVEL_TERMINAL);
            } else if (btn == this.PatternTerminalEx) {
                ItemWirelessUltraTerminal.switchTerminal(this.mc.thePlayer, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL_EX);
            }
            if (btn == this.magnetOff || btn == this.magnetME || btn == this.magnetInv) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketFluidTerminalBtns("WirelessTerminal.MagnetMode", this.getMagnetMode().ordinal()));
            } else if (btn == this.magnetFilter) {
                FluidCraft.proxy.netHandler
                        .sendToServer(new CPacketFluidTerminalBtns("WirelessTerminal.OpenMagnet", 0));
            } else if (btn == this.restockDisableBtn || btn == this.restockEnableBtn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidTerminalBtns("WirelessTerminal.Stock", 1));
            } else if (btn == this.dataSyncDisableBtn || btn == this.dataSyncEnableBtn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidTerminalBtns("Terminal.Sync", !isSyncData()));
            }
        }
        super.actionPerformed(btn);
    }

    protected boolean isPortableCell() {
        return false;
    }
}
