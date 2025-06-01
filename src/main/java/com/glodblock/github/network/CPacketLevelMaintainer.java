package com.glodblock.github.network;

import javax.annotation.Nullable;

import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketLevelMaintainer implements IMessage {

    public enum Action {
        Quantity,
        Batch,
        Enable,
        Disable,
    }

    private Action action;
    private long size;
    private int slotIndex;

    @SuppressWarnings("unused")
    public CPacketLevelMaintainer() {}

    public CPacketLevelMaintainer(Action action, int slotIndex) {
        this.action = action;
        this.slotIndex = slotIndex;
        this.size = 0;
    }

    public CPacketLevelMaintainer(Action action, int slotIndex, long size) {
        this.action = action;
        this.slotIndex = slotIndex;
        this.size = size;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = Action.values()[buf.readInt()];
        this.slotIndex = buf.readInt();
        this.size = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action.ordinal());
        buf.writeInt(this.slotIndex);
        buf.writeLong(this.size);
    }

    public static class Handler implements IMessageHandler<CPacketLevelMaintainer, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketLevelMaintainer message, MessageContext ctx) {
            if (ctx.getServerHandler().playerEntity.openContainer instanceof final ContainerLevelMaintainer clm) {
                switch (message.action) {
                    case Quantity -> clm.getTile().updateQuantity(message.slotIndex, message.size);
                    case Batch -> clm.getTile().updateBatchSize(message.slotIndex, message.size);
                    case Enable -> clm.getTile().updateStatus(message.slotIndex, false);
                    case Disable -> clm.getTile().updateStatus(message.slotIndex, true);
                }
                clm.updateGui();
            }
            return null;
        }
    }
}
