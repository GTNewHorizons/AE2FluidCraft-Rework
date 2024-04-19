package com.glodblock.github.loader.recipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.ENERGY_CARD;
import static com.glodblock.github.util.Util.Wireless.hasEnergyCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;

import appeng.util.Platform;

public class WirelessTerminalEnergyRecipe extends ShapelessRecipes {

    private final ItemStack installedTerm;
    public static List<ItemStack> energyCards = getEnergyCards();

    public WirelessTerminalEnergyRecipe(ItemStack term, ItemStack energyCard) {
        super(term, Arrays.asList(term, energyCard));
        this.installedTerm = installEnergyCard(term);
    }

    private static List<ItemStack> getEnergyCards() {
        return new ArrayList<ItemStack>() {

            {
                add(ENERGY_CARD.stack());
            }
        };
    }

    private boolean isEnergyCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        for (ItemStack c : energyCards) {
            if (Objects.equals(c.getItem(), is.getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack card = inv.getStackInSlot(1);
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasEnergyCard(term)
                && isEnergyCard(card);
    }

    public static ItemStack getEnergyCard() {
        return ENERGY_CARD.stack();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installEnergyCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installEnergyCard(ItemStack is) {
        is = is.copy();
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(ItemBaseWirelessTerminal.infinityEnergyCard, true);
        is.setTagCompound(data);
        return is;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return installedTerm;
    }
}
