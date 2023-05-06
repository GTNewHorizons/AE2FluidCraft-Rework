package com.glodblock.github.common.item;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.parts.PartFluidImportBus;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidImportBus extends FCBaseItem implements IPartItem {

    public ItemFluidImportBus() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_IMPORT);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartFluidImportBus createPartFromItemStack(ItemStack is) {
        return new PartFluidImportBus(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemFluidImportBus register() {
        if (!Config.fluidIOBus) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_IMPORT, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public void registerIcons(IIconRegister _iconRegister) {}

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
