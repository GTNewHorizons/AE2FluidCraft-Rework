package com.glodblock.github.client.gui.container;

import java.nio.BufferOverflowException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.slot.SlotRestrictedInput;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.Platform;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.base.FCContainerMonitor;
import com.glodblock.github.network.SPacketMEItemInvUpdate;

public class ContainerItemMonitor extends FCContainerMonitor<IAEItemStack> {

    private final IItemList<IAEItemStack> items = AEApi.instance().storage().createItemList();

    public ContainerItemMonitor(final InventoryPlayer ip, final ITerminalHost monitorable) {
        this(ip, monitorable, true);
    }

    protected ContainerItemMonitor(final InventoryPlayer ip, final ITerminalHost monitorable,
            final boolean bindInventory) {
        super(ip, monitorable, bindInventory);
        if (Platform.isServer()) {
            this.serverCM = monitorable.getConfigManager();
            if (monitorable instanceof IGridHost) {
                final IGridNode node = ((IGridHost) monitorable).getGridNode(ForgeDirection.UNKNOWN);
                if (node != null) {
                    this.networkNode = node;
                    final IGrid g = node.getGrid();
                    if (g != null) {
                        this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                        IStorageGrid storageGrid = g.getCache(IStorageGrid.class);
                        this.monitor = storageGrid.getItemInventory();
                        if (this.monitor == null) {
                            this.setValidContainer(false);
                        } else {
                            this.monitor.addListener(this, null);
                            this.setCellInventory(this.monitor);
                        }
                    }
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.monitor = monitorable.getItemInventory();
                this.monitor.addListener(this, null);
                this.setCellInventory(this.monitor);
                this.setPowerSource((IEnergySource) monitorable);
            }
        } else {
            this.monitor = null;
        }

        this.canAccessViewCells = false;
        if (monitorable instanceof IViewCellStorage) {
            for (int y = 0; y < 5; y++) {
                this.cellView[y] = new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.VIEW_CELL,
                        ((IViewCellStorage) monitorable).getViewCellStorage(),
                        y,
                        206,
                        y * 18 + 8,
                        this.getInventoryPlayer());
                this.cellView[y].setAllowEdit(this.canAccessViewCells);
                this.addSlotToContainer(this.cellView[y]);
            }
        }

        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
    }

    @Override
    protected void processItemList() {
        if (!this.items.isEmpty()) {
            final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();
            SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate();
            for (final IAEItemStack is : this.items) {
                final IAEItemStack send = monitorCache.findPrecise(is);
                try {
                    if (send != null) {
                        packet.appendItem(send);
                    } else {
                        is.setStackSize(0);
                        packet.appendItem(is);
                    }
                } catch (BufferOverflowException e) {
                    for (final Object c : this.crafters) {
                        if (c instanceof EntityPlayerMP) {
                            FluidCraft.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                        }
                    }
                    packet = new SPacketMEItemInvUpdate();
                    if (send != null) {
                        packet.appendItem(send);
                    } else {
                        is.setStackSize(0);
                        packet.appendItem(is);
                    }
                }
            }
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayerMP) {
                    FluidCraft.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                }
            }
            this.items.resetStatus();
        }
    }

    @Override
    protected void queueInventory(final ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
            final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();
            SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate();
            for (final IAEItemStack is : monitorCache) {
                try {
                    packet.appendItem(is);
                } catch (BufferOverflowException e) {
                    FluidCraft.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                    packet = new SPacketMEItemInvUpdate();
                    packet.appendItem(is);
                }
            }
            FluidCraft.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
        }
    }

    @Override
    public void removeCraftingFromCrafters(final ICrafting c) {
        super.removeCraftingFromCrafters(c);
        if (this.crafters.isEmpty() && this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final BaseActionSource source) {
        for (final IAEItemStack is : change) {
            this.items.add(is);
        }
    }

    @Override
    protected boolean isWirelessTerminal() {
        return false;
    }
}
