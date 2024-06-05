package com.glodblock.github.loader.recipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.QUANTUM_BRIDGE_CARD;
import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;

import appeng.util.Platform;

public class WirelessTerminalQuantumBridgeRecipe extends ShapelessRecipes {

    private final ItemStack installedTerm;
    public static ItemStack quantumBridgeCard = QUANTUM_BRIDGE_CARD.stack();

    public boolean isQuantumBridgeCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return Objects.equals(quantumBridgeCard.getItem(), is.getItem());
    }

    public WirelessTerminalQuantumBridgeRecipe(ItemStack term) {
        super(term, Arrays.asList(term, getQuantumBridgeCard()));
        this.installedTerm = installQuantumBridgeCard(term);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack infinityBoosterCard = inv.getStackInSlot(1);
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasInfinityBoosterCard(term)
                && isQuantumBridgeCard(infinityBoosterCard);
    }

    public static ItemStack getQuantumBridgeCard() {
        return quantumBridgeCard;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installQuantumBridgeCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installQuantumBridgeCard(ItemStack is) {
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
