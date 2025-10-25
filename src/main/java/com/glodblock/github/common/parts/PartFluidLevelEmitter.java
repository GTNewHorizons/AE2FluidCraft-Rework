package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import appeng.helpers.Reflected;

public class PartFluidLevelEmitter extends PartLevelTerminal {

    @Reflected
    public PartFluidLevelEmitter(final ItemStack is) {
        super(is);
    }
}
