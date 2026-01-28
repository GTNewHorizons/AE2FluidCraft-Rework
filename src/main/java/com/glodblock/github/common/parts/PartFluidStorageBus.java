package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEStackType;
import appeng.parts.misc.PartStorageBus;

public class PartFluidStorageBus extends PartStorageBus {

    public PartFluidStorageBus(ItemStack is) {
        super(is);
    }

    @Override
    public IAEStackType<?> getStackType() {
        return FLUID_STACK_TYPE;
    }
}
