package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;

public class ContainerFluidAutoFiller extends AEBaseContainer {

    public static class FakeSlot extends SlotFake {

        public FakeSlot(IInventory inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }

        @Override
        public void putStack(ItemStack is) {
            MutablePair<Boolean, ItemStack> result = ContainerFluidAutoFiller.isItemValid(is);
            if (result.left) {
                if (super.getHasStack()) super.clearStack();
                hasChange = true;
                super.putStack(result.right);
            }
        }
    }

    private final TileFluidAutoFiller tile;
    private static boolean hasChange;

    public ContainerFluidAutoFiller(InventoryPlayer ipl, TileFluidAutoFiller tile) {
        super(ipl, tile);
        this.tile = tile;
        addSlotToContainer(new FakeSlot(tile.getInventory(), 0, 80, 35));
        bindPlayerInventory(ipl, 0, 84);
    }

    public static MutablePair<Boolean, ItemStack> isItemValid(ItemStack itemStack) {
        boolean result = Util.FluidUtil.isFluidContainer(itemStack);
        if (!result) return new MutablePair<>(false, null);
        ItemStack tank = itemStack.copy();
        tank.stackSize = 1;
        if (Util.FluidUtil.isFilled(tank)) {
            return new MutablePair<>(true, Util.FluidUtil.clearFluid(tank));
        } else {
            return new MutablePair<>(true, tank);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (hasChange) {
            tile.updatePattern();
            hasChange = false;
        }
    }
}
