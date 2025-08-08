package com.glodblock.github.common.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.data.IAEFluidStack;

public interface IFluidCellInventoryHandler {

    IFluidCellInventory getCellInv();

    Iterable<IAEFluidStack> getPartitionInv();

    boolean isPreformatted();

    /** use {@link IFluidCellInventoryHandler#getWhitelist} instead */
    @Deprecated
    IncludeExclude getIncludeExcludeMode();

    IncludeExclude getWhitelist();
}
