package com.glodblock.github.util;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import appeng.api.implementations.items.IStorageCell;
import appeng.me.storage.CellInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemBasicFluidStorageCell;

import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEStackType;
import appeng.core.sync.GuiBridge;
import appeng.me.storage.FluidCellInventory;
import appeng.me.storage.FluidCellInventoryHandler;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FluidCellHandler implements ICellHandler {

    @Override
    public boolean isCell(final ItemStack is) {
        return FluidCellInventory.isCell(is);
    }

    /**
     * ME Chest icon
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Dark() {
        return FCPartsTexture.BlockMEChestFluid_Dark.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Light() {
        return FCPartsTexture.BlockMEChestFluid_Bright.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Medium() {
        return FCPartsTexture.BlockMEChestFluid_Medium.getIcon();
    }

    @Override
    public void openChestGui(final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler,
            @SuppressWarnings("rawtypes") final IMEInventoryHandler inv, final ItemStack itemStack,
            final StorageChannel channel) {
        Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
    }

    @Override
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        if (handler instanceof FluidCellInventoryHandler ci) {
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, @SuppressWarnings("rawtypes") final IMEInventory handler) {
        return 0.0;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack fluidCell, ISaveProvider saveProvider, IAEStackType<?> type) {
        if (fluidCell != null && fluidCell.getItem() instanceof IStorageCell cell && cell.getStackType() == type) {
            return CellInventory.getCell(fluidCell, saveProvider, type);
        }
        return null;
    }
}
