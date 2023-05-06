package com.glodblock.github.network;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSwitchGuis implements IMessage {

    private GuiType guiType;
    private boolean switchTerminal;

    public CPacketSwitchGuis(GuiType guiType) {
        this(guiType, false);
    }

    public CPacketSwitchGuis(GuiType guiType, boolean switchTerminal) {
        this.guiType = guiType;
        this.switchTerminal = switchTerminal;
    }

    public CPacketSwitchGuis() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        guiType = GuiType.getByOrdinal(byteBuf.readByte());
        switchTerminal = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeByte(guiType != null ? guiType.ordinal() : 0);
        byteBuf.writeBoolean(this.switchTerminal);
    }

    public static class Handler implements IMessageHandler<CPacketSwitchGuis, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketSwitchGuis message, MessageContext ctx) {
            if (message.guiType == null) {
                return null;
            }

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            Container cont = player.openContainer;

            // switch terminal
            if (message.switchTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(player, message.guiType);
                return null;
            }

            // open new terminal
            if (cont instanceof AEBaseContainer) {
                ContainerOpenContext context = ((AEBaseContainer) cont).getOpenContext();
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
                } else if (((AEBaseContainer) cont).getTarget() instanceof IWirelessTerminal) {
                    InventoryHandler.openGui(
                            player,
                            player.worldObj,
                            new BlockPos(
                                    ((IWirelessTerminal) ((AEBaseContainer) cont).getTarget()).getInventorySlot(),
                                    Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                    0),
                            Objects.requireNonNull(context.getSide()),
                            message.guiType);
                }
            }
            return null;
        }
    }

}
