package com.glodblock.github.nei.recipes.extractor;

import java.util.List;

import com.glodblock.github.nei.NEIUtils;
import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public class AvaritiaRecipeExtractor implements IRecipeExtractor {

    public AvaritiaRecipeExtractor() {}

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        return getInputIngredients(rawInputs);
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
            int index) {
        return getOutputIngredients(rawOutputs);
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        return NEIUtils.compress(ExtractorUtil.packItemStack(rawInputs));
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return ExtractorUtil.packItemStack(rawOutputs);
    }
}
