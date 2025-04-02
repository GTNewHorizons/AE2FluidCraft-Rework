package com.glodblock.github.client.gui.container.base;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.item.IFluidPortableCell;
import com.glodblock.github.inventory.item.IWirelessExtendCard;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.util.Util;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;

public abstract class FCBaseContainer extends AEBaseContainer {

    private int ticks;
    private final double powerMultiplier = 0.5;
    private ITerminalHost host;
    private int slot = -1;
    private final InventoryPlayer ip;

    @GuiSync(105)
    public boolean restock = false;

    @GuiSync(106)
    public WirelessMagnet.Mode mode = WirelessMagnet.Mode.Off;

    public FCBaseContainer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.ip = ip;
        this.host = monitorable;
        if (isWirelessTerminal()) {
            this.slot = lockSlot();
        }
    }

    private int lockSlot() {
        if (isWirelessTerminal()) {
            if (this.host instanceof IFluidPortableCell) {
                final int slotIndex = ((IFluidPortableCell) this.host).getInventorySlot();
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
                    ((IFluidPortableCell) this.host));
        }

        if (Platform.isServer()) {
            if (this.host instanceof IWirelessExtendCard wec) {
                this.mode = wec.getMagnetCardMode();
                this.restock = wec.isRestock();
            }
        }
        super.detectAndSendChanges();
    }

    public double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    public ITerminalHost getHost() {
        return this.host;
    }

    public IFluidPortableCell getPortableCell() {
        if (this.host instanceof IFluidPortableCell fpc) {
            return fpc;
        }
        return null;
    }
}
