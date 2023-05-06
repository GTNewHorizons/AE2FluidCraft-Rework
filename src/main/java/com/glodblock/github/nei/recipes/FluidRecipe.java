package com.glodblock.github.nei.recipes;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.IRecipeExtractorLegacy;
import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public final class FluidRecipe {

    private static final HashMap<String, IRecipeExtractor> IdentifierMap = new HashMap<>();
    private static final HashMap<String, IRecipeExtractor> IdentifierMapLegacy = new HashMap<>();

    public static void addRecipeMap(String recipeIdentifier, IRecipeExtractor extractor) {
        IdentifierMap.put(recipeIdentifier, extractor);
        if (recipeIdentifier == null && extractor instanceof IRecipeExtractorLegacy) {
            IdentifierMapLegacy.put(((IRecipeExtractorLegacy) extractor).getClassName(), extractor);
        }
    }

    public static List<OrderStack<?>> getPackageInputs(IRecipeHandler recipe, int index, boolean priority) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        if (tRecipe == null) return new ArrayList<>();
        if (!IdentifierMap.containsKey(tRecipe.getOverlayIdentifier())) return getDefaultPackageInputs(tRecipe, index);
        if (tRecipe.getOverlayIdentifier() == null) return getPackageInputsLegacy(recipe, index);
        IRecipeExtractor extractor = IdentifierMap.get(tRecipe.getOverlayIdentifier());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(tRecipe.getIngredientStacks(index));
        List<OrderStack<?>> out = extractor.getInputIngredients(tmp, recipe, index);
        if (priority) {
            List<OrderStack<?>> reordered = new ArrayList<>();
            byte numFluids = 0;
            for (OrderStack<?> orderStack : out) {
                if (orderStack != null && orderStack.getStack() instanceof FluidStack) {
                    reordered.add(new OrderStack<>(orderStack.getStack(), numFluids++));
                }
            }
            for (OrderStack<?> orderStack : out) {
                if (orderStack != null && orderStack.getStack() instanceof ItemStack) {
                    reordered.add(new OrderStack<>(orderStack.getStack(), numFluids++));
                }
            }
            return reordered;
        }
        return out;
    }

    private static List<OrderStack<?>> getDefaultPackageInputs(TemplateRecipeHandler tRecipe, int index) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        AtomicInteger i = new AtomicInteger(0);
        tRecipe.getIngredientStacks(index)
                .forEach(ps -> tmp.add(new OrderStack<>(ps.item, i.getAndIncrement(), ps.items)));
        return tmp;
    }

    private static List<OrderStack<?>> getDefaultPackageOutputs(TemplateRecipeHandler tRecipe, int index) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        AtomicInteger i = new AtomicInteger(0);
        tmp.add(new OrderStack<>(tRecipe.getResultStack(index).item, i.getAndIncrement()));
        tRecipe.getOtherStacks(index).forEach(ps -> tmp.add(new OrderStack<>(ps.item, i.getAndIncrement())));
        return tmp;
    }

    public static List<OrderStack<?>> getPackageOutputs(IRecipeHandler recipe, int index, boolean useOther) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        if (tRecipe == null) return new ArrayList<>();
        if (!IdentifierMap.containsKey(tRecipe.getOverlayIdentifier())) return getDefaultPackageOutputs(tRecipe, index);
        if (tRecipe.getOverlayIdentifier() == null) return getPackageOutputsLegacy(recipe, index, useOther);
        IRecipeExtractor extractor = IdentifierMap.get(tRecipe.getOverlayIdentifier());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(Collections.singleton(tRecipe.getResultStack(index)));
        if (useOther) tmp.addAll(tRecipe.getOtherStacks(index));
        return extractor.getOutputIngredients(tmp, recipe, index);
    }

    public static List<String> getSupportRecipes() {
        return new ArrayList<>(IdentifierMap.keySet());
    }

    public static List<OrderStack<?>> getPackageInputsLegacy(IRecipeHandler recipe, int index) {
        if (recipe == null || !IdentifierMapLegacy.containsKey(recipe.getClass().getName())) return new ArrayList<>();
        IRecipeExtractor extractor = IdentifierMapLegacy.get(recipe.getClass().getName());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(recipe.getIngredientStacks(index));
        return extractor.getInputIngredients(tmp, recipe, index);
    }

    public static List<OrderStack<?>> getPackageOutputsLegacy(IRecipeHandler recipe, int index, boolean useOther) {
        if (recipe == null || !IdentifierMapLegacy.containsKey(recipe.getClass().getName())) return new ArrayList<>();
        IRecipeExtractor extractor = IdentifierMapLegacy.get(recipe.getClass().getName());
        if (extractor == null) return new ArrayList<>();
        List<PositionedStack> tmp = new ArrayList<>(Collections.singleton(recipe.getResultStack(index)));
        if (useOther) tmp.addAll(recipe.getOtherStacks(index));
        return extractor.getOutputIngredients(tmp, recipe, index);
    }
}
