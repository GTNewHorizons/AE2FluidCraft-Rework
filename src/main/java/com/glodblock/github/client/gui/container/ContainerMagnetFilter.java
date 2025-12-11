package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.inventory.item.IWirelessMagnetFilter;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.inventory.item.WirelessMagnetCardFilterInventory;

import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerSubGui;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternOutputs;
import appeng.util.Platform;

public class ContainerMagnetFilter extends ContainerSubGui implements IOptionalSlotHost {

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
    private final IWirelessMagnetFilter object;

    public ContainerMagnetFilter(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.object = (WirelessMagnetCardFilterInventory) monitorable;
        this.filter = object.getInventoryByName("config");
        this.filterSlots = new OptionalSlotFake[27];
        oreDictFilter = object.getOreDictFilter();
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
        this.lockPlayerInventorySlot(this.object.getInventorySlot());
        bindPlayerInventory(ip, 0, 126);
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
        if (Platform.isServer()) {
            this.listMode = this.object.getListMode();
            this.meta = this.object.getMetaMode();
            this.nbt = this.object.getNBTMode();
            this.ore = this.object.getOreMode();
            this.oreDict = this.object.getOreDictMode();
            this.oreDictFilter = this.object.getOreDictFilter();
        }
        super.detectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }
}
