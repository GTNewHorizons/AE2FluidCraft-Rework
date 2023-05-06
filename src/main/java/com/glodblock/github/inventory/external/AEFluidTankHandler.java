package com.glodblock.github.inventory.external;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.inventory.MEMonitorIFluidHandler;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;

// This handler is too generic, so it will overlap other mods' fluid handler.
// It will be injected into external handler by ASM to make sure it is executed at last.
public class AEFluidTankHandler implements IExternalStorageHandler {

    public static final AEFluidTankHandler INSTANCE = new AEFluidTankHandler();

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        return channel == StorageChannel.FLUIDS && te instanceof IFluidHandler;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src) {
        if (channel == StorageChannel.FLUIDS) {
            return new MEMonitorIFluidHandler((IFluidHandler) te, d);
        }
        return null;
    }
}
