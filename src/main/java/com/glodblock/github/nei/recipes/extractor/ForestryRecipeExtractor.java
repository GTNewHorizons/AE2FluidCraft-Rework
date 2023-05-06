package com.glodblock.github.nei.recipes.extractor;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.nei.object.IRecipeExtractorLegacy;
import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import forestry.core.recipes.nei.PositionedFluidTank;
import forestry.core.recipes.nei.RecipeHandlerBase;
import forestry.factory.recipes.nei.NEIHandlerFabricator;
import forestry.factory.recipes.nei.NEIHandlerSqueezer;

public class ForestryRecipeExtractor implements IRecipeExtractorLegacy {

    private final IRecipeHandler handler;

    public ForestryRecipeExtractor(IRecipeHandler recipes) {
        handler = recipes;
    }

    @Override
    public String getClassName() {
        return handler.getClass().getName();
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        return ExtractorUtil.packItemStack(rawInputs);
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return ExtractorUtil.packItemStack(rawOutputs);
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        List<OrderStack<?>> tmp = new ArrayList<>();
        if (tRecipe.arecipes.get(index) instanceof RecipeHandlerBase.CachedBaseRecipe) {
            tmp = getInputIngredients(rawInputs);
            List<PositionedFluidTank> tanks = ((RecipeHandlerBase.CachedBaseRecipe) tRecipe.arecipes.get(index))
                    .getFluidTanks();
            if (tanks.size() > 0 && !(handler instanceof NEIHandlerSqueezer)) {
                FluidStack fluid = tanks.get(0).tank.getFluid();
                if (fluid != null) {
                    tmp.add(new OrderStack<>(fluid, rawInputs.size()));
                }
            }
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
            int index) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        removeGlass(rawOutputs);
        List<OrderStack<?>> tmp = new ArrayList<>();
        if (tRecipe.arecipes.get(index) instanceof RecipeHandlerBase.CachedBaseRecipe) {
            tmp = getOutputIngredients(rawOutputs);
            List<PositionedFluidTank> tanks = ((RecipeHandlerBase.CachedBaseRecipe) tRecipe.arecipes.get(index))
                    .getFluidTanks();
            if (tanks.size() > 0 && handler instanceof NEIHandlerSqueezer) {
                FluidStack fluid = tanks.get(0).tank.getFluid();
                if (fluid != null) {
                    tmp.add(new OrderStack<>(fluid, rawOutputs.size()));
                }
            } else if (tanks.size() > 1) {
                FluidStack fluid = tanks.get(1).tank.getFluid();
                if (fluid != null) {
                    tmp.add(new OrderStack<>(fluid, rawOutputs.size()));
                }
            }
        }
        return tmp;
    }

    private void removeGlass(List<PositionedStack> list) {
        if (handler instanceof NEIHandlerFabricator) {
            list.remove(list.size() - 1);
        }
    }
}
