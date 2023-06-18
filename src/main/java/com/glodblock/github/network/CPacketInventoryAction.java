package com.glodblock.github.network;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerPatternValueAmount;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketInventoryAction implements IMessage {

    private InventoryAction action;
    private int slot;
    private long id;
    private IAEItemStack stack;
    private boolean isEmpty;

    public CPacketInventoryAction() {}

    public CPacketInventoryAction(final InventoryAction action, final int slot, final int id) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = null;
        this.isEmpty = true;
    }

    public CPacketInventoryAction(final InventoryAction action, final int slot, final int id, IAEItemStack stack) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = stack;
        this.isEmpty = stack == null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeInt(slot);
        buf.writeLong(id);
        buf.writeBoolean(isEmpty);
        if (!isEmpty) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = InventoryAction.values()[buf.readInt()];
        slot = buf.readInt();
        id = buf.readLong();
        isEmpty = buf.readBoolean();
        if (!isEmpty) {
            try {
                stack = AEItemStack.loadItemStackFromPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketInventoryAction, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryAction message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if (sender.openContainer instanceof final AEBaseContainer baseContainer) {
                Object target = baseContainer.getTarget();
                if (message.action == InventoryAction.AUTO_CRAFT) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    sender,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.FLUID_CRAFTING_AMOUNT);
                        } else if (target instanceof IWirelessTerminal) {
                            InventoryHandler.openGui(
                                    sender,
                                    sender.worldObj,
                                    new BlockPos(
                                            ((IWirelessTerminal) target).getInventorySlot(),
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            0),
                                    ForgeDirection.UNKNOWN,
                                    GuiType.FLUID_CRAFTING_AMOUNT);
                        }
                        if (sender.openContainer instanceof final ContainerCraftAmount cca) {
                            if (baseContainer.getTargetStack() != null) {
                                cca.getCraftingItem().putStack(baseContainer.getTargetStack().getItemStack());
                                cca.setItemToCraft(baseContainer.getTargetStack());
                            }
                            cca.detectAndSendChanges();
                        }
                    }
                } else if (message.action == InventoryAction.SET_PATTERN_VALUE) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null && message.stack != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    sender,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.PATTERN_VALUE_SET);
                        } else if (target instanceof IWirelessTerminal) {
                            InventoryHandler.openGui(
                                    sender,
                                    sender.worldObj,
                                    new BlockPos(
                                            ((IWirelessTerminal) target).getInventorySlot(),
                                            Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                            0),
                                    ForgeDirection.UNKNOWN,
                                    GuiType.PATTERN_VALUE_SET);
                        }
                        int amt = (int) message.stack.getStackSize();
                        if (message.stack.getItem() instanceof ItemFluidPacket) {
                            FluidStack fluid = ItemFluidPacket.getFluidStack(message.stack);
                            amt = fluid == null ? 1 : fluid.amount;
                        }
                        FluidCraft.proxy.netHandler.sendTo(new SPacketSetItemAmount(amt), sender);
                        if (sender.openContainer instanceof final ContainerPatternValueAmount cpv) {
                            if (baseContainer.getTargetStack() != null) {
                                cpv.setValueIndex(message.slot);
                                cpv.getPatternValue().putStack(baseContainer.getTargetStack().getItemStack());
                            }
                            cpv.detectAndSendChanges();
                        }
                    }
                } else {
                    baseContainer.doAction(sender, message.action, message.slot, message.id);
                }
            }
            return null;
        }
    }
}
