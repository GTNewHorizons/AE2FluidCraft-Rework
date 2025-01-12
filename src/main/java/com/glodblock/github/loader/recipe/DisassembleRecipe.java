package com.glodblock.github.loader.recipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.*;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.IterationCounter;
import appeng.util.Platform;

public class DisassembleRecipe implements IRecipe {

    private final Map<ItemStack, ItemStack> cellMappings;
    private final Map<ItemStack, ItemStack> nonCellMappings;

    public DisassembleRecipe() {
        this.cellMappings = new HashMap<>(16);
        this.nonCellMappings = new HashMap<>(1);

        this.cellMappings.put(new ItemStack(CELL1K), new ItemStack(CELL_PART, 1, 0));
        this.cellMappings.put(new ItemStack(CELL4K), new ItemStack(CELL_PART, 1, 1));
        this.cellMappings.put(new ItemStack(CELL16K), new ItemStack(CELL_PART, 1, 2));
        this.cellMappings.put(new ItemStack(CELL64K), new ItemStack(CELL_PART, 1, 3));
        this.cellMappings.put(new ItemStack(CELL256K), new ItemStack(CELL_PART, 1, 4));
        this.cellMappings.put(new ItemStack(CELL1024K), new ItemStack(CELL_PART, 1, 5));
        this.cellMappings.put(new ItemStack(CELL4096K), new ItemStack(CELL_PART, 1, 6));
        this.cellMappings.put(new ItemStack(CELL16384K), new ItemStack(CELL_PART, 1, 7));

        this.cellMappings.put(new ItemStack(CELL1KM), new ItemStack(CELL_PART, 1, 0));
        this.cellMappings.put(new ItemStack(CELL4KM), new ItemStack(CELL_PART, 1, 1));
        this.cellMappings.put(new ItemStack(CELL16KM), new ItemStack(CELL_PART, 1, 2));
        this.cellMappings.put(new ItemStack(CELL64KM), new ItemStack(CELL_PART, 1, 3));
        this.cellMappings.put(new ItemStack(CELL256KM), new ItemStack(CELL_PART, 1, 4));
        this.cellMappings.put(new ItemStack(CELL1024KM), new ItemStack(CELL_PART, 1, 5));
        this.cellMappings.put(new ItemStack(CELL4096KM), new ItemStack(CELL_PART, 1, 6));
        this.cellMappings.put(new ItemStack(CELL16384KM), new ItemStack(CELL_PART, 1, 7));

        this.nonCellMappings.put(
                new ItemStack(PATTERN),
                AEApi.instance().definitions().materials().blankPattern().maybeStack(1).get());
    }

    @Override
    public boolean matches(final InventoryCrafting inv, final World w) {
        return this.getOutput(inv) != null;
    }

    @SuppressWarnings("unchecked")
    private ItemStack getOutput(final IInventory inventory) {
        int itemCount = 0;
        ItemStack output = null;

        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
            final ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
            if (stackInSlot != null) {
                // needs a single input in the recipe
                itemCount++;
                if (itemCount > 1) {
                    return null;
                }

                // handle storage cells
                final ItemStack storageCellStack = this.getCellOutput(stackInSlot);
                if (storageCellStack != null) {
                    // make sure the storage cell stackInSlot empty...
                    final IMEInventory<IAEFluidStack> cellInv = AEApi.instance().registries().cell()
                            .getCellInventory(stackInSlot, null, StorageChannel.FLUIDS);
                    if (cellInv != null) {
                        final IItemList<IAEFluidStack> list = cellInv
                                .getAvailableItems(StorageChannel.FLUIDS.createList(), IterationCounter.fetchNewId());
                        if (!list.isEmpty()) {
                            return null;
                        }
                    }

                    output = storageCellStack;
                }

                // handle crafting storage blocks
                final ItemStack craftingStorageStack = this.getNonCellOutput(stackInSlot);
                if (craftingStorageStack != null) {
                    output = craftingStorageStack;
                }
            }
        }

        return output;
    }

    private ItemStack getCellOutput(final ItemStack compared) {
        for (final Map.Entry<ItemStack, ItemStack> entry : this.cellMappings.entrySet()) {
            if (Platform.isSameItemType(compared, entry.getKey())) {
                return entry.getValue().copy();
            }
        }

        return null;
    }

    private ItemStack getNonCellOutput(final ItemStack compared) {
        for (final Map.Entry<ItemStack, ItemStack> entry : this.nonCellMappings.entrySet()) {
            if (Platform.isSameItemType(compared, entry.getKey())) {
                return entry.getValue().copy();
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(final InventoryCrafting inv) {
        return this.getOutput(inv);
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }

    @Nullable
    @Override
    public ItemStack getRecipeOutput() // no default output..
    {
        return null;
    }
}
