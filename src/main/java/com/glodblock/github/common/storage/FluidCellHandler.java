package com.glodblock.github.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;

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
            if (chest instanceof TileEntity te) {
                InventoryHandler
                        .openGui(player, te.getWorldObj(), new BlockPos(te), chest.getUp(), GuiType.FLUID_TERMINAL);
            }
        }
    }

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof final FluidCellInventoryHandler ci) {
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
