package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.inventory.item.IItemTerminal;
import com.glodblock.github.inventory.item.IWirelessMagnetFilter;
import com.glodblock.github.inventory.item.WirelessMagnet;

import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternOutputs;
import appeng.util.Platform;

public class ContainerMagnetFilter extends FCBaseContainer implements IOptionalSlotHost {

    @GuiSync(101)
    public WirelessMagnet.ListMode listMode;

    @GuiSync(102)
    public boolean nbt;

    @GuiSync(103)
    public boolean meta;

    @GuiSync(104)
    public boolean ore;

    @GuiSync(105)
    public boolean oreDict;

    @GuiSync(106)
    public String oreDictFilter;

    protected OptionalSlotFake[] filterSlots;
    protected final IInventory filter;

    public ContainerMagnetFilter(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.filter = ((IItemTerminal) monitorable).getInventoryByName("config");
        this.filterSlots = new OptionalSlotFake[27];
        oreDictFilter = ((IWirelessMagnetFilter) getHost()).getOreDictFilter();
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
                this.filterSlots[x + y * 9].setRenderDisabled(false);
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
        if (Platform.isServer() && this.getPortableCell() instanceof IWirelessMagnetFilter) {
            IWirelessMagnetFilter host = (IWirelessMagnetFilter) this.getHost();
            this.listMode = host.getListMode();
            this.meta = host.getMetaMode();
            this.nbt = host.getNBTMode();
            this.ore = host.getOreMode();
            this.oreDict = host.getOreDictMode();
            this.oreDictFilter = host.getOreDictFilter();
        }
        super.detectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }
}
