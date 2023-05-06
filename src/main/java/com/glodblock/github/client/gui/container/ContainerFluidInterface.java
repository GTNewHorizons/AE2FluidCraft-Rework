package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFakeTypeOnly;
import com.glodblock.github.network.SPacketFluidUpdate;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.IOptionalSlotHost;
import appeng.util.item.AEFluidStack;

public class ContainerFluidInterface extends AEBaseContainer implements IOptionalSlotHost {

    private final IDualHost tile;

    public ContainerFluidInterface(InventoryPlayer ipl, IDualHost tile) {
        super(ipl, tile);
        this.tile = tile;
        IInventory inv = tile.getConfig();
        final int y = 35;
        final int x = 35;
        for (int i = 0; i < 6; i++) {
            addSlotToContainer(
                    new OptionalFluidSlotFakeTypeOnly(inv, tile.getDualityFluid().getConfig(), this, i, x, y, i, 0, 0));
        }
        bindPlayerInventory(ipl, 0, 149);
    }

    public IDualHost getTile() {
        return tile;
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return idx >= 0 && idx < 6;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        Map<Integer, IAEFluidStack> tmp = new HashMap<>();
        for (int i = 0; i < tile.getInternalFluid().getSlots(); i++) {
            tmp.put(i, tile.getInternalFluid().getFluidInSlot(i));
        }
        for (int i = 0; i < tile.getConfig().getSizeInventory(); i++) {
            tmp.put(i + 100, AEFluidStack.create(ItemFluidPacket.getFluidStack(tile.getConfig().getStackInSlot(i))));
        }
        for (final Object g : this.crafters) {
            if (g instanceof EntityPlayer) {
                FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(tmp), (EntityPlayerMP) g);
            }
        }
    }
}
