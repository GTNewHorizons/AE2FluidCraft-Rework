package com.glodblock.github.network;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.UltraTerminalModes;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomButtonProvider;
import appeng.util.Platform;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSwitchGuis implements IMessage {

    private UltraTerminalModes mode;
    private boolean switchTerminal;

    public CPacketSwitchGuis(UltraTerminalModes mode) {
        this(mode, false);
    }

    public CPacketSwitchGuis(UltraTerminalModes mode, boolean switchTerminal) {
        this.mode = mode;
        this.switchTerminal = switchTerminal;
    }

    public CPacketSwitchGuis() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        final int ord = byteBuf.readInt();
        mode = ord != -1 ? UltraTerminalModes.values()[ord] : null;
        switchTerminal = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(mode != null ? mode.ordinal() : -1);
        byteBuf.writeBoolean(this.switchTerminal);
    }

    public static class Handler implements IMessageHandler<CPacketSwitchGuis, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketSwitchGuis message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            Container cont = player.openContainer;

            // switch terminal

            if (message.switchTerminal) {
                ImmutablePair<Integer, ItemStack> temp = Util.getUltraWirelessTerm(player);
                if (temp != null && temp.getRight().getItem() instanceof ItemWirelessUltraTerminal iwut) {
                    if (message.mode != null && cont instanceof AEBaseContainer abc) {
                        abc.setSwitchAbleGuiNext(message.mode.ordinal());

                    }
                    iwut.switchTerminal(player, temp.getRight(), message.mode);
                    if (player.openContainer instanceof AEBaseContainer abc) {
                        if (abc.getTarget() instanceof ICustomButtonProvider icbp) {
                            FluidCraft.proxy.netHandler
                                    .sendTo(new SPacketCustomButtonUpdate(icbp.getDataObject()), player);
                        }
                    }
                }
            } else {
                // open new terminal //TODO in world stuff
                if (cont instanceof AEBaseContainer aeBaseContainer) {
                    ContainerOpenContext context = aeBaseContainer.getOpenContext();
                    if (context == null) return null;
                    TileEntity te = context.getTile();
                    Object guiType = GuiType.GUI_SUPER_STOCK_REPLENISHER;
                    if (te != null) {
                        if (guiType instanceof GuiType gt) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(te),
                                    Objects.requireNonNull(context.getSide()),
                                    gt);
                        } else if (guiType instanceof GuiBridge gb) Platform.openGUI(player, te, context.getSide(), gb);
                    } else if (aeBaseContainer.getTarget() instanceof IWirelessTerminal wt) {
                        if (guiType instanceof GuiType gt) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(
                                            wt.getInventorySlot(),
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            0),
                                    Objects.requireNonNull(context.getSide()),
                                    gt);
                        } else if (guiType instanceof GuiBridge gb) Platform.openGUI(player, null, null, gb);
                    }
                }
            }
            return null;
        }
    }

}
