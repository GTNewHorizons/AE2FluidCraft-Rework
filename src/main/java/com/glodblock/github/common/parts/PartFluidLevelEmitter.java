package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import appeng.helpers.Reflected;
import appeng.parts.automation.PartLevelEmitter;

public class PartFluidLevelEmitter extends PartLevelEmitter {

    @Reflected
    public PartFluidLevelEmitter(final ItemStack is) {
        super(is);
    }
}
