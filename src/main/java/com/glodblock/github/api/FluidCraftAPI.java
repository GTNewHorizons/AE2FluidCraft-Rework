package com.glodblock.github.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.fluids.Fluid;

public final class FluidCraftAPI implements IFluidCraftAPI {

    private static final FluidCraftAPI API = new FluidCraftAPI();

    /**
     * These fluids will not be allowed to be stored in fluid storage cells.
     */
    private final Set<Class<? extends Fluid>> blacklistedFluids = new HashSet<>();

    /**
     * These fluids will not be displayed in a fluid terminal.
     */
    private final Set<Class<? extends Fluid>> blacklistedDispFluids = new HashSet<>();

    public static FluidCraftAPI instance() {
        return API;
    }

    @Override
    public void blacklistFluidInStorage(Class<? extends Fluid> fluid) {
        blacklistedFluids.add(fluid);
    }

    @Override
    public void blacklistFluidInDisplay(Class<? extends Fluid> fluid) {
        blacklistedDispFluids.add(fluid);
    }

    @Override
    public boolean isBlacklistedInStorage(Class<? extends Fluid> fluid) {
        return blacklistedFluids.contains(fluid);
    }

    @Override
    public boolean isBlacklistedInDisplay(Class<? extends Fluid> fluid) {
        return blacklistedDispFluids.contains(fluid);
    }
}
