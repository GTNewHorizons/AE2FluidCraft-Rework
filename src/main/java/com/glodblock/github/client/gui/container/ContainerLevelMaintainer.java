package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;

import appeng.container.AEBaseContainer;
import appeng.util.item.AEItemStack;

public class ContainerLevelMaintainer extends AEBaseContainer {

    private final TileLevelMaintainer tile;
    private final SlotFluidConvertingFake[] requestSlots = new SlotFluidConvertingFake[TileLevelMaintainer.REQ_COUNT];

    public ContainerLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(ipl, tile);
        this.tile = tile;
        AeItemStackHandler request = new AeItemStackHandler(tile.getRequestSlots());
        for (int y = 0; y < TileLevelMaintainer.REQ_COUNT; y++) {
            SlotFluidConvertingFake slot = new SlotFluidConvertingFake(request, y, 27, 20 + y * 19);
            addSlotToContainer(slot);
            requestSlots[y] = slot;
        }
        bindPlayerInventory(ipl, 0, 130);
    }

    public TileLevelMaintainer getTile() {
        return tile;
    }

    public SlotFluidConvertingFake[] getRequestSlots() {
        return this.requestSlots;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        for (SlotFluidConvertingFake slot : this.getRequestSlots()) {
            if (!slot.getHasStack()) {
                slot.setAeStack(AEItemStack.create(((Slot) this.inventorySlots.get(idx)).getStack()), true);
                break;
            }
        }
        this.detectAndSendChanges();
        return null;
    }
}
