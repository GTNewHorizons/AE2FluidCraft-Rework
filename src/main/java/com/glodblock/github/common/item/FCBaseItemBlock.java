package com.glodblock.github.common.item;

import net.minecraft.block.Block;

import com.glodblock.github.common.block.FCBaseBlock;

import appeng.block.AEBaseItemBlock;

public class FCBaseItemBlock extends AEBaseItemBlock {

    private final FCBaseBlock blockType;

    public FCBaseItemBlock(Block id) {
        super(id);
        blockType = (FCBaseBlock) id;
    }
}
