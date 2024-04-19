package com.glodblock.github.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEnergyCard extends FCBaseItem {

    public ItemEnergyCard() {
        super();
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_ENERGY_CARD);
        this.setTextureName(FluidCraft.resource(NameConst.ITEM_ENERGY_CARD).toString());
    }

    @Override
    public ItemEnergyCard register() {
        GameRegistry.registerItem(this, NameConst.ITEM_ENERGY_CARD, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List toolTip, boolean flag) {
        if (isShiftKeyDown()) {
            toolTip.addAll(RenderUtil.listFormattedStringToWidth(NameConst.i18n(NameConst.TT_ENERGY_CARD_DESC)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
