package com.glodblock.github.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

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
    private int amount;
    private int valueIndex;

    public CPacketPatternValueSet() {
        // NO-OP
    }

    public CPacketPatternValueSet(int originalGui, int amount, int valueIndex) {
        this.originGui = GuiType.getByOrdinal(originalGui);
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(originGui.ordinal());
        buf.writeInt(amount);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.originGui = GuiType.getByOrdinal(buf.readInt());
        this.amount = buf.readInt();
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
                        if (player.openContainer instanceof FCContainerEncodeTerminal) {
                            Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot instanceof SlotFake) {
                                ItemStack stack = slot.getStack().copy();
                                if (Util.isFluidPacket(stack)) {
                                    FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
                                    if (fluidStack != null) {
                                        fluidStack = ItemFluidPacket.getFluidStack(stack).copy();
                                        fluidStack.amount = message.amount;
                                    }
                                    slot.putStack(ItemFluidPacket.newStack(fluidStack));
                                } else {
                                    stack.stackSize = message.amount;
                                    slot.putStack(stack);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
}
