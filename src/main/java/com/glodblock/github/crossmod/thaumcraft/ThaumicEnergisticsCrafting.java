package com.glodblock.github.crossmod.thaumcraft;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

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

    public static IAEStack<?> convertItemAspectStack(IAEStack<?> stack) {
        if (stack instanceof IAEItemStack ais && isAspectStack(ais.getItemStack())) {
            Aspect aspect = getAspect(ais.getItemStack());
            if (aspect == null) return stack;

            return new AEEssentiaStack(aspect, stack.getStackSize());
        } else {
            return stack;
        }
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
}
