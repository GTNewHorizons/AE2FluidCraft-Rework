package com.glodblock.github.common.parts;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor {

    public PartFluidConversionMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartShiftActivate(final EntityPlayer player, final Vec3 pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }
        FluidStack fluidStack;
        ItemStack item = player.getCurrentEquippedItem();
        if (item != null && item.getItem() instanceof ItemFluidPacket) {
            fluidStack = ItemFluidPacket.getFluidStack(item);
        } else {
            fluidStack = Util.FluidUtil.getFluidFromContainer(item);
        }
        if (this.getDisplayed() == null || fluidStack == null
                || fluidStack.getFluid() != ((IAEFluidStack) this.getDisplayed()).getFluid()) {
            return false;
        }

        try {
            if (!this.getProxy().isActive()) {
                return false;
            }
            final IEnergySource energy = this.getProxy().getEnergy();
            final IMEMonitor<IAEFluidStack> cell = this.getProxy().getStorage().getFluidInventory();
            final IAEFluidStack input = (IAEFluidStack) this.getDisplayed().copy().setStackSize(fluidStack.amount);
            final IAEFluidStack failedToInsert = Platform
                    .poweredInsert(energy, cell, input, new PlayerSource(player, this));
            if (failedToInsert != null && failedToInsert.getStackSize() == input.getStackSize()) {
                return false;
            } else if (failedToInsert == null || failedToInsert.getStackSize() != input.getStackSize()) {
                if (item.getItem() instanceof ItemFluidPacket) {
                    if (failedToInsert != null) {
                        player.getCurrentEquippedItem()
                                .setTagCompound(ItemFluidPacket.newStack(failedToInsert).getTagCompound());
                        return true;
                    }
                } else {
                    ItemStack tmp = item.copy();
                    tmp.stackSize = 1;
                    ItemStack tank = null;
                    if (failedToInsert == null) {
                        tank = Util.FluidUtil.clearFluid(tmp);
                    } else {
                        if (tmp.getItem() instanceof IFluidContainerItem) {
                            tank = Util.FluidUtil.setFluidContainerAmount(tmp, (int) failedToInsert.getStackSize());
                        } else if (FluidContainerRegistry.isContainer(tmp)) {
                            IAEFluidStack insertedFluid = input.copy();
                            insertedFluid.decStackSize(failedToInsert.getStackSize());
                            this.getProxy().getStorage().getFluidInventory()
                                    .extractItems(insertedFluid, Actionable.MODULATE, new PlayerSource(player, this));
                            return false;
                        }
                    }
                    if (tank != null && !player.inventory.addItemStackToInventory(tank)) {
                        player.entityDropItem(tank, 0);
                    }
                }
                item.stackSize--;
                if (item.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                }
            }

        } catch (final GridAccessException e) {
            // :P
        }
        return true;
    }

    @Override
    protected void extractItem(final EntityPlayer player) {
        final IAEFluidStack input = (IAEFluidStack) this.getDisplayed();
        ItemStack eq = player.getCurrentEquippedItem();
        if (input != null && Util.FluidUtil.isFluidContainer(eq) && Util.FluidUtil.isEmpty(eq)) {
            try {
                if (!this.getProxy().isActive()) {
                    return;
                }

                final IEnergySource energy = this.getProxy().getEnergy();
                final IMEMonitor<IAEFluidStack> cell = this.getProxy().getStorage().getFluidInventory();
                ItemStack tank = eq.copy();
                tank.stackSize = 1;
                MutablePair<Integer, ItemStack> fillStack = Util.FluidUtil.fillStack(tank, input.getFluidStack());
                input.setStackSize(fillStack.left);

                final IAEFluidStack retrieved = Platform
                        .poweredExtraction(energy, cell, input, new PlayerSource(player, this));
                if (retrieved != null) {
                    if (!player.inventory.addItemStackToInventory(fillStack.right)) {
                        player.entityDropItem(fillStack.right.copy(), 0);
                    }
                    eq.stackSize--;
                    if (eq.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                    final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                    ItemStack newItems = adaptor.addItems(
                            Util.FluidUtil.setFluidContainerAmount(fillStack.right, (int) retrieved.getStackSize()));
                    if (newItems != null) {
                        final TileEntity te = this.getTile();
                        final List<ItemStack> list = Collections.singletonList(newItems);
                        Platform.spawnDrops(
                                player.worldObj,
                                te.xCoord + this.getSide().offsetX,
                                te.yCoord + this.getSide().offsetY,
                                te.zCoord + this.getSide().offsetZ,
                                list);
                    }

                    if (player.openContainer != null) {
                        player.openContainer.detectAndSendChanges();
                    }
                }
            } catch (final GridAccessException e) {
                // :P
            }
        }
    }
}
