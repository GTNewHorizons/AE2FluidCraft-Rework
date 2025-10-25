package com.glodblock.github.common.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.base.Optional;

import appeng.items.contents.CellUpgrades;

public abstract class ItemBaseInfinityStorageCell extends FCBaseItemCell {

    public ItemBaseInfinityStorageCell(Optional subName) {
        super(subName);
    }

    @Override
    public long getBytesLong(final ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }
}
