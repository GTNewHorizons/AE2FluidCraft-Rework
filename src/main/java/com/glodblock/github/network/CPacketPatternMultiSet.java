package com.glodblock.github.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.gui.container.ContainerPatternMulti;
import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.api.networking.IGridHost;
import appeng.container.ContainerOpenContext;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketPatternMultiSet implements IMessage {

    private static GuiType guiType;
    private static int multi;

    public CPacketPatternMultiSet(GuiType guiType, int multi) {
        CPacketPatternMultiSet.guiType = guiType;
        CPacketPatternMultiSet.multi = multi;
    }

    public CPacketPatternMultiSet() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        guiType = GuiType.getByOrdinal(byteBuf.readByte());
        multi = byteBuf.readInt();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(guiType != null ? guiType.ordinal() : 0);
        byteBuf.writeInt(multi);
    }

    public static class Handler implements IMessageHandler<CPacketPatternMultiSet, IMessage> {

        @Override
        public IMessage onMessage(CPacketPatternMultiSet message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerPatternMulti cpv) {
                final Object target = cpv.getTarget();
                if (target instanceof IGridHost) {
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    player,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(cpv.getOpenContext().getSide()),
                                    guiType);
                        } else if (target instanceof IWirelessTerminal wt) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(
                                            wt.getInventorySlot(),
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            0),
                                    ForgeDirection.UNKNOWN,
                                    guiType);
                        }
                        if (player.openContainer instanceof FCContainerEncodeTerminal cpt) {
                            cpt.multiplyOrDivideStacks(multi);
                        }
                    }
                }
            }
            return null;
        }
    }

}
