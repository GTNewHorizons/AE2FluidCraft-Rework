package com.glodblock.github.network;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiPatternValueAmount;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketSetItemAmount implements IMessage {

    private long amount;

    public SPacketSetItemAmount() {
        // NO-OP
    }

    public SPacketSetItemAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(amount);
    }

    public static class Handler implements IMessageHandler<SPacketSetItemAmount, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketSetItemAmount message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiPatternValueAmount) {
                ((GuiPatternValueAmount) gs).setAmount(message.amount);
            }
            return null;
        }
    }
}
