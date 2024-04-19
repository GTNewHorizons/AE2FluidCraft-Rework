package com.glodblock.github.network;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.client.gui.GuiItemMonitor;
import com.glodblock.github.client.gui.GuiLevelMaintainer;
import com.glodblock.github.proxy.ClientProxy;

import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet dedicated to item inventory update.
 */
public class SPacketMEItemInvUpdate extends SPacketMEBaseInvUpdate implements IMessage {

    public SPacketMEItemInvUpdate() {
        super();
    }

    /**
     * Used for the GUI to confirm crafting. 0 = available 1 = pending 2 = missing
     */
    public SPacketMEItemInvUpdate(byte b) {
        super(b);
    }

    public void appendItem(final IAEItemStack is) {
        list.add(is);
    }

    public void addAll(final List<IAEItemStack> list) {
        this.list.addAll(list);
    }

    public static class Handler implements IMessageHandler<SPacketMEItemInvUpdate, IMessage> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(SPacketMEItemInvUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs == null && message.ref != 0) {
                ClientProxy.postUpdate((List) message.list, message.ref);
            }
            if (gs instanceof GuiItemMonitor) {
                ((GuiItemMonitor) gs).postUpdate((List) message.list);
            } else if (gs instanceof GuiFluidCraftConfirm) {
                ((GuiFluidCraftConfirm) gs).postUpdate((List) message.list, message.ref);
            } else if (gs instanceof GuiLevelMaintainer) {
                ((GuiLevelMaintainer) gs).postUpdate((List) message.list);
            }
            return null;
        }
    }
}
