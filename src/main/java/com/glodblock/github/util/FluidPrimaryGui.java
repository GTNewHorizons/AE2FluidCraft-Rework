package com.glodblock.github.util;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;

import appeng.container.PrimaryGui;

public class FluidPrimaryGui extends PrimaryGui {

    public FluidPrimaryGui(Object gui, ItemStack guiIcon, TileEntity te, ForgeDirection side) {
        super(gui, guiIcon, te, side);
    }

    @Override
    public void open(EntityPlayer p) {
        if (gui instanceof GuiType gt) {
            if (te != null) {
                InventoryHandler.openGui(p, te.getWorldObj(), new BlockPos(te), Objects.requireNonNull(side), gt);
            } else {
                InventoryHandler.openGui(
                        p,
                        p.worldObj,
                        new BlockPos(
                                this.slotIndex == Integer.MIN_VALUE ? p.inventory.currentItem : this.slotIndex,
                                Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                0),
                        Objects.requireNonNull(side),
                        gt);
            }
        }
    }
}
