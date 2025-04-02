package com.glodblock.github.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiSuperStoker;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketSuperStokerUpdate implements IMessage {

    private Map<Integer, IAEStack<?>> list;

    public SPacketSuperStokerUpdate() {}

    public SPacketSuperStokerUpdate(Map<Integer, IAEStack<?>> data) {
        this.list = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.list = new HashMap<>();
        try {
            Util.readAEStackMapFromBuf(this.list, buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            Util.writeAEStackMapToBuf(this.list, buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<SPacketSuperStokerUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketSuperStokerUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiSuperStoker gss) {
                gss.update(message.list);
            }
            return null;
        }
    }
}
