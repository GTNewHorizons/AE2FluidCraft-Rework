package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IClickableInTerminal;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.api.implementations.IUpgradeableHost;
import appeng.container.AEBaseContainer;
import appeng.helpers.ICustomNameObject;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketRenamer implements IMessage {

    private int x;
    private int y;
    private int z;
    private int dim;
    private ForgeDirection side;
    private Action action;
    private String text;

    public enum Action {
        OPEN,
        GET_TEXT,
        SET_TEXT,
    }

    public CPacketRenamer() {}

    public CPacketRenamer(String text) {
        this.action = Action.SET_TEXT;
        this.text = text;
    }

    public CPacketRenamer(Action a) {
        this.action = a;
    }

    public CPacketRenamer(int x, int y, int z, int dim, ForgeDirection side) {
        this.action = Action.OPEN;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.side = side;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = Action.values()[buf.readInt()];
        if (this.action == Action.OPEN) {
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            this.dim = buf.readInt();
            this.side = ForgeDirection.getOrientation(buf.readInt());
        } else if (this.action == Action.SET_TEXT) {
            int leName = buf.readInt();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < leName; i++) {
                sb.append(buf.readChar());
            }
            this.text = sb.toString();
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action.ordinal());
        if (this.action == Action.OPEN) {
            buf.writeInt(this.x);
            buf.writeInt(this.y);
            buf.writeInt(this.z);
            buf.writeInt(this.dim);
            buf.writeInt(side.ordinal());
        } else if (this.action == Action.SET_TEXT) {
            buf.writeInt(this.text.length());
            for (int i = 0; i < this.text.length(); i++) {
                buf.writeChar(this.text.charAt(i));
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketRenamer, IMessage> {

        private String getName(ICustomNameObject obj) {
            return obj.hasCustomName() ? obj.getCustomName() : "";
        }

        private String getName(TileEntity tile, ForgeDirection side) {
            if (tile instanceof TileCableBus) {
                return getName((ICustomNameObject) ((TileCableBus) tile).getPart(side));
            } else {
                return getName((ICustomNameObject) tile);
            }
        }

        private void setName(TileEntity tile, ForgeDirection side, String text) {
            if (tile instanceof TileCableBus) {
                ((ICustomNameObject) ((TileCableBus) tile).getPart(side)).setCustomName(text);
            } else {
                ((ICustomNameObject) tile).setCustomName(text);
            }
        }

        @Override
        public IMessage onMessage(CPacketRenamer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            AEBaseContainer con = (AEBaseContainer) player.openContainer;
            switch (message.action) {
                case OPEN -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        TileEntity tile = DimensionManager.getWorld(message.dim)
                                .getTileEntity(message.x, message.y, message.z);
                        if (!(tile instanceof ICustomNameObject)) {
                            break;
                        }

                        String name = getName(tile, message.side);
                        clickableInterface.setClickedInterface(
                                new Util.DimensionalCoordSide(
                                        message.x,
                                        message.y,
                                        message.z,
                                        message.dim,
                                        message.side,
                                        name));

                        if (con.getTarget() instanceof IWirelessTerminal terminal) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(
                                            terminal.getInventorySlot(),
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            1),
                                    message.side,
                                    GuiType.RENAMER);
                        } else if (con.getTarget() instanceof IUpgradeableHost host) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(host.getTile()),
                                    con.getOpenContext().getSide(),
                                    GuiType.RENAMER);
                        }
                    }
                }
                case GET_TEXT -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                        FluidCraft.proxy.netHandler
                                .sendTo(new SPacketStringUpdate(this.getName(tile, intMsg.getSide())), player);
                    }
                }
                case SET_TEXT -> {
                    if (con.getTarget() instanceof IClickableInTerminal clickableInterface) {
                        Util.DimensionalCoordSide intMsg = clickableInterface.getClickedInterface();
                        TileEntity tile = DimensionManager.getWorld(intMsg.getDimension())
                                .getTileEntity(intMsg.x, intMsg.y, intMsg.z);
                        this.setName(tile, intMsg.getSide(), message.text);

                        FluidCraft.proxy.netHandler.sendTo(new SPacketSwitchBack(), player);
                    }
                }
            }
            return null;
        }

    }
}
