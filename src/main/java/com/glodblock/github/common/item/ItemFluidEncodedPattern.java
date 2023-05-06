package com.glodblock.github.common.item;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.NameConst;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemFluidEncodedPattern extends ItemEncodedPattern implements IRegister<ItemFluidEncodedPattern> {

    public ItemFluidEncodedPattern() {
        super();
        this.setUnlocalizedName(NameConst.ITEM_FLUID_ENCODED_PATTERN);
        this.setTextureName(FluidCraft.MODID + ":" + NameConst.ITEM_FLUID_ENCODED_PATTERN);
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        FluidPatternDetails pattern = new FluidPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    @Override
    public ItemFluidEncodedPattern register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_ENCODED_PATTERN, FluidCraft.MODID);
        return this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }
}
