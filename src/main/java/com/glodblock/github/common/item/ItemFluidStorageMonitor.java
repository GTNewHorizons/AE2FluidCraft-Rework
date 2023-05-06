package com.glodblock.github.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.parts.PartFluidStorageMonitor;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidStorageMonitor extends FCBaseItem implements IPartItem {

    public ItemFluidStorageMonitor() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_STORAGE_MONITOR);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemFluidStorageMonitor register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_STORAGE_MONITOR, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public void registerIcons(IIconRegister _iconRegister) {}

    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public PartFluidStorageMonitor createPartFromItemStack(ItemStack is) {
        return new PartFluidStorageMonitor(is);
    }
}
