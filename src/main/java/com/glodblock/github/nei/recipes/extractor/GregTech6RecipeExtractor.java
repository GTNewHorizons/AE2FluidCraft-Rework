package com.glodblock.github.nei.recipes.extractor;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import gregapi.item.ItemFluidDisplay;
import gregapi.recipes.Recipe;

public class GregTech6RecipeExtractor implements IRecipeExtractor {

    private final Recipe.RecipeMap Recipes;

    public GregTech6RecipeExtractor(Recipe.RecipeMap aMap) {
        Recipes = aMap;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        this.removeMachine(rawInputs);
        return ExtractorUtil.packItemStack(rawInputs, GregTech6RecipeExtractor::getFluidFromDisplay);
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return ExtractorUtil.packItemStack(rawOutputs, GregTech6RecipeExtractor::getFluidFromDisplay);
    }

    private void removeMachine(List<PositionedStack> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            PositionedStack positionedStack = list.get(i);
            if (positionedStack != null) {
                for (ItemStack machine : this.Recipes.mRecipeMachineList) {
                    if (positionedStack.items[0].isItemEqual(machine)) {
                        list.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public static Object getFluidFromDisplay(PositionedStack stack) {
        if (stack != null) {
            ItemStack item = stack.items[0].copy();
            if (item.getItem() instanceof ItemFluidDisplay) {
                if (item.getTagCompound() != null) {
                    FluidStack fluid = ((ItemFluidDisplay) item.getItem()).getFluid(item);
                    return fluid != null && fluid.amount > 0 ? fluid : null;
                }
            } else {
                return item;
            }
        }
        return null;
    }
}
