package com.glodblock.github.network;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerPatternItemRenamer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketPatternItemRenamer implements IMessage {

    private GuiType guiType;
    private String name = "";
    private int valueIndex;

    public SPacketPatternItemRenamer() {}

    public SPacketPatternItemRenamer(GuiType guiType, String text, int valIndex) {
        this.guiType = guiType;
        name = text;
        valueIndex = valIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(guiType != null ? guiType.ordinal() : 0);
        buf.writeInt(valueIndex);

        buf.writeInt(name.length());
        for (int i = 0; i < name.length(); i++) {
            buf.writeChar(name.charAt(i));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        guiType = GuiType.getByOrdinal(buf.readByte());
        valueIndex = buf.readInt();

        int leName = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        name = sb.toString();
    }

    public static class Handler implements IMessageHandler<SPacketPatternItemRenamer, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketPatternItemRenamer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerPatternItemRenamer cpv) {
                ContainerOpenContext context = cpv.getOpenContext();
                if (context == null) {
                    return null;
                }
                TileEntity te = context.getTile();
                if (te != null) {
                    InventoryHandler.openGui(
                            player,
                            player.worldObj,
                            new BlockPos(te),
                            Objects.requireNonNull(context.getSide()),
                            message.guiType);
                } else if (cpv.getTarget() instanceof IWirelessTerminal iwt) {
                    InventoryHandler.openGui(
                            player,
                            player.worldObj,
                            new BlockPos(
                                    iwt.getInventorySlot(),
                                    Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                    0),
                            Objects.requireNonNull(context.getSide()),
                            message.guiType);
                }
                if (player.openContainer instanceof FCContainerEncodeTerminal) {
                    Slot slot = player.openContainer.getSlot(message.valueIndex);
                    if (slot != null && slot.getHasStack()) {
                        ItemStack nextStack = slot.getStack().copy();
                        nextStack.setRepairCost(2);
                        nextStack.setStackDisplayName(message.name);
                        slot.putStack(nextStack);
                    }
                }
            }
            return null;
        }
    }
}
