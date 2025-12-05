package com.glodblock.github.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.util.PlayerInventoryUtil;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Network packet sent from client to server when a player looks at a block and requests to withdraw that block type
 * from their wireless terminal's ME network.
 *
 * <p>
 * This packet handles the entire withdrawal flow including:
 * <ul>
 * <li>Scanning the player's inventory for existing stacks of the target block</li>
 * <li>Consolidating partial stacks to maximize inventory efficiency</li>
 * <li>Extracting the appropriate amount from the ME network to fill or create a full stack</li>
 * <li>Setting the target stack as the player's active hotbar slot for immediate use</li>
 * </ul>
 *
 * <p>
 * The packet carries the coordinates of the block the player is looking at, which determines what item type to withdraw
 * from the ME network.
 */
public class CPacketPickBlockWithdraw implements IMessage {

    private int blockX;
    private int blockY;
    private int blockZ;

    public CPacketPickBlockWithdraw() {
        // Required for FML
    }

    public CPacketPickBlockWithdraw(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<CPacketPickBlockWithdraw, IMessage> {

        @Override
        public IMessage onMessage(CPacketPickBlockWithdraw message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.inventory == null) {
                return null;
            }
            World world = player.worldObj;

            // Ensure the player has a wireless terminal
            IMEInventoryHandler<IAEItemStack> terminalInventory = getWirelessInventory(player);
            if (terminalInventory == null) {
                return null;
            }

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

            // 1. Scan through the player's main inventory to categorize existing stacks of the target block:
            // - If a full stack (stackSize >= maxStackSize) is found, record its slot and stop searching.
            // This indicates the player already has the maximum possible stack, so no withdrawal is needed.
            // - Otherwise, collect all partial stack slots (stacks that match the item but aren't full).
            // Partial stacks will be consolidated in a later step.
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
                PlayerInventoryUtil.setSlotAsActiveSlot(player, fullStackSlot);
                return null;
            }

            // 3. If there are no partial stacks and the player's inventory is full,
            // then return since we cannot add a retrieved stack to a full inventory
            int nextEmptySlot = PlayerInventoryUtil.getFirstEmptyStackReverse(player.inventory);
            if (partialStackSlotsList.isEmpty() && nextEmptySlot == -1) {
                return null;
            }

            // 4. Consolidate all partial stacks of target block into 1 ItemStack.
            // If a full stack is obtained, set it as the active slot and return.
            ItemStack consolidatedStack = null;
            int consolidatedStackSlot = -1;
            for (Integer partialStackSlot : partialStackSlotsList) {
                if (consolidatedStack == null) {
                    consolidatedStack = player.inventory.getStackInSlot(partialStackSlot);
                    consolidatedStackSlot = partialStackSlot;
                } else {
                    PlayerInventoryUtil
                            .consolidateItemStacks(player.inventory, partialStackSlot, consolidatedStackSlot);
                }

                // Check if we created a full stack of items
                if (consolidatedStack.stackSize == consolidatedStack.getMaxStackSize()) {
                    PlayerInventoryUtil.setSlotAsActiveSlot(player, consolidatedStackSlot);
                    return null;
                }
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
            PlayerSource source = new PlayerSource(player, null);
            IAEStack<?> extractedStack = terminalInventory.extractItems(targetAeItemStack, Actionable.MODULATE, source);
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
            // The slot to swap will have either been a consolidated stack of partial ItemStacks,
            // or it will have been a newly created ItemStack in the next empty slot.
            int slotToSwap = consolidatedStack == null ? nextEmptySlot : consolidatedStackSlot;
            PlayerInventoryUtil.setSlotAsActiveSlot(player, slotToSwap);

            return null; // No reply packet needed
        }

        @SuppressWarnings("unchecked")
        private IMEInventoryHandler<IAEItemStack> getWirelessInventory(EntityPlayerMP player) {
            ImmutablePair<Integer, ItemStack> terminalAndInventorySlot = Util.getWirelessTerminal(player);
            ItemStack wctTerminal = null;

            if (terminalAndInventorySlot == null) {
                // Secondary check, does the player have a WirelessCraftingTerminal terminal
                // Why is this mod still in GTNH ;-;
                wctTerminal = RandomUtils.getWirelessTerm(player.inventory);
                if (wctTerminal == null) {
                    return null;
                }
            }

            IMEInventoryHandler<IAEItemStack> inventoryHandler;
            if (wctTerminal != null) {
                inventoryHandler = ContainerWirelessCraftingTerminal.getGuiObject(
                        wctTerminal,
                        player,
                        player.worldObj,
                        (int) player.posX,
                        (int) player.posY,
                        (int) player.posZ).getItemInventory();
            } else {
                ItemStack terminalItemStack = terminalAndInventorySlot.getRight();
                if (terminalItemStack == null) {
                    return null;
                }

                // Obtain the inventory handler for the AE2 wireless terminal.
                //
                // We can safely suppress this warning, as we are the one providing the storage type in
                // the function call.
                inventoryHandler = (IMEInventoryHandler<IAEItemStack>) Util
                        .getWirelessInv(terminalItemStack, player, StorageChannel.ITEMS);
            }

            return inventoryHandler;
        }
    }
}
