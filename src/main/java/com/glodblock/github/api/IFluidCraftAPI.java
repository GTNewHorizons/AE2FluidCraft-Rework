package com.glodblock.github.api;

import net.minecraftforge.fluids.Fluid;

@SuppressWarnings("unused")
public interface IFluidCraftAPI {

    /**
     * Blacklisted fluids will not be allowed to be added to a storage cell.
     */
    void blacklistFluidInStorage(Class<? extends Fluid> fluid);

    /**
     * Blacklisted fluids will not be displayed in fluid terminals.
     */
    void blacklistFluidInDisplay(Class<? extends Fluid> fluid);

    /**
     * Mostly for internal use; queries whether the fluid is blacklisted from storage cells.
     */
    boolean isBlacklistedInStorage(Class<? extends Fluid> fluid);

    /**
     * Mostly for internal use; queries whether the fluid is blacklisted from being displayed.
     */
    boolean isBlacklistedInDisplay(Class<? extends Fluid> fluid);
}
