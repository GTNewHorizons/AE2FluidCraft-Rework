package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.base.FCPart;

import appeng.core.sync.GuiBridge;

public class PartFluidTerminal extends FCPart {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidTerminal_Dark;

    public PartFluidTerminal(ItemStack is) {
        super(is, true);
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

    @Override
    public boolean isBooting() {
        return super.isBooting();
    }

    @Override
    public GuiBridge getGui() {
        return GuiBridge.GUI_ME;
    }
}
