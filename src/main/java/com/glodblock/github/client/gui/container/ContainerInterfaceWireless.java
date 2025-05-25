package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.inventory.item.IWirelessTerminal;

import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

public class ContainerInterfaceWireless extends FCBaseContainer {

    ContainerInterfaceTerminal delegateContainer;

    public ContainerInterfaceWireless(final InventoryPlayer ip, final IWirelessTerminal monitorable) {
        super(ip, monitorable);

        delegateContainer = new ContainerInterfaceTerminal(ip, monitorable);

        this.bindPlayerInventory(ip, 14, 3);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }
        super.detectAndSendChanges();

        delegateContainer.detectAndSendChanges();
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        delegateContainer.doAction(player, action, slot, id);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }
}
