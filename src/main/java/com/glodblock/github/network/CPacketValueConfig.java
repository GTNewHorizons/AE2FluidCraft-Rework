package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.client.gui.container.ContainerFluidLevelEmitter;
import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.inventory.item.WirelessCraftingTerminalInventory;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketValueConfig implements IMessage {

    private long amount;
    private int valueIndex;

    public CPacketValueConfig() {}

    public CPacketValueConfig(long amount, int valueIndex) {
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = buf.readLong();
        this.valueIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(amount);
        buf.writeInt(valueIndex);
    }

    public static class Handler implements IMessageHandler<CPacketValueConfig, IMessage> {

        @Override

        public IMessage onMessage(CPacketValueConfig message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            Container container = player.openContainer;
            if (container != null) {
                if (container instanceof ContainerFluidLevelEmitter) {
                    ((ContainerFluidLevelEmitter) container).setLevel(message.amount, player);
                } else if (container instanceof ContainerPlayer) {
                    ImmutablePair<Integer, ItemStack> result = Util.Wireless.getUltraWirelessTerm(player);
                    if (result == null) return null;
                    if (message.valueIndex == 1) {
                        ItemBaseWirelessTerminal
                                .toggleRestockItemsMode(result.getRight(), !Util.Wireless.isRestock(result.getRight()));
                        return null;
                    } else if (Util.Wireless.isRestock(result.getRight())) {
                        restockItems(result.getRight(), result.getLeft(), player);
                    }
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private void restockItems(ItemStack terminal, int slot, EntityPlayer player) {
            IGridNode iGridNode = Util.Wireless.getWirelessGrid(terminal);
            if (iGridNode == null) return;
            WirelessCraftingTerminalInventory inv = new WirelessCraftingTerminalInventory(
                    terminal,
                    slot,
                    iGridNode,
                    player);

            for (int i = 0; i < 9; i++) {
                ItemStack is = player.inventory.mainInventory[i];
                if (is == null) continue;
                int maxSize = is.getMaxStackSize();
                if (is.stackSize == maxSize) continue;

                int fillSize = maxSize - is.stackSize;

                IAEItemStack ias = AEApi.instance().storage().createItemStack(is);

                ias.setStackSize(fillSize);
                IAEItemStack extractedItem = (IAEItemStack) inv
                        .extractItems(ias, Actionable.MODULATE, inv.getActionSource());
                if (extractedItem == null) continue;
                player.inventory.addItemStackToInventory(extractedItem.getItemStack());
            }
        }
    }
}
