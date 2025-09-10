package com.glodblock.github.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.item.WirelessCraftingTerminalInventory;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSelectBlockWithdraw implements IMessage {

    private int blockX;
    private int blockY;
    private int blockZ;

    public CPacketSelectBlockWithdraw() {
        // Required for FML
    }

    public CPacketSelectBlockWithdraw(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockX = buf.readInt();
        this.blockY = buf.readInt();
        this.blockZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.blockX);
        buf.writeInt(this.blockY);
        buf.writeInt(this.blockZ);
    }

    public static class Handler implements IMessageHandler<CPacketSelectBlockWithdraw, IMessage> {

        @Override
        public IMessage onMessage(CPacketSelectBlockWithdraw message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.inventory == null) {
                return null;
            }
            World world = player.worldObj;

            // Ensure the player has the wireless terminal
            ImmutablePair<Integer, ItemStack> terminalAndInventorySlot = Util.getUltraWirelessTerm(player);
            if (terminalAndInventorySlot == null) {
                return null;
            }

            ItemStack terminalStack = terminalAndInventorySlot.getRight();
            if (terminalStack == null || !(terminalStack.getItem() instanceof ItemWirelessUltraTerminal)) {
                return null;
            }

            IGridNode wirelessGrid = Util.getWirelessGrid(terminalStack);
            if (wirelessGrid == null) {
                return null;
            }

            // Create the terminal inventory handler
            WirelessCraftingTerminalInventory terminalInventory = new WirelessCraftingTerminalInventory(
                    terminalStack,
                    terminalAndInventorySlot.getLeft(),
                    wirelessGrid,
                    player);

            // Get the target block
            Block targetBlock = world.getBlock(message.blockX, message.blockY, message.blockZ);
            if (targetBlock == Blocks.air) {
                return null; // Don't try to withdraw air
            }

            Item targetItem = Item.getItemFromBlock(targetBlock);
            if (targetItem == null) {
                return null; // Should not happen for non-air blocks
            }

            // Check player inventory for existing stacks to determine how much to withdraw.
            ItemStack itemToFind = new ItemStack(
                    targetItem,
                    1,
                    world.getBlockMetadata(message.blockX, message.blockY, message.blockZ));

            // 1. Initial Scan for full/partial stacks
            int fullStackSlot = -1;
            List<Integer> partialStackSlotsList = new ArrayList<>();
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stackInSlot = player.inventory.mainInventory[i];
                if (stackInSlot != null && stackInSlot.isItemEqual(itemToFind)
                        && ItemStack.areItemStackTagsEqual(stackInSlot, itemToFind)) {
                    if (stackInSlot.stackSize >= stackInSlot.getMaxStackSize()) {
                        fullStackSlot = i;
                        break; // Found a full stack, no need to do anything.
                    }
                    partialStackSlotsList.add(i);
                }
            }

            // 2. If a full stack already exists, put in active slot and return.
            if (fullStackSlot >= 0) {
                setSlotAsActiveSlot(player, fullStackSlot);
                return null;
            }

            // 3. If there are no partial stacks and the player's inventory is full,
            // then return since we cannot add a retrieved stack to a full inventory
            int nextEmptySlot = getFirstEmptyStackReverse(player.inventory);
            if (partialStackSlotsList.isEmpty() && nextEmptySlot == -1) {
                return null;
            }

            // 4. Consolidate if multiple partial stacks exist.
            ItemStack consolidatedStack = null;
            int consolidatedStackSlot = -1;
            for (Integer partialStackSlot : partialStackSlotsList) {
                if (consolidatedStack == null) {
                    consolidatedStack = player.inventory.getStackInSlot(partialStackSlot);
                    consolidatedStackSlot = partialStackSlot;
                } else {
                    consolidateItemStacks(player.inventory, partialStackSlot, consolidatedStackSlot);
                }

                // Check if we created a full stack of items
                if (consolidatedStack.stackSize == consolidatedStack.getMaxStackSize()) {
                    swapInventorySlots(player.inventory, consolidatedStackSlot, player.inventory.currentItem);
                    return null;
                }
            }

            // Ensure the wireless terminal is within network range,
            // if not, move consolidated stack to the active slot and return
            if (!Util.rangeCheck(terminalStack, player, wirelessGrid)) {
                setSlotAsActiveSlot(player, consolidatedStackSlot);

                // TODO: Return out of network range player message.
                return null;
            }

            // 5. Calculate withdrawal amount
            int amountToWithdraw = consolidatedStack == null ? itemToFind.getMaxStackSize()
                    : itemToFind.getMaxStackSize() - consolidatedStack.stackSize;
            if (amountToWithdraw <= 0) {
                return null;
            }

            // Create an IAEItemStack for the target block with the calculated amount
            ItemStack targetItemStack = itemToFind.copy();
            targetItemStack.stackSize = amountToWithdraw;
            IAEItemStack targetAeItemStack = AEApi.instance().storage().createItemStack(targetItemStack);
            if (targetAeItemStack == null) {
                return null;
            }

            // 6. Extract items from the network
            IAEStack<?> extractedStack = terminalInventory.extractItems(targetAeItemStack);
            if (extractedStack instanceof IAEItemStack extractedAeItemStack && extractedStack.getStackSize() > 0) {
                ItemStack itemsToGive = extractedAeItemStack.getItemStack();
                // Update the player's inventory with the withdrawn items
                if (itemsToGive != null && itemsToGive.stackSize > 0) {
                    if (consolidatedStack == null) {
                        player.inventory.setInventorySlotContents(nextEmptySlot, itemsToGive);
                    } else {
                        consolidatedStack.stackSize += itemsToGive.stackSize;
                    }
                    player.inventory.markDirty();
                }
            }

            // If the target stack is already in the player's hotbar, set that as the active slot.
            // Otherwise, move the target stack to the active slot.
            int slotToSwap = consolidatedStack == null ? nextEmptySlot : consolidatedStackSlot;
            setSlotAsActiveSlot(player, slotToSwap);

            return null; // No reply packet needed
        }

        private int getFirstEmptyStackReverse(InventoryPlayer inventory) {
            for (int i = inventory.mainInventory.length - 1; i >= 0; --i) {
                if (inventory.mainInventory[i] == null) {
                    return i;
                }
            }

            return -1;
        }

        private void setSlotAsActiveSlot(EntityPlayerMP player, int slot) {
            if (slot >= 0 && slot <= 8) {
                player.inventory.currentItem = slot;
                player.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(slot));
            } else {
                swapInventorySlots(player.inventory, player.inventory.currentItem, slot);
            }
            player.inventory.markDirty();
        }

        /**
         * Moves an ItemStack from a source slot to a destination slot in the player's inventory. If the destination
         * slot is occupied, the items are swapped.
         *
         * @param inventory The player's inventory.
         * @param slot1     The index of the first ItemStack to move.
         * @param slot2     The index of the second ItemStack to move.
         */
        private void swapInventorySlots(InventoryPlayer inventory, int slot1, int slot2) {
            if (slot1 == slot2) {
                return;
            }

            // Get the stacks from both slots
            ItemStack sourceStack = inventory.getStackInSlot(slot1);
            ItemStack destinationStack = inventory.getStackInSlot(slot2);

            // Set the destination slot with the source stack (even if it's null)
            inventory.setInventorySlotContents(slot2, sourceStack);

            // Set the source slot with the original destination stack
            inventory.setInventorySlotContents(slot1, destinationStack);

            // Mark the inventory as dirty to ensure changes are saved and synced
            inventory.markDirty();
        }

        /**
         * Consolidate ItemStacks from a source slot to a destination slot in the player's inventory.
         *
         * @param inventory       The player's inventory.
         * @param sourceSlot      The index of the slot to move the item from.
         * @param destinationSlot The index of the slot to move the item to.
         */
        private void consolidateItemStacks(InventoryPlayer inventory, int sourceSlot, int destinationSlot) {
            ItemStack sourceStack = inventory.getStackInSlot(sourceSlot);
            ItemStack destinationStack = inventory.getStackInSlot(destinationSlot);

            if (!sourceStack.isItemEqual(destinationStack)
                    || !ItemStack.areItemStackTagsEqual(sourceStack, destinationStack)) {
                return;
            }

            int missingQuantity = destinationStack.getMaxStackSize() - destinationStack.stackSize;
            if (missingQuantity >= sourceStack.stackSize) {
                destinationStack.stackSize = destinationStack.stackSize + sourceStack.stackSize;
                sourceStack = null;
            } else {
                sourceStack.stackSize -= missingQuantity;
                destinationStack.stackSize += missingQuantity;
            }

            // Update the inventory stacks
            inventory.setInventorySlotContents(destinationSlot, destinationStack);
            inventory.setInventorySlotContents(sourceSlot, sourceStack);

            // Mark the inventory as dirty to ensure changes are saved and synced
            inventory.markDirty();
        }
    }
}
