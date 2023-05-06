package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.crossmod.extracells.ProxyItem;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;

public class ProxyFluidStorageCell extends ProxyItem implements IStorageFluidCell {

    public ProxyFluidStorageCell(String ec2itemName) {
        super(ec2itemName);
    }

    @Override
    public long getBytes(ItemStack cellItem) {
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
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        if (Config.blacklistEssentiaGas && ModAndClassUtil.ThE && requestedAddition != null) {
            return ModAndClassUtil.essentiaGas.isInstance(requestedAddition.getFluid());
        }
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

    }
}
