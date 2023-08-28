package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import com.glodblock.github.inventory.item.IWirelessPatternTerminal;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class WirelessFluidPatternTerminalPatterns extends AppEngInternalInventory {

    private final ItemStack is;
    private final IWirelessPatternTerminal terminal;

    public WirelessFluidPatternTerminalPatterns(final ItemStack is, final IWirelessPatternTerminal term) {
        super(null, 2);
        this.is = is;
        this.terminal = term;
        this.readFromNBT(Platform.openNbtData(is), "pattern");

    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), "pattern");
    }

    @Override
    public void markDirty(int slotIndex) {
        this.markDirty();
    }

    @Override
    public void setInventorySlotContents(final int slot, final ItemStack newItemStack) {
        final ItemStack oldStack = this.inv[slot];
        this.inv[slot] = newItemStack;

        if (this.eventsEnabled()) {
            ItemStack removed = oldStack;
            ItemStack added = newItemStack;

            if (oldStack != null && newItemStack != null && Platform.isSameItemPrecise(oldStack, newItemStack)) {
                if (oldStack.stackSize > newItemStack.stackSize) {
                    removed = removed.copy();
                    removed.stackSize -= newItemStack.stackSize;
                    added = null;
                } else if (oldStack.stackSize < newItemStack.stackSize) {
                    added = added.copy();
                    added.stackSize -= oldStack.stackSize;
                    removed = null;
                } else {
                    removed = added = null;
                }
            }
            this.terminal.onChangeInventory(this, slot, InvOperation.setInventorySlotContents, removed, added);
        }

        this.markDirty();
    }
}
