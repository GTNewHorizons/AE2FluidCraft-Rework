package com.glodblock.github.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemFluidExtremeStorageCell extends FCBaseItemCell
        implements IStorageFluidCell, IRegister<ItemFluidExtremeStorageCell> {

    private final String name;

    public ItemFluidExtremeStorageCell(String name, long bytes, int perType, int totalTypes, double drain) {
        super(bytes, perType, totalTypes, drain);
        setUnlocalizedName(name);
        this.name = name;
        setTextureName(FluidCraft.resource(name).toString());
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        return stack;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return false;
    }

    @Override
    public ItemFluidExtremeStorageCell register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, this.name, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

}
