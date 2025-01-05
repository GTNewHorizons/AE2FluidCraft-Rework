package com.glodblock.github.crossmod.thaumcraft;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
        if (stack == null) return false;

        return stack.getItem() == neiAddonAspect || stack.getItem() == thaumicEnergisticsAspect;
    }

    @Nullable
    @Method(modid = "thaumicenergistics")
    public static String getAspectName(ItemStack stack) {
        if (stack == null) return null;

        if (stack.getItem() == neiAddonAspect) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null || !(tag.getTag("Aspects") instanceof NBTTagList aspects)) return null;
            if (aspects.tagCount() != 1) return null;
            String aspect = aspects.getCompoundTagAt(0).getString("key");
            if (aspect.isEmpty()) return null;

            return aspect;
        }

        if (stack.getItem() == thaumicEnergisticsAspect) {
            return ItemCraftingAspect.getAspect(stack).getTag();
        }

        return null;
    }

    @Method(modid = "thaumicenergistics")
    public static ItemStack getAspectStack(String aspectName, int stackSize) {
        return ItemCraftingAspect.createStackForAspect(Aspect.getAspect(aspectName), stackSize);
    }

    /**
     * Converts an aspect stack into a thaumic energistics stack.
     */
    public static IAEItemStack convertAspectStack(IAEItemStack stack) {
        if (stack == null) return null;

        String aspect = getAspectName(stack.getItemStack());

        if (aspect == null) return stack;

        return Objects.requireNonNull(AEItemStack.create(getAspectStack(aspect, 1))).setStackSize(stack.getStackSize());
    }

    /**
     * Gets the amount of essentia stored in a grid for a given aspect preview.
     */
    @Method(modid = "thaumicenergistics")
    public static long getEssentiaAmount(IAEItemStack stack, IGrid grid) {
        String aspectName = getAspectName(stack.getItemStack());

        if (aspectName == null) return 0;

        Aspect aspect = Aspect.getAspect(aspectName);

        if (aspect == null) return 0;

        IEssentiaGrid essentiaGrid = grid.getCache(IEssentiaGrid.class);

        return essentiaGrid.getEssentiaAmount(aspect);
    }
}
