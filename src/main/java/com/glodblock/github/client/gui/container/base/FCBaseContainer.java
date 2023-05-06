package com.glodblock.github.client.gui.container.base;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.item.IFluidPortableCell;
import com.glodblock.github.util.Util;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;

public abstract class FCBaseContainer extends AEBaseContainer {

    private int ticks;
    private final double powerMultiplier = 0.5;
    private IFluidPortableCell host;
    private int slot = -1;
    private final InventoryPlayer ip;

    public FCBaseContainer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.ip = ip;
        if (isWirelessTerminal()) {
            host = (IFluidPortableCell) monitorable;
            this.slot = lockSlot();
        }
    }

    private int lockSlot() {
        if (isWirelessTerminal()) {
            if (this.host != null) {
                final int slotIndex = this.host.getInventorySlot();
                if (Util.GuiHelper.decodeInvType(slotIndex).getLeft() == Util.GuiHelper.InvType.PLAYER_INV) {
                    this.lockPlayerInventorySlot(slotIndex);
                }
                return slotIndex;
            } else {
                this.lockPlayerInventorySlot(ip.currentItem);
            }
        }
        return -1;
    }

    protected abstract boolean isWirelessTerminal();

    public void detectAndSendChanges() {
        if (isWirelessTerminal()) {
            this.ticks = Util.drainItemPower(
                    this,
                    this.getPlayerInv(),
                    this.slot,
                    this.ticks,
                    this.getPowerMultiplier(),
                    this.host);
        }
        super.detectAndSendChanges();
    }

    public double getPowerMultiplier() {
        return this.powerMultiplier;
    }

}
