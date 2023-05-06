package com.glodblock.github.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.client.gui.GuiFluidMonitor;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Dedicated packet for fluid updates.
 */
public class SPacketMEFluidInvUpdate implements IMessage {

    private List<IAEFluidStack> list = new ArrayList<>();
    private byte ref = (byte) 0;
    private boolean resort = true;

    public SPacketMEFluidInvUpdate() {}

    /**
     * Used for the GUI to confirm crafting. 0 = available 1 = pending 2 = missing
     */
    public SPacketMEFluidInvUpdate(byte b) {
        ref = b;
    }

    /**
     * If resort, call "updateView()". Used when multiple packets are sent to open an inventory; only the last packet
     * should resort
     * 
     * @param resort whether this packet should resort the term or not
     */
    public SPacketMEFluidInvUpdate(boolean resort) {
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
                list.add(AEFluidStack.loadFluidStackFromPacket(buf));
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
            for (IAEFluidStack is : list) {
                is.writeToPacket(buf);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void setResort(boolean resort) {
        this.resort = resort;
    }

    public void appendFluid(final IAEFluidStack is) {
        list.add(is);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public static class Handler implements IMessageHandler<SPacketMEFluidInvUpdate, IMessage> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(SPacketMEFluidInvUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiFluidMonitor) {
                ((GuiFluidMonitor) gs).postUpdate(message.list, message.resort);
            } else if (gs instanceof GuiFluidCraftConfirm) {
                ((GuiFluidCraftConfirm) gs).postUpdate((List) message.list, message.ref);
            }
            return null;
        }
    }
}
