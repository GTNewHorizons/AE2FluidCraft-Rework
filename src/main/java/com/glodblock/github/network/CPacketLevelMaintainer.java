package com.glodblock.github.network;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketLevelMaintainer implements IMessage {

    private String action;
    private long size;
    private int slotIndex;

    public CPacketLevelMaintainer() {}

    public CPacketLevelMaintainer(String action, int slotIndex) {
        this.action = action;
        this.size = 0;
        this.slotIndex = slotIndex;
    }

    public CPacketLevelMaintainer(String action) {
        this.action = action;
        this.size = 0;
        this.slotIndex = 0;
    }

    public CPacketLevelMaintainer(String action, int slotIndex, long size) {
        this.action = action;
        this.size = size;
        this.slotIndex = slotIndex;
    }

    public CPacketLevelMaintainer(String action, int slotIndex, String size) {
        this.action = action;
        this.slotIndex = slotIndex;
        this.size = size.isEmpty() ? 0 : Long.parseLong(size);
    }

    public static IAEItemStack setTag(IAEItemStack ias, long batch, int slotIndex, boolean enable, int state) {
        NBTTagCompound data = new NBTTagCompound();
        ItemStack is = ias.getItemStack();
        data.setLong("Batch", batch);
        data.setLong("Index", slotIndex);
        data.setBoolean("Enable", enable);
        data.setInteger("State", state);
        is.setTagCompound(data);
        IAEItemStack iaeItemStack = AEItemStack.create(is);
        iaeItemStack.setStackSize(ias.getStackSize());
        return iaeItemStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int leAction = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leAction; i++) {
            sb.append(buf.readChar());
        }
        this.action = sb.toString();
        this.slotIndex = buf.readInt();
        this.size = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action.length());
        for (int i = 0; i < this.action.length(); i++) {
            buf.writeChar(this.action.charAt(i));
        }
        buf.writeInt(this.slotIndex);
        buf.writeLong(this.size);
    }

    public static class Handler implements IMessageHandler<CPacketLevelMaintainer, IMessage> {

        private void refresh(ContainerLevelMaintainer cca, EntityPlayerMP player) {
            List<IAEItemStack> toSend = new ArrayList<>(TileLevelMaintainer.REQ_COUNT);
            for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                IAEItemStack is = cca.getTile().requests.getRequestQtyStack(i);
                IAEItemStack is1 = cca.getTile().requests.getRequestBatches().getStack(i);
                if (is != null) {
                    if (is1 != null) {
                        NBTTagCompound data;
                        data = is1.getItemStack().getTagCompound();
                        toSend.add(
                                setTag(
                                        is,
                                        is1.getStackSize(),
                                        i,
                                        data.getBoolean("Enable"),
                                        cca.getTile().requests.getState(i).ordinal()));
                    } else {
                        toSend.add(setTag(is, 0, i, true, 0));
                    }
                }
            }
            SPacketMEUpdateBuffer.scheduleItemUpdate(player, toSend);
        }

        @Nullable
        @Override
        public IMessage onMessage(CPacketLevelMaintainer message, MessageContext ctx) {
            if (message.action.startsWith("TileLevelMaintainer.")
                    && ctx.getServerHandler().playerEntity.openContainer instanceof final ContainerLevelMaintainer cca) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                switch (message.action) {
                    case "TileLevelMaintainer.Quantity" -> cca.getTile()
                            .updateQuantity(message.slotIndex, message.size);
                    case "TileLevelMaintainer.Batch" -> cca.getTile().updateBatchSize(message.slotIndex, message.size);
                    case "TileLevelMaintainer.Enable" -> cca.getTile().setRequestStatus(message.slotIndex, false);
                    case "TileLevelMaintainer.Disable" -> cca.getTile().setRequestStatus(message.slotIndex, true);
                }
                this.refresh(cca, player);
            }
            return null;
        }
    }
}
