package com.glodblock.github.common.tile;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;

import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class TileIngredientBuffer extends AEBaseInvTile implements IAEFluidInventory, IFluidHandler {

    protected AppEngInternalInventory invItems;
    protected AEFluidInventory invFluids;

    public TileIngredientBuffer() {
        this.invFluids = new AEFluidInventory(this, 4, 64000);
        this.invItems = new AppEngInternalInventory(this, 9);
    }

    @Nonnull
    @Override
    public IInventory getInternalInventory() {
        return invItems;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        markForUpdate();
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        markForUpdate();
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return this.invFluids;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    protected void writeToStream(ByteBuf data) throws IOException {
        for (int i = 0; i < invItems.getSizeInventory(); i++) {
            ByteBufUtils.writeItemStack(data, invItems.getStackInSlot(i));
        }
        this.invFluids.writeToBuf(data);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = false;
        for (int i = 0; i < invItems.getSizeInventory(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, invItems.getStackInSlot(i))) {
                invItems.setInventorySlotContents(i, stack);
                changed = true;
            }
        }
        changed |= this.invFluids.readFromBuf(data);
        return changed;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        invItems.readFromNBT(data, "ItemInv");
        invFluids.readFromNBT(data, "FluidInv");
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        invItems.writeToNBT(data, "ItemInv");
        invFluids.writeToNBT(data, "FluidInv");
        return data;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return invFluids.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return invFluids.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return invFluids.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return invFluids.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return invFluids.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return invFluids.getTankInfo(from);
    }
}
