package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IClickableInTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketLevelTerminalCommands implements IMessage {

    private Action action;
    private int x;
    private int y;
    private int z;
    private int dim;
    private ForgeDirection side;
    private int originalGui = -1;

    public enum Action {
        EDIT,
        BACK,
        ENABLE,
        DISABLE,
        ENABLE_ALL,
        DISABLE_ALL,
    }

    public CPacketLevelTerminalCommands() {}

    public CPacketLevelTerminalCommands(Action action, int x, int y, int z, int dim, ForgeDirection side) {
        this.action = action;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.side = side;
    }

    public CPacketLevelTerminalCommands setOriginalGui(int originalGui) {
        this.originalGui = originalGui;
        return this;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = Action.values()[buf.readInt()];
        switch (action) {
            case EDIT -> {
                x = buf.readInt();
                y = buf.readInt();
                z = buf.readInt();
                dim = buf.readInt();
                side = ForgeDirection.getOrientation(buf.readInt());
            }
            case BACK -> {
                x = buf.readInt();
                y = buf.readInt();
                z = buf.readInt();
                dim = buf.readInt();
                side = ForgeDirection.getOrientation(buf.readInt());
                originalGui = buf.readInt();
            }
            case ENABLE -> {}
            case DISABLE -> {}
            case ENABLE_ALL -> {}
            case DISABLE_ALL -> {}
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        switch (action) {
            case EDIT -> {
                buf.writeInt(x);
                buf.writeInt(y);
                buf.writeInt(z);
                buf.writeInt(dim);
                buf.writeInt(side.ordinal());
            }
            case BACK -> {
                buf.writeInt(x);
                buf.writeInt(y);
                buf.writeInt(z);
                buf.writeInt(dim);
                buf.writeInt(side.ordinal());
                buf.writeInt(originalGui);
            }
            case ENABLE -> {}
            case DISABLE -> {}
            case ENABLE_ALL -> {}
            case DISABLE_ALL -> {}
        }
    }

    public static class Handler implements IMessageHandler<CPacketLevelTerminalCommands, IMessage> {

        @Override
        public IMessage onMessage(CPacketLevelTerminalCommands message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            AEBaseContainer con = (AEBaseContainer) player.openContainer;
            switch (message.action) {
                case EDIT -> {
                    TileEntity tile = DimensionManager.getWorld(message.dim)
                            .getTileEntity(message.x, message.y, message.z);
                    InventoryHandler.openGui(
                            player,
                            player.worldObj,
                            new BlockPos(tile),
                            message.side,
                            GuiType.LEVEL_MAINTAINER);
                }
                case BACK -> {
                    GuiType originalGui = GuiType.getByOrdinal(message.originalGui);
                    if (originalGui == null) {
                        return null;
                    }

                    InventoryHandler.openGui(
                            player,
                            player.worldObj,
                            new BlockPos(message.x, message.y, message.z, DimensionManager.getWorld(message.dim)),
                            message.side,
                            originalGui);
                }
                case ENABLE -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                    }
                }
                case DISABLE -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                    }
                }
                case ENABLE_ALL -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                    }
                }
                case DISABLE_ALL -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                    }
                }
            }
            return null;
        }

    }
}
