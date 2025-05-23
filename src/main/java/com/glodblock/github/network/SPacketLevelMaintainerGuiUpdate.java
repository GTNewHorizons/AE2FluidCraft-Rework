package com.glodblock.github.network;

import static com.glodblock.github.common.tile.TileLevelMaintainer.REQ_COUNT;
import static com.glodblock.github.common.tile.TileLevelMaintainer.RequestInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.api.registries.LevelState;
import com.glodblock.github.client.gui.GuiLevelMaintainer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketLevelMaintainerGuiUpdate implements IMessage {

    private Info[] infoList;
    private boolean onlyState;

    @SuppressWarnings("unused")
    public SPacketLevelMaintainerGuiUpdate() {}

    public SPacketLevelMaintainerGuiUpdate(RequestInfo[] requests, boolean onlyState) {
        this.infoList = new Info[REQ_COUNT];
        this.onlyState = onlyState;

        for (int i = 0; i < REQ_COUNT; i++) {
            if (requests[i] == null) {
                this.infoList[i] = null;
            } else {
                this.infoList[i] = new Info(
                        requests[i].getQuantity(),
                        requests[i].getBatchSize(),
                        requests[i].isEnable(),
                        requests[i].getState());
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.infoList = new Info[REQ_COUNT];
        this.onlyState = buf.readBoolean();

        for (int i = 0; i < REQ_COUNT; i++) {
            if (buf.readBoolean()) {
                if (this.onlyState) {
                    this.infoList[i] = new Info(LevelState.values()[buf.readInt()]);
                } else {
                    this.infoList[i] = new Info(
                            buf.readLong(),
                            buf.readLong(),
                            buf.readBoolean(),
                            LevelState.values()[buf.readInt()]);
                }
            } else {
                this.infoList[i] = null;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.onlyState);
        for (Info info : this.infoList) {
            buf.writeBoolean(info != null);
            if (info == null) continue;
            if (!this.onlyState) {
                buf.writeLong(info.quantity);
                buf.writeLong(info.batchSize);
                buf.writeBoolean(info.enable);
            }
            buf.writeInt(info.state.ordinal());
        }
    }

    private static class Info {

        long quantity;
        long batchSize;
        boolean enable;
        LevelState state;

        Info(long quantity, long batchSize, boolean enable, LevelState state) {
            this.quantity = quantity;
            this.batchSize = batchSize;
            this.enable = enable;
            this.state = state;
        }

        Info(LevelState state) {
            this.quantity = 0;
            this.batchSize = 0;
            this.enable = false;
            this.state = state;
        }
    }

    public static class Handler implements IMessageHandler<SPacketLevelMaintainerGuiUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketLevelMaintainerGuiUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

            if (gs instanceof GuiLevelMaintainer gui) {
                for (int i = 0; i < REQ_COUNT; i++) {
                    Info info = message.infoList[i];
                    if (info == null) continue;
                    if (message.onlyState) {
                        gui.updateComponent(i, info.state);
                    } else {
                        gui.updateComponent(i, info.quantity, info.batchSize, info.enable, info.state);
                    }
                }
            }

            return null;
        }
    }
}
