package com.glodblock.github.network;

import net.minecraft.inventory.Container;

import com.glodblock.github.client.gui.container.ContainerSuperStockReplenisher;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.gtnewhorizons.angelica.shadow.javax.annotation.Nullable;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSuperStockReplenisherUpdate implements IMessage {

    private boolean isFullStockMode;

    public CPacketSuperStockReplenisherUpdate() {}

    public CPacketSuperStockReplenisherUpdate(boolean isFullStockMode) {
        this.isFullStockMode = isFullStockMode;
    }

    public boolean isFullStockMode() {
        return this.isFullStockMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isFullStockMode = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isFullStockMode);
    }

    public static class Handler implements IMessageHandler<CPacketSuperStockReplenisherUpdate, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketSuperStockReplenisherUpdate message, MessageContext ctx) {
            final Container c = ctx.getServerHandler().playerEntity.openContainer;

            if (c instanceof ContainerSuperStockReplenisher cssr
                    && cssr.getTarget() instanceof TileSuperStockReplenisher tile) {
                tile.setFullStockMode(message.isFullStockMode);
                cssr.forceUpdate();
                tile.markDirty();
                cssr.detectAndSendChanges();
            }

            return null;
        }
    }

}
