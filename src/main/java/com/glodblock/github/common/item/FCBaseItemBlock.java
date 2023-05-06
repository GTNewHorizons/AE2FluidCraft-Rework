package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.block.FCBaseBlock;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FCBaseItemBlock extends AEBaseItemBlock {

    private final FCBaseBlock blockType;

    public FCBaseItemBlock(Block id) {
        super(id);
        blockType = (FCBaseBlock) id;
    }

    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {
        blockType.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
        super.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
