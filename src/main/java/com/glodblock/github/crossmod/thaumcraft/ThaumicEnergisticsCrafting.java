package com.glodblock.github.crossmod.thaumcraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.registry.GameRegistry;

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
        if (AEStackTypeRegistry.getType("essentia") == null) return stack;

        if (stack instanceof IAEItemStack ais && isAspectStack(ais.getItemStack())) {
            String aspectTag = getAspectTag(ais.getItemStack());
            if (aspectTag == null || aspectTag.isEmpty()) return stack;

            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("StackType", "essentia");
            tag.setString("AspectTag", aspectTag);
            tag.setLong("Cnt", stack.getStackSize());
            return IAEStack.fromNBTGeneric(tag);
        } else {
            return stack;
        }
    }

    private static String getAspectTag(ItemStack stack) {
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
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("Aspect")) {
                return tag.getString("Aspect");
            }
        }

        return null;
    }
}
