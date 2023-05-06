package com.glodblock.github.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.container.ContainerFluidMonitor;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketFluidUpdate implements IMessage {

    private Map<Integer, IAEFluidStack> list;
    private ItemStack itemStack;
    private int slotIndex;

    public CPacketFluidUpdate() {}

    public CPacketFluidUpdate(Map<Integer, IAEFluidStack> data, ItemStack itemStack) {
        this.list = data;
        this.itemStack = itemStack;
        this.slotIndex = -1;
    }

    public CPacketFluidUpdate(Map<Integer, IAEFluidStack> data) {
        this.list = data;
    }

    public CPacketFluidUpdate(Map<Integer, IAEFluidStack> data, ItemStack itemStack, int slotIndex) {
        this.list = data;
        this.itemStack = itemStack;
        this.slotIndex = slotIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.list = new HashMap<>();
        try {
            Util.readFluidMapFromBuf(this.list, buf);
            if (buf.readBoolean()) {
                this.itemStack = AEItemStack.loadItemStackFromPacket(buf).getItemStack();
                this.slotIndex = buf.readInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            Util.writeFluidMapToBuf(this.list, buf);
            if (this.itemStack != null) {
                buf.writeBoolean(true);
                AEItemStack.create(itemStack).writeToPacket(buf);
                buf.writeInt(this.slotIndex);
            } else {
                buf.writeBoolean(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidUpdate, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidUpdate message, MessageContext ctx) {
            Container container = ctx.getServerHandler().playerEntity.openContainer;
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (container instanceof ContainerFluidMonitor) {
                ItemStack item = player.inventory.getItemStack();
                ((ContainerFluidMonitor) container).postChange(
                        new ArrayList<>(message.list.values()),
                        message.itemStack == null ? item : message.itemStack,
                        player,
                        message.slotIndex);
            }
            return null;
        }
    }
}
