package com.glodblock.github.common.item;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.api.config.FuzzyMode;
import appeng.core.features.AEFeature;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCreativeFluidStorageCell extends ItemBaseInfinityStorageCell
        implements IRegister<ItemCreativeFluidStorageCell> {

    public ItemCreativeFluidStorageCell() {
        super(com.google.common.base.Optional.absent());
        setUnlocalizedName(NameConst.ITEM_CREATIVE_FLUID_STORAGE);
        setTextureName(FluidCraft.resource(NameConst.ITEM_CREATIVE_FLUID_STORAGE).toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public ItemCreativeFluidStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_CREATIVE_FLUID_STORAGE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
