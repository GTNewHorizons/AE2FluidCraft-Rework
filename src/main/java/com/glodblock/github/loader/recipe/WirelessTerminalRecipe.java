package com.glodblock.github.loader.recipe;

import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import java.util.Arrays;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class WirelessTerminalRecipe extends ShapelessRecipes {

    private static ItemStack infinityBoosterCard;
    private final ItemStack installedTerm;

    public WirelessTerminalRecipe(ItemStack term) {
        super(term, Arrays.asList(term, getInfinityBoosterCard()));
        this.installedTerm = installInfinityBoosterCard(term);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack card = inv.getStackInSlot(1);
        ItemStack recipeCard = getInfinityBoosterCard();
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasInfinityBoosterCard(term)
                && card != null
                && recipeCard != null
                && card.getItem() == recipeCard.getItem();
    }

    public static ItemStack getInfinityBoosterCard() {
        if (!ModAndClassUtil.WCT) return null;
        if (infinityBoosterCard == null) {
            infinityBoosterCard = GameRegistry.findItemStack("ae2wct", "infinityBoosterCard", 1);
        }
        return infinityBoosterCard == null ? null : infinityBoosterCard.copy();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installInfinityBoosterCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installInfinityBoosterCard(ItemStack is) {
        is = is.copy();
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(ItemBaseWirelessTerminal.infinityBoosterCard, true);
        is.setTagCompound(data);
        return is;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return installedTerm;
    }

}
