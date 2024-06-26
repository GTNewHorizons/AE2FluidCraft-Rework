package com.glodblock.github.common.item;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBasicFluidStoragePart extends FCBaseItem {

    public static final int types = 8;
    private static final HashMap<Integer, IIcon> icon = new HashMap<>();

    public ItemBasicFluidStoragePart() {
        super();
        setHasSubtypes(true);
        setUnlocalizedName(NameConst.ITEM_FLUID_PART);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < types; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return StatCollector.translateToLocalFormatted(
                "item.fluid_part." + meta + ".name",
                CellType.getTypeColor(meta),
                EnumChatFormatting.RESET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0; i < types; i++) {
            icon.put(i, iconRegister.registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_PART + "." + i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return icon.get(meta);
    }

    @Override
    public ItemBasicFluidStoragePart register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PART, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
