package com.glodblock.github.loader.recipe;

import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import java.util.Arrays;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;

import appeng.util.Platform;

public class WirelessTerminalRecipe extends ShapelessRecipes {

    private static final ItemStack infinityBoosterCard = ItemEnum.BOOSTER_CARD.getStack();
    private final ItemStack installedTerm;

    public WirelessTerminalRecipe(ItemStack term) {
        super(term, Arrays.asList(term, infinityBoosterCard));
        this.installedTerm = installInfinityBoosterCard(term);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack infinityBoosterCard = inv.getStackInSlot(1);
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasInfinityBoosterCard(term)
                && infinityBoosterCard != null
                && infinityBoosterCard.getItem() instanceof ItemInfinityBooster;
    }

    public static ItemStack getInfinityBoosterCard() {
        return infinityBoosterCard.copy();
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
