package com.glodblock.github.network;

import net.minecraft.client.Minecraft;

import appeng.container.AEBaseContainer;
import appeng.helpers.ICustomButtonDataObject;
import appeng.helpers.ICustomButtonProvider;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketCustomButtonUpdate implements IMessage {

    private ICustomButtonDataObject dataObject;

    private ByteBuf buf;

    public SPacketCustomButtonUpdate() {}

    public SPacketCustomButtonUpdate(ICustomButtonDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.dataObject.writeByte(buf);
    }

    public static class Handler implements IMessageHandler<SPacketCustomButtonUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketCustomButtonUpdate message, MessageContext ctx) {
            if (Minecraft.getMinecraft().thePlayer.openContainer instanceof AEBaseContainer abc
                    && abc.getTarget() instanceof ICustomButtonProvider icbp) {
                icbp.getDataObject().readByte(message.buf);
            }
            return null;
        }
    }
}
