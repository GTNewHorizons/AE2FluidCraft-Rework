package com.glodblock.github.common.storage;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;

public interface IFluidCellInventory extends IMEInventory<IAEFluidStack> {

    /**
     * @return the item stack of this storage cell.
     */
    ItemStack getItemStack();

    /**
     * @return idle cost for this Storage Cell
     */
    double getIdleDrain(ItemStack is);

    /**
     * @return access configured list
     */
    IInventory getConfigInventory();

    /**
     * @return How many bytes are used for each type?
     */
    int getBytesPerType();

    /**
     * @return true if a new fluid type can be added.
     */
    boolean canHoldNewFluid();

    /**
     * @return total byte storage.
     */
    long getTotalBytes();

    /**
     * @return how many bytes are free.
     */
    long getFreeBytes();

    /**
     * @return how many bytes are in use.
     */
    long getUsedBytes();

    /**
     * @return how many fluids are stored.
     */
    long getStoredFluidCount();

    /**
     * @return how many more fluids can be stored.
     */
    long getRemainingFluidCount();

    /**
     * @return how many more fluids can be stored with Distribution card.
     */
    long getRemainingFluidCountDist(IAEFluidStack l);

    /**
     * @return how many fluid types remain.
     */
    long getRemainingFluidTypes();

    /**
     * @return how many fluids can be added without consuming another byte.
     */
    int getUnusedFluidCount();

    /**
     * @return the status number for this drive.
     */
    int getStatusForCell();

    /**
     * @return how many items types are currently stored.
     */
    long getStoredFluidTypes();

    /**
     * @return max number of types.
     */
    long getTotalFluidTypes();

    List<IAEFluidStack> getContents();

    /**
     * @return access installed upgrades.
     */
    IInventory getUpgradesInventory();
}
