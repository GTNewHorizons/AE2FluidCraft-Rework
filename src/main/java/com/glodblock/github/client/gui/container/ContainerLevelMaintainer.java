package com.glodblock.github.client.gui.container;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer.State;
import com.glodblock.github.common.tile.TileLevelMaintainer.TLMTags;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

public class ContainerLevelMaintainer extends AEBaseContainer {

    private final TileLevelMaintainer tile;
    private final SlotFluidConvertingFake[] requestSlots = new SlotFluidConvertingFake[TileLevelMaintainer.REQ_COUNT];

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
        Slot slot = getSlot(slotId);
        if (slot instanceof SlotFluidConvertingFake) {
            final ItemStack stack = player.inventory.getItemStack();
            switch (action) {
                case PICKUP_OR_SET_DOWN -> {
                    if (stack == null) {
                        slot.putStack(null);
                    } else {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(createLevelValues(stack.copy()));
                    }
                }
                case PLACE_SINGLE -> {
                    if (stack != null) {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(
                                createLevelValues(Objects.requireNonNull(Util.copyStackWithSize(stack, 1))));
                    }
                }
                case SPLIT_OR_PLACE_SINGLE -> {
                    ItemStack inSlot = slot.getStack();
                    if (inSlot != null) {
                        if (stack == null) {
                            slot.putStack(
                                    createLevelValues(
                                            Objects.requireNonNull(
                                                    Util.copyStackWithSize(
                                                            inSlot,
                                                            Math.max(1, inSlot.stackSize - 1)))));
                        } else if (stack.isItemEqual(inSlot)) {
                            slot.putStack(
                                    createLevelValues(
                                            Objects.requireNonNull(
                                                    Util.copyStackWithSize(
                                                            inSlot,
                                                            Math.min(
                                                                    inSlot.getMaxStackSize(),
                                                                    inSlot.stackSize + 1)))));
                        } else {
                            ((SlotFluidConvertingFake) slot).putConvertedStack(
                                    createLevelValues(Objects.requireNonNull(Util.copyStackWithSize(stack, 1))));
                        }
                    } else if (stack != null) {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(
                                createLevelValues(Objects.requireNonNull(Util.copyStackWithSize(stack, 1))));
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
                ItemStack itemStack = ((Slot) this.inventorySlots.get(idx)).getStack();
                ItemStack configuration = createLevelValues(itemStack.copy());
                configuration.getTagCompound().setInteger(TLMTags.Index.tagName, i);
                slot.putConvertedStack(configuration);
                break;
            }
        }

        this.detectAndSendChanges();
        return null;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        super.detectAndSendChanges();
    }

    public static ItemStack createLevelValues(ItemStack itemStack) {
        var data = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
        if (!data.hasKey(TLMTags.Stack.tagName)) {
            var itemStackTag = new NBTTagCompound();
            itemStack.copy().writeToNBT(itemStackTag);
            data.setTag(TLMTags.Stack.tagName, itemStackTag);
        }
        if (!data.hasKey(TLMTags.Quantity.tagName)) {
            data.setLong(TLMTags.Quantity.tagName, itemStack.stackSize > 0 ? itemStack.stackSize : 1);
        }
        if (!data.hasKey(TLMTags.Batch.tagName)) {
            data.setLong(TLMTags.Batch.tagName, 1);
        }
        if (!data.hasKey(TLMTags.Enable.tagName)) {
            data.setBoolean(TLMTags.Enable.tagName, false);
        }
        if (data.hasKey(TLMTags.Index.tagName)) {
            data.removeTag(TLMTags.Index.tagName);
        }

        data.setInteger(TLMTags.State.tagName, State.None.ordinal());
        itemStack.setTagCompound(data);

        return itemStack;
    }

}
