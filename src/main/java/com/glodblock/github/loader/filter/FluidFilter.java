package com.glodblock.github.loader.filter;

import static appeng.api.config.TypeFilter.*;

import net.minecraft.item.Item;

import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.TypeFilter;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.me.ItemRepo;

public class FluidFilter {

    public static void installFilter() {
        ItemRepo.registerTypeHandler(FluidFilter::filter, FLUIDS);
    }

    private static boolean filter(IAEStack<?> stack, TypeFilter typeFilter) {
        if (typeFilter == ALL) return true;
        if (stack instanceof IAEItemStack) {
            Item item = ((IAEItemStack) stack).getItem();
            if (item != null) {
                switch (typeFilter) {
                    case ITEMS:
                        return !(item instanceof ItemFluidDrop);
                    case FLUIDS:
                        return item instanceof ItemFluidDrop;
                }
            }
        }
        return true;
    }
}
