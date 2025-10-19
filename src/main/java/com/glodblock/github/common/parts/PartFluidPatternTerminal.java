package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.base.FCPart;

import appeng.core.sync.GuiBridge;

public class PartFluidPatternTerminal extends FCPart {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    public PartFluidPatternTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public GuiBridge getGui() {
        return null;
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }
}
