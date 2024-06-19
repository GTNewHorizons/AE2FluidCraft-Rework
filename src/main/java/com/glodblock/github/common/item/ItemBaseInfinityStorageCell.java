package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.AppEngInternalInventory;

public abstract class ItemBaseInfinityStorageCell extends AEBaseItem implements IStorageFluidCell {

    protected static class InfinityConfig extends AppEngInternalInventory {

        public InfinityConfig(final ItemStack is) {
            super(null, 1);
            this.setInventorySlotContents(0, is);
        }

        @Override
        public void markDirty() {}
    }

    @Override
    public long getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public abstract IInventory getConfigInventory(ItemStack is);

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {

    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
                                      final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
            .getCellInventory(stack, null, StorageChannel.FLUIDS);

        if (inventory instanceof final IFluidCellInventoryHandler handler) {
            final IFluidCellInventory cellInventory = handler.getCellInv();

            if (GuiScreen.isCtrlKeyDown()) {
                if (!cellInventory.getContents().isEmpty()) {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_CONTENTS));
                    for (IAEFluidStack fluid : cellInventory.getContents()) {
                        if (fluid != null) {
                            lines.add(String.format("  %s %s", StatCollector.translateToLocal(NameConst.TT_INFINITY_FLUID_STORAGE_TIPS),  fluid.getFluidStack().getLocalizedName()));
                        }
                    }
                } else {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_EMPTY));
                }
            } else {
                lines.add(StatCollector.translateToLocal(NameConst.TT_CTRL_FOR_MORE));
            }
        }
    }
}
