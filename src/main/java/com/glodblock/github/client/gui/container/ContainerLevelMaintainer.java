package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.network.SPacketLevelMaintainerGuiUpdate;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerSubGui;
import appeng.container.interfaces.IVirtualSlotHolder;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketVirtualSlot;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ContainerLevelMaintainer extends ContainerSubGui implements IVirtualSlotHolder {

    public static final int PANEL_WIDENING = 38;

    public static final int PLAYER_INV_OFFSET_X = PANEL_WIDENING / 2;

    private final TileLevelMaintainer tile;

    private static final int UPDATE_INTERVAL = 20;
    private boolean isFirstUpdate = true;
    private int updateCount = UPDATE_INTERVAL;

    public ContainerLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(ipl, tile);
        this.tile = tile;

        bindPlayerInventory(ipl, PLAYER_INV_OFFSET_X, 130);
    }

    public TileLevelMaintainer getTile() {
        return tile;
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

        ItemStack clickedStack = this.inventorySlots.get(idx).getStack();
        if (clickedStack != null) {
            IAEStackInventory inventory = this.getTile().getAEStackInventory();
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                IAEStack<?> stack = inventory.getAEStackInSlot(i);
                if (stack == null) {
                    tile.updateStack(i, AEItemStack.create(clickedStack));
                    break;
                }
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
        this.sendGuiUpdate(true);
    }

    public void updateGuiFull() {
        this.sendGuiUpdate(false);
    }

    private void sendGuiUpdate(boolean stateOnly) {
        if (this.isFirstUpdate) {
            Int2ObjectMap<IAEStack<?>> list = new Int2ObjectOpenHashMap<>();
            for (int i = 0; i < this.tile.requests.length; i++) {
                TileLevelMaintainer.RequestInfo info = this.tile.requests[i];
                list.put(i, info != null ? info.getAEStack() : null);
            }
            NetworkHandler.instance.sendTo(
                    new PacketVirtualSlot(StorageName.NONE, list),
                    (EntityPlayerMP) this.getInventoryPlayer().player);
        }
        FluidCraft.proxy.netHandler.sendTo(
                new SPacketLevelMaintainerGuiUpdate(
                        this.tile.requests,
                        stateOnly && !this.isFirstUpdate,
                        this.tile.isLiteMode()),
                (EntityPlayerMP) this.getInventoryPlayer().player);
        this.isFirstUpdate = false;
        this.updateCount = 0;
    }

    @Override
    public void receiveSlotStacks(StorageName invName, Int2ObjectMap<IAEStack<?>> slotStacks) {
        for (var entry : slotStacks.int2ObjectEntrySet()) {
            this.tile.updateStack(entry.getIntKey(), entry.getValue());
        }
        if (!this.tile.getWorldObj().isRemote) {
            for (var player : this.crafters) {
                NetworkHandler.instance
                        .sendTo(new PacketVirtualSlot(StorageName.NONE, slotStacks), (EntityPlayerMP) player);
                FluidCraft.proxy.netHandler.sendTo(
                        new SPacketLevelMaintainerGuiUpdate(this.tile.requests, false, this.tile.isLiteMode()),
                        (EntityPlayerMP) player);
            }
        }
    }
}
