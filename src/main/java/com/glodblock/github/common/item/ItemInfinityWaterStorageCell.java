package com.glodblock.github.common.item;

import static com.glodblock.github.util.Util.FluidUtil.water_bucket;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInfinityWaterStorageCell extends ItemBaseInfinityStorageCell
        implements IStorageFluidCell, IRegister<ItemInfinityWaterStorageCell> {

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new InfinityConfig(water_bucket);
    }

    public ItemInfinityWaterStorageCell() {
        super();
        setUnlocalizedName(NameConst.ITEM_INFINITY_WATER_FLUID_STORAGE);
        setTextureName(FluidCraft.resource(NameConst.ITEM_INFINITY_FLUID_STORAGE).toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
    }

    @Override
    public ItemInfinityWaterStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_INFINITY_WATER_FLUID_STORAGE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
