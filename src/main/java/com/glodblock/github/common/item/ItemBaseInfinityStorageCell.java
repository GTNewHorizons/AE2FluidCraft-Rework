package com.glodblock.github.common.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.storage.IStorageFluidCell;

import appeng.api.storage.data.IAEFluidStack;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellUpgrades;

public abstract class ItemBaseInfinityStorageCell extends AEBaseItem implements IStorageFluidCell {

    @Override
    public long getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public abstract IInventory getConfigInventory(ItemStack is);

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
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
