package com.glodblock.github.network;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;

import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;

import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketLevelMaintainer implements IMessage {

    public enum Action {
        Refresh,
        Quantity,
        Batch,
        Enable,
        Disable
    }

    private Action action;
    private long size;
    private int slotIndex;

    public CPacketLevelMaintainer() {
        this.action = Action.Refresh;
        this.slotIndex = 0;
        this.size = 0;
    }

    public CPacketLevelMaintainer(Action action) {
        this.action = action;
        this.slotIndex = 0;
        this.size = 0;
    }

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

        private List<IAEItemStack> refresh(ContainerLevelMaintainer cca) {
            List<IAEItemStack> toSend = new ArrayList<>(TileLevelMaintainer.REQ_COUNT);
            for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                IAEItemStack ias = cca.getTile().requests.getAEItemStack(i);
                if (ias != null) {
                    toSend.add(ias);
                }
            }
            return toSend;
        }

        @Nullable
        @Override
        public IMessage onMessage(CPacketLevelMaintainer message, MessageContext ctx) {
            if (ctx.getServerHandler().playerEntity.openContainer instanceof final ContainerLevelMaintainer clm) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                List<IAEItemStack> toSend = switch (message.action) {
                    case Quantity -> {
                        clm.getTile().updateQuantity(message.slotIndex, message.size);
                        yield this.refresh(clm);
                    }
                    case Batch -> {
                        clm.getTile().updateBatchSize(message.slotIndex, message.size);
                        yield this.refresh(clm);
                    }
                    case Enable -> {
                        clm.getTile().updateStatus(message.slotIndex, false);
                        yield this.refresh(clm);
                    }
                    case Disable -> {
                        clm.getTile().updateStatus(message.slotIndex, true);
                        yield this.refresh(clm);
                    }
                    case Refresh -> this.refresh(clm);
                };

                SPacketMEUpdateBuffer.scheduleItemUpdate(player, toSend);
            }
            return null;
        }
    }
}
