package com.glodblock.github.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartFluidP2PInterface extends FCBaseItem implements IPartItem {

    private IIcon icon;

    public ItemPartFluidP2PInterface() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_P2P_INTERFACE);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        return new PartFluidP2PInterface(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemPartFluidP2PInterface register() {
        if (!Config.fluidP2PInterface) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_P2P_INTERFACE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public IIcon getIconFromDamage(int dmg) {
        return icon;
    }

    @Override
    public void registerIcons(IIconRegister register) {
        if (Config.fluidP2PInterface) {
            this.icon = register.registerIcon("appliedenergistics2:ItemPart.P2PTunnel");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
