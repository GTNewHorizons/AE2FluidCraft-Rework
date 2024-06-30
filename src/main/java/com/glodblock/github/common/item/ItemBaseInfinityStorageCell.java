package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.items.AEBaseItem;
import appeng.tile.inventory.AppEngInternalInventory;

public abstract class ItemBaseInfinityStorageCell extends AEBaseItem {

    protected static class InfinityConfig extends AppEngInternalInventory {

        public InfinityConfig(final ItemStack is) {
            super(null, 1);
            this.setInventorySlotContents(0, is);
        }

        @Override
        public void markDirty() {}
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
