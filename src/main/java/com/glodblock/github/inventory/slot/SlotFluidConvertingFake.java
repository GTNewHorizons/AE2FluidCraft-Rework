package com.glodblock.github.inventory.slot;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventory;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.util.item.AEItemStack;

public class SlotFluidConvertingFake extends SlotFake implements ISlotFluid {

    private final AeStackInventory<IAEItemStack> inv;

    public SlotFluidConvertingFake(AeItemStackHandler inv, int idx, int x, int y) {
        super(inv, idx, x, y);
        this.inv = inv.getAeInventory();
    }

    @Override
    public void putStack(ItemStack stack) {
        inv.setStack(getSlotIndex(), AEItemStack.create(stack));
    }

    @Override
    public void setAeStack(@Nullable IAEItemStack stack, boolean sync) {
        inv.setStack(getSlotIndex(), stack);
    }

    public void putConvertedStack(ItemStack stack) {
        if (stack == null) {
            setAeStack(null, false);
            return;
        } else if (stack.getItem() instanceof IFluidContainerItem) {
            FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
            if (fluid != null) {
                fluid.amount *= stack.stackSize;
                IAEItemStack aeStack = ItemFluidPacket.newAeStack(fluid);
                if (aeStack != null) {
                    setAeStack(aeStack, false);
                    return;
                }
            }
        } else if (FluidContainerRegistry.isContainer(stack)) {
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (fluid != null) {
                fluid.amount *= stack.stackSize;
                IAEItemStack aeStack = ItemFluidPacket.newAeStack(fluid);
                if (aeStack != null) {
                    setAeStack(aeStack, false);
                    return;
                }
            }
        }
        putStack(stack);
    }

    @Nullable
    @Override
    public IAEItemStack getAeStack() {
        return inv.getStack(getSlotIndex());
    }
}
