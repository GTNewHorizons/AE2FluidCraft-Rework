package com.glodblock.github.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;

import appeng.api.parts.ILevelEmitter;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.PrimaryGui;
import appeng.container.interfaces.IContainerSubGui;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
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
        RENAME
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
            case EDIT, RENAME -> {
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
            case EDIT, RENAME -> {
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

            if (Objects.requireNonNull(message.action) == Action.EDIT) {
                if (player.openContainer instanceof AEBaseContainer abc) {
                    final PrimaryGui pGui = abc.createPrimaryGui();

                    TileEntity tile = DimensionManager.getWorld(message.dim)
                            .getTileEntity(message.x, message.y, message.z);

                    if (tile instanceof IPartHost host) {
                        IPart part = host.getPart(message.side);
                        if (part instanceof ILevelEmitter) {
                            InventoryHandler.openGui(
                                    player,
                                    tile.getWorldObj(),
                                    new BlockPos(tile),
                                    message.side,
                                    GuiType.LEVEL_EMITTER_PROXY);
                        }
                    } else {
                        InventoryHandler.openGui(
                                player,
                                tile.getWorldObj(),
                                new BlockPos(tile),
                                message.side,
                                GuiType.LEVEL_MAINTAINER);
                    }

                    if (player.openContainer instanceof IContainerSubGui sg) {
                        sg.setPrimaryGui(pGui);
                    }
                }
            } else if (Objects.requireNonNull(message.action) == Action.RENAME) {
                if (player.openContainer instanceof AEBaseContainer abc) {
                    final PrimaryGui pGui = abc.createPrimaryGui();

                    TileEntity tile = DimensionManager.getWorld(message.dim)
                            .getTileEntity(message.x, message.y, message.z);

                    Platform.openGUI(player, tile, message.side, GuiBridge.GUI_RENAMER);

                    if (player.openContainer instanceof IContainerSubGui sg) {
                        sg.setPrimaryGui(pGui);
                    }
                }
            }
            return null;
        }

    }
}
