package com.glodblock.github.common.item;

import net.minecraft.item.ItemStack;

import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.base.Optional;

import appeng.api.storage.StorageChannel;
import appeng.items.AEBaseCell;
import appeng.util.Platform;

public abstract class FCBaseItemCell extends AEBaseCell {

    protected CellType component;

    public FCBaseItemCell(long bytes, int perType, int totalType, double drain) {
        super(Optional.of(bytes / 1024 + "k"));
        this.totalBytes = bytes;
        this.perType = perType;
        this.idleDrain = drain;
        this.totalTypes = totalType;
        this.component = null;
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }

    public FCBaseItemCell(Optional subName) {
        super(subName);
    }

    public ItemStack getHousing() {
        return ItemAndBlockHolder.CELL_HOUSING.stack();
    }

    public ItemStack getComponent() {
        return component.stack(1);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public String getCellData(ItemStack is) {
        return totalBytes + ","
                + this.getTotalTypes(null)
                + ","
                + perType
                + ","
                + 8 * 256
                + ","
                + Platform.openNbtData(is).getByte("cellRestrictionTypes")
                + ","
                + Platform.openNbtData(is).getLong("cellRestrictionAmount")
                + ","
                + "fluid";
    }
}
