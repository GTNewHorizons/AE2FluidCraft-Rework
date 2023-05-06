package com.glodblock.github.crossmod.extracells.storage;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.crossmod.extracells.ProxyItem;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;

/**
 * Proxy Item Storage. 256K, 1024K, etc. -> AE2's version. For now its just mirroring AE2's version, will need to
 * transition to something better.
 */
public class ProxyItemStorageCell extends ProxyItem implements IStorageCell {

    public ProxyItemStorageCell(String ec2itemName) {
        super(ec2itemName);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        if (cellItem == null) {
            return 0;
        }
        int meta = cellItem.getItemDamage();
        if (replacements.containsKey(meta)) {
            ProxyItemEntry entry = replacements.get(meta);
            if (entry instanceof ProxyStorageEntry) {
                return (int) ((ProxyStorageEntry) entry).maxBytes;
            }
        }
        return 0;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        if (cellItem == null) {
            return 0;
        }
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
    public int getBytesPerType(ItemStack cellItem) {
        if (cellItem == null) {
            return 0;
        }
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
    public long getBytesLong(ItemStack cellItem) {
        if (cellItem == null) {
            return 0;
        }
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
    public int getTotalTypes(ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return false;
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
    public double getIdleDrain(@Nullable ItemStack cellItem) {
        if (cellItem == null) {
            return 0.0;
        }
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
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
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
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public String getOreFilter(ItemStack is) {
        return Platform.openNbtData(is).getString("OreFilter");
    }
}
