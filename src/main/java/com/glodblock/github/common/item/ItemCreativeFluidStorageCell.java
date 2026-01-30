package com.glodblock.github.common.item;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.api.storage.StorageChannel;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseInfiniteCell;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCreativeFluidStorageCell extends AEBaseInfiniteCell
        implements IRegister<ItemCreativeFluidStorageCell> {

    public ItemCreativeFluidStorageCell() {
        setUnlocalizedName(NameConst.ITEM_CREATIVE_FLUID_STORAGE);
        setTextureName(FluidCraft.resource(NameConst.ITEM_CREATIVE_FLUID_STORAGE).toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
    }

    @Override
    public ItemCreativeFluidStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_CREATIVE_FLUID_STORAGE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 63;
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }
}
