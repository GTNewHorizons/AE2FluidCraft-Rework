package com.glodblock.github.nei.recipes.extractor;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import gregtech.api.enums.ItemList;
import gregtech.common.items.GT_FluidDisplayItem;

public class GregTech5RecipeExtractor implements IRecipeExtractor {

    boolean removeSpecial;

    public GregTech5RecipeExtractor(boolean removeSpecial) {
        this.removeSpecial = removeSpecial;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        if (removeSpecial) removeSpecial(rawInputs);
        return ExtractorUtil.packItemStack(rawInputs, GregTech5RecipeExtractor::getFluidFromDisplay);
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return ExtractorUtil.packItemStack(rawOutputs, GregTech5RecipeExtractor::getFluidFromDisplay);
    }

    private void removeSpecial(List<PositionedStack> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            PositionedStack positionedStack = list.get(i);
            if (positionedStack != null && (positionedStack.items[0].isItemEqual(ItemList.Tool_DataStick.get(1))
                    || positionedStack.items[0].isItemEqual(ItemList.Tool_DataOrb.get(1)))) {
                list.remove(i);
                break;
            }
        }
    }

    public static Object getFluidFromDisplay(PositionedStack stack) {
        if (stack != null) {
            ItemStack item = stack.items[0].copy();
            if (item.getItem() instanceof GT_FluidDisplayItem) {
                if (item.getTagCompound() != null) {
                    Fluid fluid = FluidRegistry.getFluid(item.getItemDamage());
                    int amt = (int) item.getTagCompound().getLong("mFluidDisplayAmount");
                    return amt > 0 && fluid != null ? new FluidStack(fluid, amt) : null;
                }
            } else {
                return item;
            }
        }
        return null;
    }
}
