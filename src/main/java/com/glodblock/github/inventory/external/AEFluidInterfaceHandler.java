package com.glodblock.github.inventory.external;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.util.Platform;

public class AEFluidInterfaceHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable) {
                return true;
            } else return Platform.getPartFromTE(te, d.getOpposite()) instanceof ITileStorageMonitorable;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable
                    && ((ITileStorageMonitorable) te).getMonitorable(d, src) != null) {
                return ((ITileStorageMonitorable) te).getMonitorable(d, src).getFluidInventory();
            } else if (Platform.getPartFromTE(te, d.getOpposite()) instanceof ITileStorageMonitorable part
                    && part.getMonitorable(d, src) != null) {
                        return part.getMonitorable(d, src).getFluidInventory();
                    }
        }
        return null;
    }
}
