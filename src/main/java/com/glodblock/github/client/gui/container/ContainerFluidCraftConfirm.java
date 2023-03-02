package com.glodblock.github.client.gui.container;

import java.util.Objects;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftConfirm;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;

public class ContainerFluidCraftConfirm extends ContainerCraftConfirm {

    public ContainerFluidCraftConfirm(final InventoryPlayer ip, final ITerminalHost te) {
        super(ip, te);
    }

    @Override
    public void switchToOriginalGUI() {
        GuiType originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof PartFluidPatternTerminal) {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }
        if (ah instanceof PartFluidPatternTerminalEx) {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        }
        if (ah instanceof IWirelessTerminal) {
            ItemStack terminal = ((IWirelessTerminal) ah).getItemStack();
            if (terminal.getItem() instanceof ItemBaseWirelessTerminal) {
                originalGui = ((ItemBaseWirelessTerminal) terminal.getItem()).guiGuiType(terminal);
            }
        }
        if (ah instanceof IWirelessTerminal) {
            InventoryHandler.openGui(
                    this.getInventoryPlayer().player,
                    getWorld(),
                    new BlockPos(((IWirelessTerminal) ah).getInventorySlot(), 0, 0),
                    ForgeDirection.UNKNOWN,
                    originalGui);
        } else if (this.getOpenContext() != null) {
            InventoryHandler.openGui(
                    this.getInventoryPlayer().player,
                    getWorld(),
                    new BlockPos(this.getOpenContext().getTile()),
                    Objects.requireNonNull(this.getOpenContext().getSide()),
                    originalGui);
        }
    }
}
