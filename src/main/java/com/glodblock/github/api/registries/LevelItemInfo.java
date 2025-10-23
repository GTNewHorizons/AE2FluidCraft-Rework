package com.glodblock.github.api.registries;

import org.jetbrains.annotations.NotNull;

import appeng.api.storage.data.IAEStack;

public class LevelItemInfo {

    public IAEStack<?> stack;
    public long quantity;
    public long batchSize;
    public LevelState state;

    public LevelItemInfo(@NotNull IAEStack<?> stack, long quantity, long batchSize, LevelState state) {
        this.stack = stack;
        this.quantity = quantity;
        this.batchSize = batchSize;
        this.state = state;
    }
}
