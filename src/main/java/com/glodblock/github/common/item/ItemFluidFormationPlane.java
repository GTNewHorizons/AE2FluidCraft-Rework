package com.glodblock.github.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.PartFluidFormationPlane;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidFormationPlane extends FCBaseItem implements IPartItem {

    public ItemFluidFormationPlane() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_FORMATION_PLANE);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(stack, x, y, z, side, player, world);
    }

    @Override
    public ItemFluidFormationPlane register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_FORMATION_PLANE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public IIcon getIconIndex(ItemStack p_77650_1_) {
        return FCPartsTexture.PartFluidFormationPlane.getIcon();
    }

    public void registerIcons(IIconRegister _iconRegister) {}

    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public PartFluidFormationPlane createPartFromItemStack(ItemStack is) {
        return new PartFluidFormationPlane(is);
    }
}
