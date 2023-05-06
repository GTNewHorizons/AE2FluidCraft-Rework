package com.glodblock.github.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.client.gui.GuiItemMonitor;
import com.glodblock.github.client.gui.GuiLevelMaintainer;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Packet dedicated to item inventory update.
 */
public class SPacketMEItemInvUpdate implements IMessage {

    private List<IAEItemStack> list = new ArrayList<>();
    private byte ref = (byte) 0;
    private boolean resort = true;

    public SPacketMEItemInvUpdate() {}

    /**
     * Used for the GUI to confirm crafting. 0 = available 1 = pending 2 = missing
     */
    public SPacketMEItemInvUpdate(byte b) {
        ref = b;
    }

    /**
     * If resort, call "updateView()". Used when multiple packets are sent to open an inventory; only the last packet
     * should resort.
     *
     * @param resort whether this packet should resort the term or not
     */
    public SPacketMEItemInvUpdate(boolean resort) {
        this.resort = resort;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ref = buf.readByte();
        resort = buf.readBoolean();
        int amount = buf.readInt();
        list = new ArrayList<>(amount);
        try {
            for (int i = 0; i < amount; i++) {
                list.add(AEItemStack.loadItemStackFromPacket(buf));
            }
        } catch (Exception io) {
            System.out.println("Error handling payload w/ " + amount + " items.");
            io.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(ref);
        buf.writeBoolean(resort);
        buf.writeInt(list.size());
        try {
            for (IAEItemStack is : list) {
                is.writeToPacket(buf);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void setResort(boolean resort) {
        this.resort = resort;
    }

    public void appendItem(final IAEItemStack is) {
        list.add(is);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public static class Handler implements IMessageHandler<SPacketMEItemInvUpdate, IMessage> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(SPacketMEItemInvUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiItemMonitor) {
                ((GuiItemMonitor) gs).postUpdate(message.list, message.resort);
            } else if (gs instanceof GuiFluidCraftConfirm) {
                ((GuiFluidCraftConfirm) gs).postUpdate(message.list, message.ref);
            } else if (gs instanceof GuiLevelMaintainer) {
                ((GuiLevelMaintainer) gs).postUpdate(message.list);
            }
            return null;
        }
    }
}
