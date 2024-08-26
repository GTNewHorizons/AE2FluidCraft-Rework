package com.glodblock.github.network;

import java.util.Objects;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.BlockPos;

import appeng.api.config.CraftingMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.me.cache.CraftingGridCache;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketCraftRequest implements IMessage {

    private long amount;
    private boolean heldShift;

    private CraftingMode craftingMode;

    public CPacketCraftRequest() {}

    public CPacketCraftRequest(final int craftAmt, final boolean shift) {
        this(craftAmt, shift, CraftingMode.STANDARD);
    }

    public CPacketCraftRequest(final int craftAmt, final boolean shift, final CraftingMode craftingMode) {
        amount = craftAmt;
        heldShift = shift;
        this.craftingMode = craftingMode;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(amount);
        buf.writeBoolean(heldShift);
        buf.writeBoolean(this.craftingMode == CraftingMode.STANDARD);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readLong();
        heldShift = buf.readBoolean();
        craftingMode = buf.readBoolean() ? CraftingMode.STANDARD : CraftingMode.IGNORE_MISSING;
    }

    public static class Handler implements IMessageHandler<CPacketCraftRequest, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketCraftRequest message, MessageContext ctx) {
            if (ctx.getServerHandler().playerEntity.openContainer instanceof final ContainerCraftAmount cca) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                final Object target = cca.getTarget();
                if (target instanceof final IGridHost gh) {
                    final IGridNode gn = gh.getGridNode(ForgeDirection.UNKNOWN);

                    if (gn == null) {
                        return null;
                    }

                    final IGrid g = gn.getGrid();
                    if (g == null || cca.getItemToCraft() == null) {
                        return null;
                    }

                    cca.getItemToCraft().setStackSize(message.amount);

                    Future<ICraftingJob> futureJob = null;
                    try {
                        final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                        if(cg instanceof CraftingGridCache cgc) {
                        	futureJob = cgc.beginCraftingJob(
                                    cca.getWorld(),
                                    cca.getGrid(),
                                    cca.getActionSrc(),
                                    cca.getItemToCraft(),
                                    message.craftingMode,
                                    null);
                        }else {
                        	futureJob = cg.beginCraftingJob(
                        			cca.getWorld(),
                        			cca.getGrid(),
                        			cca.getActionSrc(),
                        			cca.getItemToCraft(),
                        			null);
                        }

                        final ContainerOpenContext context = cca.getOpenContext();
                        if (context != null) {
                            final TileEntity te = context.getTile();
                            if (te != null) {
                                InventoryHandler.openGui(
                                        player,
                                        player.worldObj,
                                        new BlockPos(te),
                                        Objects.requireNonNull(context.getSide()),
                                        GuiType.FLUID_CRAFTING_CONFIRM);
                            } else if (target instanceof IWirelessTerminal) {
                                InventoryHandler.openGui(
                                        player,
                                        player.worldObj,
                                        new BlockPos(((IWirelessTerminal) target).getInventorySlot(), 0, 0),
                                        Objects.requireNonNull(context.getSide()),
                                        GuiType.FLUID_CRAFTING_CONFIRM_ITEM);
                            }

                            if (player.openContainer instanceof final ContainerCraftConfirm ccc) {
                                ccc.setItemToCraft(cca.getItemToCraft());
                                ccc.setAutoStart(message.heldShift);
                                ccc.setJob(futureJob);
                                cca.detectAndSendChanges();
                            }
                        }
                    } catch (final Throwable e) {
                        if (futureJob != null) {
                            futureJob.cancel(true);
                        }
                        AELog.debug(e);
                    }
                }
            }
            return null;
        }
    }
}
