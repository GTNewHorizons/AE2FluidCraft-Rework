package com.glodblock.github.api.registries;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class LevelItemInfo {

    public ItemStack stack;
    public long quantity;
    public long batchSize;
    public LevelState state;

    public LevelItemInfo(@NotNull ItemStack stack, long quantity, long batchSize, LevelState state) {
        this.stack = stack;
        this.quantity = quantity;
        this.batchSize = batchSize;
        this.state = state;
    }
}
