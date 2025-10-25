package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.crossmod.extracells.ProxyItem;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;

public class ProxyFluidStorageCell extends ProxyItem implements IStorageCell<IAEFluidStack> {

    public ProxyFluidStorageCell(String ec2itemName) {
        super(ec2itemName);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return Math.toIntExact(getBytesLong(cellItem));
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        int meta = cellItem.getItemDamage();
        if (replacements.containsKey(meta)) {
            ProxyItemEntry entry = replacements.get(meta);
            if (entry instanceof ProxyStorageEntry) {
                return ((ProxyStorageEntry) entry).maxBytes;
            }
        }
        return 0;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        int meta = cellItem.getItemDamage();
        if (replacements.containsKey(meta)) {
            ProxyItemEntry entry = replacements.get(meta);
            if (entry instanceof ProxyStorageEntry) {
                return ((ProxyStorageEntry) entry).bytesPerType;
            }
        }
        return 0;
    }

    @Override
    public boolean isBlackListed(IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

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
        int meta = is.getItemDamage();
        if (replacements.containsKey(meta)) {
            ProxyItemEntry entry = replacements.get(meta);
            if (entry instanceof ProxyStorageEntry) {
                return ((ProxyStorageEntry) entry).idleDrain;
            }
        }
        return 0;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 5;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IAEStackInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {

    }
}
