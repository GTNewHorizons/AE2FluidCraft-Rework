package com.glodblock.github.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.*;
import appeng.client.texture.ExtraBlockTextures;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;

public class FluidCellHandler implements ICellHandler {

    @Override
    public boolean isCell(final ItemStack is) {
        return FluidCellInventory.isCell(is);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IMEInventoryHandler getCellInventory(final ItemStack is, final ISaveProvider container,
            final StorageChannel channel) {
        if (channel == StorageChannel.FLUIDS) {
            return FluidCellInventory.getCell(is, container);
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler,
            final IMEInventoryHandler inv, final ItemStack is, final StorageChannel chan) {
        if (chan == StorageChannel.FLUIDS) {
            if (chest instanceof TileEntity) {
                TileEntity te = (TileEntity) chest;
                InventoryHandler
                        .openGui(player, te.getWorldObj(), new BlockPos(te), chest.getUp(), GuiType.FLUID_TERMINAL);
            }
        }
    }

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof FluidCellInventoryHandler) {
            final FluidCellInventoryHandler ci = (FluidCellInventoryHandler) handler;
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final IMEInventory handler) {
        final IFluidCellInventory inv = ((IFluidCellInventoryHandler) handler).getCellInv();
        return inv.getIdleDrain(is);
    }
}
