package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.client.gui.container.ContainerFluidLevelEmitter;
import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

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
                    ImmutablePair<Integer, ItemStack> result = Util.getUltraWirelessTerm(player);
                    if (result != null) {
                        final ItemStack wirelessTerm = result.getRight();
                        if (message.valueIndex == 1) {
                            ItemBaseWirelessTerminal
                                    .toggleRestockItemsMode(wirelessTerm, !Util.isRestock(wirelessTerm));
                            player.addChatMessage(
                                    new ChatComponentText(
                                            StatCollector.translateToLocal(
                                                    !Util.isRestock(wirelessTerm)
                                                            ? NameConst.TT_ULTRA_TERMINAL_RESTOCK_ON
                                                            : NameConst.TT_ULTRA_TERMINAL_RESTOCK_OFF)));
                        }
                    }
                }
            }
            return null;
        }
    }
}
