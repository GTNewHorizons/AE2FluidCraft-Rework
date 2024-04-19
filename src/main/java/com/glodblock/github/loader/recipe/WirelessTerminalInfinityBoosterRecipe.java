package com.glodblock.github.loader.recipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.QUANTUM_BRIDGE_CARD;
import static com.glodblock.github.util.Util.Wireless.hasInfinityBoosterCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.util.Platform;

public class WirelessTerminalInfinityBoosterRecipe extends ShapelessRecipes {

    private final ItemStack installedTerm;
    public static List<ItemStack> infCards = getInfinityCards();

    private static List<ItemStack> getInfinityCards() {
        List<ItemStack> list = new ArrayList<ItemStack>() {

            {
                add(QUANTUM_BRIDGE_CARD.stack());
            }
        };
        if (ModAndClassUtil.WCT) {
            list.add(ItemEnum.BOOSTER_CARD.getStack());
        }
        return list;
    }

    public boolean isInfinityCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        for (ItemStack c : infCards) {
            if (Objects.equals(c.getItem(), is.getItem())) {
                return true;
            }
        }
        return false;
    }

    public WirelessTerminalInfinityBoosterRecipe(ItemStack term, ItemStack infinityBoosterCard) {
        super(term, Arrays.asList(term, infinityBoosterCard));
        this.installedTerm = installInfinityBoosterCard(term);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack infinityBoosterCard = inv.getStackInSlot(1);
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasInfinityBoosterCard(term)
                && isInfinityCard(infinityBoosterCard);
    }

    public static ItemStack getInfinityBoosterCard() {
        return ItemAndBlockHolder.QUANTUM_BRIDGE_CARD.stack();
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
