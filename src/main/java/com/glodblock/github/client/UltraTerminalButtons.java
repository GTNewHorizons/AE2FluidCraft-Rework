package com.glodblock.github.client;

import static appeng.util.Platform.nextEnum;
import static com.glodblock.github.common.item.ItemWirelessUltraTerminal.MODE;
import static com.glodblock.github.inventory.item.WirelessMagnet.modeKey;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.UltraTerminalModes;

import appeng.helpers.ICustomButtonDataObject;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;

public class UltraTerminalButtons implements ICustomButtonDataObject {

    public static String restockItems = "restock";

    private ItemStack terminal;

    private GuiFCImgButton CraftingTerminal;
    private GuiFCImgButton PatternTerminal;
    private GuiFCImgButton InterfaceTerminal;
    private GuiFCImgButton LevelTerminal;
    private GuiFCImgButton PatternTerminalEx;

    private GuiFCImgButton magnetOff;
    private GuiFCImgButton magnetInv;
    private GuiFCImgButton magnetME;

    private GuiFCImgButton magnetFilter;

    private GuiFCImgButton restockEnableBtn;
    private GuiFCImgButton restockDisableBtn;

    private WirelessMagnet.Mode magnetMode;
    private boolean reStock;
    public UltraTerminalModes terminalMode;

    private int offset;

    public UltraTerminalButtons(ItemStack terminal) {
        this.terminal = terminal;
    }

    public void initCustomButtons(int guiLeft, int guiTop, int xSize, int ySize, int xOffset, int yOffset,
            List<GuiButton> buttonList) {
        this.offset = yOffset;
        buttonList.add(
                this.magnetOff = new GuiFCImgButton(guiLeft + xSize - 17, guiTop + ySize - 65, "MAGNET_CARD", "OFF"));
        buttonList.add(
                this.magnetInv = new GuiFCImgButton(guiLeft + xSize - 17, guiTop + ySize - 65, "MAGNET_CARD", "INV"));
        buttonList.add(
                this.magnetME = new GuiFCImgButton(guiLeft + xSize - 17, guiTop + ySize - 65, "MAGNET_CARD", "ME"));

        buttonList.add(
                this.magnetFilter = new GuiFCImgButton(
                        guiLeft + xSize - 17,
                        guiTop + ySize - 45,
                        "MAGNET_CARD",
                        "FILTER"));
        buttonList.add(
                this.restockEnableBtn = new GuiFCImgButton(
                        guiLeft + xSize - 17,
                        guiTop + ySize - 25,
                        "RESTOCK",
                        "ENABLE"));

        buttonList.add(
                this.restockDisableBtn = new GuiFCImgButton(
                        guiLeft + xSize - 17,
                        guiTop + ySize - 25,
                        "RESTOCK",
                        "DISABLE"));

        buttonList.add(this.CraftingTerminal = new GuiFCImgButton(guiLeft - 18, yOffset, "CRAFT_TEM", "YES"));

        buttonList.add(this.PatternTerminal = new GuiFCImgButton(guiLeft - 18, yOffset, "PATTERN_TEM", "YES"));

        buttonList.add(this.PatternTerminalEx = new GuiFCImgButton(guiLeft - 18, yOffset, "PATTERN_EX_TEM", "YES"));

        buttonList.add(this.InterfaceTerminal = new GuiFCImgButton(guiLeft - 18, yOffset, "INTERFACE_TEM", "YES"));

        buttonList.add(this.LevelTerminal = new GuiFCImgButton(guiLeft - 18, yOffset, "LEVEL_TEM", "YES"));

        toggleSwitchButtons();
        toggleMagnetButtonsVisibility();
        toggleRestockVisibility();
    }

