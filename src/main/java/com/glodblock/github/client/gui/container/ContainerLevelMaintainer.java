package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.network.SPacketLevelMaintainerGuiUpdate;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

public class ContainerLevelMaintainer extends AEBaseContainer {

    private final TileLevelMaintainer tile;
    private final SlotFluidConvertingFake[] requestSlots = new SlotFluidConvertingFake[TileLevelMaintainer.REQ_COUNT];

    private static final int UPDATE_INTERVAL = 20;
    private boolean isFirstUpdate = true;
    private int updateCount = UPDATE_INTERVAL;

    public ContainerLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(ipl, tile);
        this.tile = tile;
        AeItemStackHandler request = new AeItemStackHandler(tile.getRequestSlots());
        for (int y = 0; y < TileLevelMaintainer.REQ_COUNT; y++) {
            SlotFluidConvertingFake slot = new SlotFluidConvertingFake(request, y, 27, 20 + y * 19);
            addSlotToContainer(slot);
            requestSlots[y] = slot;
        }
        bindPlayerInventory(ipl, 0, 130);
    }

    public TileLevelMaintainer getTile() {
        return tile;
    }

    public SlotFluidConvertingFake[] getRequestSlots() {
        return this.requestSlots;
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        if (getSlot(slotId) instanceof SlotFluidConvertingFake slot) {
            final ItemStack stack = player.inventory.getItemStack();
            switch (action) {
                case PICKUP_OR_SET_DOWN, PLACE_SINGLE, SPLIT_OR_PLACE_SINGLE -> {
                    if (stack == null) {
                        slot.putStack(null);
                    } else {
                        slot.putConvertedStack(stack);
                    }
                }
                default -> {}
            }
        } else {
            super.doAction(player, action, slotId, id);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        if (Platform.isClient()) {
            return null;
        }

        for (int i = 0; i < this.getRequestSlots().length; i++) {
            SlotFluidConvertingFake slot = this.getRequestSlots()[i];
            if (!slot.getHasStack()) {
                ItemStack itemStack = this.inventorySlots.get(idx).getStack();
                tile.updateStack(i, itemStack.copy());
                break;
            }
        }

        this.detectAndSendChanges();
        return null;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isClient()) {
            return;
        }

        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.updateCount++ >= UPDATE_INTERVAL) {
            this.updateGui();
        }
    }

    public void updateGui() {
        FluidCraft.proxy.netHandler.sendTo(
                new SPacketLevelMaintainerGuiUpdate(this.tile.requests, !this.isFirstUpdate),
                (EntityPlayerMP) this.getInventoryPlayer().player);
        this.isFirstUpdate = false;
        this.updateCount = 0;
    }
}
