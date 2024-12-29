package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.block.BaseBlockContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BaseItemBlockContainer extends ItemBlock {

    private final BaseBlockContainer blockType;

    public BaseItemBlockContainer(Block id) {
        super(id);
        this.blockType = (BaseBlockContainer) id;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {
        blockType.addInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