    @Override
    public boolean actionPerformedCustomButtons(final GuiButton btn) {
        if (btn instanceof GuiFCImgButton) {
            UltraTerminalModes mode = null;
            if (btn == this.CraftingTerminal) {
                mode = UltraTerminalModes.CRAFTING;
            } else if (btn == this.PatternTerminal) {
                mode = UltraTerminalModes.PATTERN;
            } else if (btn == this.InterfaceTerminal) {
                mode = UltraTerminalModes.INTERFACE;
            } else if (btn == this.LevelTerminal) {
                mode = UltraTerminalModes.LEVEL;
            } else if (btn == this.PatternTerminalEx) {
                mode = UltraTerminalModes.PATTERN_EX;
            }
            if (mode != null) {
                ItemWirelessUltraTerminal.setMode(this.terminal, mode);
                FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(mode, true));
            }

            if (btn == this.magnetOff || btn == this.magnetME || btn == this.magnetInv) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketFluidPatternTermBtns(
                                CPacketFluidPatternTermBtns.Command.MAGNET_MODE,
                                this.magnetMode.ordinal()));
            } else if (btn == this.magnetFilter) FluidCraft.proxy.netHandler.sendToServer(
                    new CPacketFluidPatternTermBtns(CPacketFluidPatternTermBtns.Command.MAGNET_OPEN_FILTER, 0));
            else if (btn == this.restockDisableBtn || btn == this.restockEnableBtn) {
                FluidCraft.proxy.netHandler
                        .sendToServer(new CPacketFluidPatternTermBtns(CPacketFluidPatternTermBtns.Command.RESTOCK, 1));
            }

            return true;
        }
        return false;
    }

    public void toggleRestock() {
        this.reStock = !this.reStock;
    }

    public void toggleRestockVisibility() {
        if (Platform.isClient()) {
            if (this.reStock) {
                this.restockEnableBtn.setVisibility(true);
                this.restockDisableBtn.setVisibility(false);
            } else {
                this.restockEnableBtn.setVisibility(false);
                this.restockDisableBtn.setVisibility(true);
            }
        }
    }

    public void toggleMagnetMode() {
        this.magnetMode = nextEnum(this.magnetMode);
    }

    public void toggleMagnetButtonsVisibility() {
        switch (this.magnetMode) {
            case Off -> {
                this.magnetME.setVisibility(false);
                this.magnetInv.setVisibility(false);
                this.magnetOff.setVisibility(true);
            }
            case ME -> {
                this.magnetME.setVisibility(true);
                this.magnetInv.setVisibility(false);
                this.magnetOff.setVisibility(false);
            }
            case Inv -> {
                this.magnetME.setVisibility(false);
                this.magnetInv.setVisibility(true);
                this.magnetOff.setVisibility(false);
            }
        }
    }

    public void toggleSwitchButtons() {
        int offset = this.offset;

        CraftingTerminal.setVisibility(false);
        PatternTerminal.setVisibility(false);
        InterfaceTerminal.setVisibility(false);
        LevelTerminal.setVisibility(false);
        PatternTerminalEx.setVisibility(false);

        if (this.terminalMode != UltraTerminalModes.CRAFTING) {
            this.CraftingTerminal.setVisibility(true);
            this.CraftingTerminal.yPosition = offset;
            offset += 20;
        }
        if (this.terminalMode != UltraTerminalModes.PATTERN) {
            this.PatternTerminal.setVisibility(true);
            this.PatternTerminal.yPosition = offset;
            offset += 20;
        }
        if (this.terminalMode != UltraTerminalModes.PATTERN_EX) {
            this.PatternTerminalEx.setVisibility(true);
            this.PatternTerminalEx.yPosition = offset;
            offset += 20;
        }
        if (this.terminalMode != UltraTerminalModes.INTERFACE) {
            this.InterfaceTerminal.setVisibility(true);
            this.InterfaceTerminal.yPosition = offset;
            offset += 20;
        }
        if (this.terminalMode != UltraTerminalModes.LEVEL) {
            this.LevelTerminal.setVisibility(true);
            this.LevelTerminal.yPosition = offset;
            offset += 20;
        }
    }

    @Override
    public void readData(NBTTagCompound tag) {
        this.reStock = tag.getBoolean(restockItems);
        this.magnetMode = WirelessMagnet.Mode.values()[tag.getInteger(modeKey)];
        this.terminalMode = UltraTerminalModes.values()[tag.getInteger(MODE)];
    }

    @Override
    public void writeData(NBTTagCompound tag) {
        tag.setBoolean(restockItems, this.reStock);
        tag.setInteger(modeKey, this.magnetMode.ordinal());
        tag.setInteger(MODE, this.terminalMode.ordinal());
    }

    @Override
    public void readByte(ByteBuf buf) {
        this.reStock = buf.readBoolean();
        this.magnetMode = WirelessMagnet.Mode.values()[buf.readInt()];
        this.terminalMode = UltraTerminalModes.values()[buf.readInt()];

        toggleMagnetButtonsVisibility();
        toggleRestockVisibility();
        toggleSwitchButtons();
    }

    @Override
    public void writeByte(ByteBuf buf) {
        buf.writeBoolean(this.reStock);
        buf.writeInt(this.magnetMode.ordinal());
        buf.writeInt(this.terminalMode.ordinal());
    }
}
