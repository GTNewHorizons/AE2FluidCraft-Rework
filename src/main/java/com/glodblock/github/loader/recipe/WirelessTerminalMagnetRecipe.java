package com.glodblock.github.loader.recipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.MAGNET_CARD;
import static com.glodblock.github.util.Util.Wireless.hasMagnetCard;

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

public class WirelessTerminalMagnetRecipe extends ShapelessRecipes {

    private final ItemStack installedTerm;
    public static List<ItemStack> magnetCards = getMagnetCards();

    public WirelessTerminalMagnetRecipe(ItemStack term, ItemStack magnetCard) {
        super(term, Arrays.asList(term, magnetCard));
        this.installedTerm = installMagnetCard(term);
    }

    private static List<ItemStack> getMagnetCards() {
        List<ItemStack> list = new ArrayList<ItemStack>() {

            {
                add(MAGNET_CARD.stack());
            }
        };
        if (ModAndClassUtil.WCT) {
            list.add(ItemEnum.MAGNET_CARD.getStack());
        }
        return list;
    }

    private boolean isMagnetCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        for (ItemStack c : magnetCards) {
            if (Objects.equals(c.getItem(), is.getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack magnetCard = inv.getStackInSlot(1);
        return term != null && term.getItem() instanceof ItemBaseWirelessTerminal
                && !hasMagnetCard(term)
                && isMagnetCard(magnetCard);
    }

    public static ItemStack getMagnetCard() {
        return ItemAndBlockHolder.MAGNET_CARD.stack();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installMagnetCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installMagnetCard(ItemStack is) {
        is = is.copy();
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(ItemBaseWirelessTerminal.magnetCard, true);
        is.setTagCompound(data);
        return is;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return installedTerm;
    }
}
