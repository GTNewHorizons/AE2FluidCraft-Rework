package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import appeng.api.storage.StorageChannel;
import appeng.parts.misc.PartStorageBus;

public class PartFluidStorageBus extends PartStorageBus {

    public PartFluidStorageBus(ItemStack is) {
        super(is);
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }
}
