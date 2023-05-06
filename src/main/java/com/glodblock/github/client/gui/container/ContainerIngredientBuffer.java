package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.ITankDump;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.network.SPacketFluidUpdate;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;

public class ContainerIngredientBuffer extends AEBaseContainer implements ITankDump {

    private final TileIngredientBuffer tile;

    public ContainerIngredientBuffer(InventoryPlayer ipl, TileIngredientBuffer tile) {
        super(ipl, tile);
        this.tile = tile;
        IInventory inv = tile.getInternalInventory();
        addSlots(inv);
        bindPlayerInventory(ipl, 0, 140);
    }

    protected void addSlots(IInventory inv) {
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotNormal(inv, i, 8 + 18 * i, 108));
        }
    }

    public TileIngredientBuffer getTile() {
        return tile;
    }

    @Override
    public boolean canDumpTank(int index) {
        return tile.getInternalFluid().getFluidInSlot(index) != null;
    }

    @Override
    public void dumpTank(int index) {
        if (index >= 0 && index < tile.getInternalFluid().getSlots()) {
            tile.getInternalFluid().setFluidInSlot(index, null);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        Map<Integer, IAEFluidStack> tmp = new HashMap<>();
        for (int i = 0; i < tile.getInternalFluid().getSlots(); i++) {
            tmp.put(i, tile.getInternalFluid().getFluidInSlot(i));
        }
        for (final Object g : this.crafters) {
            if (g instanceof EntityPlayer) {
                FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(tmp), (EntityPlayerMP) g);
            }
        }
    }
}
