package com.glodblock.github.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.util.Platform;

public abstract class PartGuiFactory<T> extends TileGuiFactory<T> {

    PartGuiFactory(Class<T> invClass) {
        super(invClass);
    }

    @Nullable
    @Override
    protected T getInventory(TileEntity tile, ForgeDirection face) {
        IPart part = Platform.getPartFromTE(tile, face);
        if (invClass.isInstance(part)) {
            return invClass.cast(part);
        }

        return null;
    }
}
