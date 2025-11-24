package com.glodblock.github.common.item;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.api.storage.StorageChannel;
import appeng.items.storage.ItemVoidStorageCell;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemFluidVoidStorageCell extends ItemVoidStorageCell implements IRegister<ItemFluidVoidStorageCell> {

    public ItemFluidVoidStorageCell() {
        super();
        setUnlocalizedName(NameConst.ITEM_FLUID_VOID_CELL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_FLUID_VOID_CELL).toString());
    }

    @Override
    public ItemFluidVoidStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_VOID_CELL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }
}
