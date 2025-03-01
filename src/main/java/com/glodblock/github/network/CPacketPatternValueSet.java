package com.glodblock.github.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.glodblock.github.client.gui.container.ContainerPatternValueAmount;
import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.gui.PartOrItemGuiFactory;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.api.networking.IGridHost;
import appeng.container.ContainerOpenContext;
import appeng.container.slot.SlotFake;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketPatternValueSet implements IMessage {

    private GuiType originGui;
    private long amount;
    private int valueIndex;

    public CPacketPatternValueSet() {
        // NO-OP
    }

    public CPacketPatternValueSet(int originalGui, long amount, int valueIndex) {
        this.originGui = GuiType.getByOrdinal(originalGui);
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(originGui.ordinal());
        buf.writeLong(amount);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.originGui = GuiType.getByOrdinal(buf.readInt());
        this.amount = buf.readLong();
        this.valueIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<CPacketPatternValueSet, IMessage> {

        @Override
        public IMessage onMessage(CPacketPatternValueSet message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerPatternValueAmount cpv) {
                final Object target = cpv.getTarget();
                if (target instanceof IGridHost) {
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(te),
                                    Objects.requireNonNull(context.getSide()),
                                    message.originGui);
                        } else if (message.originGui.guiFactory instanceof PartOrItemGuiFactory) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(
                                            player.inventory.currentItem,
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            0),
                                    Objects.requireNonNull(context.getSide()),
                                    message.originGui);
                        } else if (target instanceof IWirelessTerminal) {
                            InventoryHandler.openGui(
                                    player,
                                    player.worldObj,
                                    new BlockPos(((IWirelessTerminal) target).getInventorySlot(), 0, 0),
                                    Objects.requireNonNull(context.getSide()),
                                    message.originGui);
                        }
                        if (player.openContainer instanceof FCContainerEncodeTerminal fcet) {
                            Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot instanceof SlotFake sf) {
                                ItemStack stack = sf.getStack();
                                if (Util.isFluidPacket(stack)) {
                                    ItemFluidPacket.setFluidAmount(stack, message.amount);
                                    sf.putStack(stack);
                                } else {
                                    sf.getAEStack().setStackSize(message.amount);
                                }
                                fcet.onCraftMatrixChanged(slot.inventory);
                                fcet.onSlotChange(slot);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
}
