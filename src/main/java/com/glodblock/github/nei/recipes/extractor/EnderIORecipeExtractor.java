package com.glodblock.github.nei.recipes.extractor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.nei.object.IRecipeExtractor;
import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.util.Ae2Reflect;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import crazypants.enderio.machine.crusher.CrusherRecipeManager;
import crazypants.enderio.nei.VatRecipeHandler;

public class EnderIORecipeExtractor implements IRecipeExtractor {

    public EnderIORecipeExtractor() {}

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
        List<OrderStack<?>> tmp;
        if (tRecipe.arecipes.get(index) instanceof VatRecipeHandler.InnerVatRecipe vatRecipe) {
            ArrayList<PositionedStack> inputs = ReflectEIO.getInputs(vatRecipe);
            tmp = ExtractorUtil.packItemStack(inputs);
            FluidStack in = ReflectEIO.getInputFluid(vatRecipe);
            if (in != null) {
                tmp.add(new OrderStack<>(in, inputs.size()));
            }
            return tmp;
        } else if (tRecipe.getOverlayIdentifier().equals("EnderIOSagMill")) {
            for (int i = rawInputs.size() - 1; i >= 0; i--) {
                PositionedStack stack = rawInputs.get(i);
                if (stack != null && CrusherRecipeManager.getInstance().isValidSagBall(stack.items[0])) {
                    rawInputs.remove(i);
                    break;
                }
            }
            return getInputIngredients(rawInputs);
        } else {
            return getInputIngredients(rawInputs);
        }
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
            int index) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipe;
        List<OrderStack<?>> tmp = new LinkedList<>();
        if (tRecipe.arecipes.get(index) instanceof VatRecipeHandler.InnerVatRecipe vatRecipe) {
            FluidStack result = ReflectEIO.getResult(vatRecipe);
            if (result != null) {
                tmp.add(new OrderStack<>(result, 0));
            }
            return tmp;
        } else {
            return getOutputIngredients(rawOutputs);
        }
    }

    private static class ReflectEIO {

        private static final Field inputsF;
        private static final Field resultF;
        private static final Field inFluidF;

        static {
            try {
                inputsF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "inputs");
                resultF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "result");
                inFluidF = Ae2Reflect.reflectField(VatRecipeHandler.InnerVatRecipe.class, "inFluid");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Failed to initialize EIO reflection hacks!", e);
            }
        }

        private static ArrayList<PositionedStack> getInputs(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, inputsF);
        }

        private static FluidStack getResult(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, resultF);
        }

        private static FluidStack getInputFluid(VatRecipeHandler.InnerVatRecipe vat) {
            return Ae2Reflect.readField(vat, inFluidF);
        }
    }
}
