package com.glodblock.github.common.item;

import static com.glodblock.github.util.Util.FluidUtil.water_bucket;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.storage.StorageChannel;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseInfiniteCell;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInfinityWaterStorageCell extends AEBaseInfiniteCell
        implements IRegister<ItemInfinityWaterStorageCell> {

    public static class InfinityConfig extends IAEStackInventory {

        public InfinityConfig(final ItemStack is) {
            super(null, 1);
            this.putAEStackInSlot(0, Util.getAEFluidFromItem(is));
        }

        @Override
        public void markDirty() {}
    }

    public ItemInfinityWaterStorageCell() {
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

    @Override
    public IAEStackInventory getConfigAEInventory(ItemStack is) {
        return new InfinityConfig(water_bucket);
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }
}
