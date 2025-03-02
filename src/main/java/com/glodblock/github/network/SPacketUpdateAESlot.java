package com.glodblock.github.network;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFluidPatternTerminalEx;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketUpdateAESlot implements IMessage {

    public IAEItemStack slotItem;

    public int slotId;

    public SPacketUpdateAESlot() {
        // NO-OP
    }

    public SPacketUpdateAESlot(final int slotId, final IAEItemStack slotItem) {
        this.slotItem = slotItem;
        this.slotId = slotId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slotId = buf.readInt();
        this.slotItem = readItem(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slotId);
        writeItem(slotItem, buf);
    }

    private IAEItemStack readItem(final ByteBuf stream) {
        try {
            final boolean hasItem = stream.readBoolean();
            if (hasItem) {
                return AEItemStack.loadItemStackFromPacket(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void writeItem(final IAEItemStack slotItem, final ByteBuf data) {
        try {
            if (slotItem == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                slotItem.writeToPacket(data);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class Handler implements IMessageHandler<SPacketUpdateAESlot, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketUpdateAESlot message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiFluidPatternTerminal gf) {
                gf.setSlotAE(message.slotId, message.slotItem);
            } else if (gs instanceof GuiFluidPatternTerminalEx gf) {
                gf.setSlotAE(message.slotId, message.slotItem);
            }
            return null;
        }
    }
}
