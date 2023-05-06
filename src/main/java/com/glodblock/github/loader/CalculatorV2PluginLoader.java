package com.glodblock.github.loader;

import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.v2.CraftingCalculations;
import appeng.crafting.v2.CraftingRequest;
import appeng.util.Platform;

public class CalculatorV2PluginLoader {

    private static long adjustByteCost(CraftingRequest<IAEItemStack> request, Long originalAmount) {
        if (request.stack.getItem() instanceof ItemFluidDrop) {
            return Platform.ceilDiv(originalAmount, 1000L);
        } else {
            return originalAmount;
        }
    }

    public static void installCalculatorV2Plugins() {
        CraftingCalculations.registerByteAmountAdjuster(CalculatorV2PluginLoader::adjustByteCost, IAEItemStack.class);
    }
}
