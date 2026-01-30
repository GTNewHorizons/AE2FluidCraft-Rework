package com.glodblock.github.common.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;

public class FluidCraftingTabs extends CreativeTabs {

    public static final FluidCraftingTabs INSTANCE = new FluidCraftingTabs(FluidCraft.MODID);

    public FluidCraftingTabs(String name) {
        super(name);
    }

    @Override
    public Item getTabIconItem() {
        return ItemAndBlockHolder.WALRUS.stack().getItem();
    }
}
