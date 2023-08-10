package com.glodblock.github.crossmod.waila.gregtech5;

import net.minecraft.item.ItemStack;

import com.glodblock.github.loader.ItemAndBlockHolder;

import gregtech.api.util.GT_ModHandler;

public class RecyclerBlacklist {

    public static void run() {
        GT_ModHandler.addToRecyclerBlackList(new ItemStack(ItemAndBlockHolder.DROP));
        GT_ModHandler.addToRecyclerBlackList(new ItemStack(ItemAndBlockHolder.PACKET));
    }
}
