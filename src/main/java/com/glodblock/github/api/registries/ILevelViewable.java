package com.glodblock.github.api.registries;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.glodblock.github.api.ISide;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.ICustomNameObject;

public interface ILevelViewable extends IGridHost, ISide, ISegmentedInventory, ICustomNameObject {

    DimensionalCoord getLocation();

    TileEntity getTile();

    @Deprecated
    default long getSortValue() {
        TileEntity te = getTile();
        return ((long) te.zCoord << 24) ^ ((long) te.xCoord << 8) ^ te.yCoord;
    }

    @Deprecated
    default boolean shouldDisplay() {
        return true;
    }

    /**
     * Number of rows to expect. This is used with {@link #rowSize()} to determine how to render the slots.
     */
    default int rows() {
        return 1;
    };

    /**
     * Number of slots per row.
     */
    default int rowSize() {
        return 1;
    };

    @Deprecated
    default ItemStack getSelfItemStack() {
        return null;
    }

    /**
     * "Target" Display representation
     */
    @Deprecated
    default ItemStack getDisplayItemStack() {
        return null;
    }

    LevelItemInfo[] getLevelItemInfoList();

}
