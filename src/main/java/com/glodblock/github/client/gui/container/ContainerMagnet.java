package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.common.item.ItemMagnetCard;
import com.glodblock.github.inventory.item.IItemTerminal;
import com.glodblock.github.inventory.item.IWirelessMagnetCardFilter;

import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternOutputs;
import appeng.util.Platform;

public class ContainerMagnet extends FCBaseContainer implements IOptionalSlotHost {

    @GuiSync(101)
    public ItemMagnetCard.ListMode listMode;

    @GuiSync(102)
    public boolean nbt;

    @GuiSync(103)
    public boolean meta;

    @GuiSync(104)
    public boolean ore;
    protected SlotFake[] filterSlots;
    protected final IInventory filter;

    public ContainerMagnet(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.filter = ((IItemTerminal) monitorable).getInventoryByName("config");
        this.filterSlots = new SlotFake[27];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(
                        this.filterSlots[x + y * 9] = new SlotPatternOutputs(
                                this.filter,
                                this,
                                x + y * 9,
                                8 + x * 18,
                                58 + y * 18,
                                0,
                                0,
                                1));
            }
        }
        bindPlayerInventory(ip, 0, 126);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int idx) {
        AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx);
        if (clickSlot == null || !clickSlot.getHasStack()) return null;
        ItemStack tis = clickSlot.getStack();
        for (SlotFake slot : this.filterSlots) {
            if (!slot.getHasStack()) {
                slot.putStack(tis);
                detectAndSendChanges();
                break;
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer() && this.getPortableCell() instanceof IWirelessMagnetCardFilter) {
            IWirelessMagnetCardFilter host = (IWirelessMagnetCardFilter) this.getHost();
            this.listMode = host.getListMode();
            this.meta = host.getMetaMode();
            this.nbt = host.getNBTMode();
            this.ore = host.getOreMode();
        }
        super.detectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }
}
