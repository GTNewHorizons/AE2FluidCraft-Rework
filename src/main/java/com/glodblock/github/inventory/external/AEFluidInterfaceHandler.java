package com.glodblock.github.inventory.external;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.util.Platform;

public class AEFluidInterfaceHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable) {
                return !(te instanceof IPartHost);
            } else return getMonitorablePart(te, d, mySrc) != null;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable tsm) {
                final IStorageMonitorable monitorable = tsm.getMonitorable(d, src);
                return monitorable != null ? monitorable.getFluidInventory() : null;
            } else {
                final IStorageMonitorable monitorable = getMonitorablePart(te, d, src);
                return monitorable != null ? monitorable.getFluidInventory() : null;
            }
        }
        return null;
    }

    private static IStorageMonitorable getMonitorablePart(TileEntity te, ForgeDirection d, BaseActionSource src) {
        final IPart part = Platform.getPartFromTE(te, d);
        return part instanceof ITileStorageMonitorable monitorablePart ? monitorablePart.getMonitorable(d, src) : null;
    }
}
