package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.FluidInterfaceButtons;
import com.glodblock.github.common.tile.TileFluidInterface;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.container.AEBaseContainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketCustomButtonUpdate implements IMessage {

    public CPacketCustomButtonUpdate() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<CPacketCustomButtonUpdate, IMessage> {

        @Override
        public IMessage onMessage(CPacketCustomButtonUpdate message, MessageContext ctx) {
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            if (c instanceof AEBaseContainer abc && abc.getTarget() instanceof TileFluidInterface tfi) {
                if (tfi.getDataObject() instanceof FluidInterfaceButtons fib) {
                    fib.setSidelessMode((SidelessMode) tfi.getConfigManager().getSetting(Settings.SIDELESS_MODE));
                    FluidCraft.proxy.netHandler.sendTo(new SPacketCustomButtonUpdate(fib), player);
                }
            }

            return null;
        }
    }
}
