package com.glodblock.github.crossmod.thaumcraft;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.networking.IGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class ThaumicEnergisticsCrafting {

    public static Item neiAddonAspect, thaumicEnergisticsAspect;

    public static void postInit() {
        neiAddonAspect = GameRegistry.findItem("thaumcraftneiplugin", "Aspect");
        thaumicEnergisticsAspect = GameRegistry.findItem("thaumicenergistics", "crafting.aspect");
    }

    /**
     * Checks if a stack is an aspect preview (nei addon or thaumic energistics). Does not mean that the stack contains
     * an aspect.
     */
    public static boolean isAspectStack(ItemStack stack) {
        if (!ModAndClassUtil.ThE || stack == null) return false;

        return stack.getItem() == neiAddonAspect || stack.getItem() == thaumicEnergisticsAspect;
    }

    @Method(modid = "thaumicenergistics")
    private static @Nullable Aspect getAspect(ItemStack stack) {
        if (stack == null) return null;

        if (stack.getItem() == neiAddonAspect) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null || !(tag.getTag("Aspects") instanceof NBTTagList aspects)) return null;
            if (aspects.tagCount() != 1) return null;
            String aspect = aspects.getCompoundTagAt(0).getString("key");
            if (aspect.isEmpty()) return null;

            return Aspect.getAspect(aspect);
        }

        if (stack.getItem() == thaumicEnergisticsAspect) {
            return Aspect.getAspect(ItemCraftingAspect.getAspect(stack).getTag());
        }

        return null;
    }

    @Method(modid = "thaumicenergistics")
    private static ItemStack getAspectStack(Aspect aspect, int stackSize) {
        return ItemCraftingAspect.createStackForAspect(aspect, stackSize);
    }

    /**
     * Converts an aspect stack into a thaumic energistics stack.
     */
    public static IAEItemStack convertAspectStack(IAEItemStack stack) {
        if (ModAndClassUtil.ThE) {
            return convertAspectStackImpl(stack);
        } else {
            return stack;
        }
    }

    @Method(modid = "thaumicenergistics")
    private static IAEItemStack convertAspectStackImpl(IAEItemStack stack) {
        if (stack == null) return null;

        Aspect aspect = getAspect(stack.getItemStack());

        if (aspect == null) return stack;

        return Objects.requireNonNull(AEItemStack.create(getAspectStack(aspect, 1))).setStackSize(stack.getStackSize());
    }

    /**
     * Gets the amount of essentia stored in a grid for a given aspect preview.
     */
    public static long getEssentiaAmount(IAEItemStack stack, IGrid grid) {
        if (ModAndClassUtil.ThE) {
            return getEssentiaAmountImpl(stack, grid);
        } else {
            return 0;
        }
    }

    @Method(modid = "thaumicenergistics")
    private static long getEssentiaAmountImpl(IAEItemStack stack, IGrid grid) {
        Aspect aspect = getAspect(stack.getItemStack());

        if (aspect == null) return 0;

        IEssentiaGrid essentiaGrid = grid.getCache(IEssentiaGrid.class);

        return essentiaGrid.getEssentiaAmount(aspect);
    }
}
