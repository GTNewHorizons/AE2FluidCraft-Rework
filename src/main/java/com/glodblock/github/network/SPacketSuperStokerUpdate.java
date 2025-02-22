package com.glodblock.github.network;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEStack;
import com.glodblock.github.client.gui.GuiEssentiaTerminal;
import com.glodblock.github.client.gui.GuiFluidIO;
import com.glodblock.github.client.gui.GuiFluidInterface;
import com.glodblock.github.client.gui.GuiFluidLevelEmitter;
import com.glodblock.github.client.gui.GuiFluidStorageBus;
import com.glodblock.github.client.gui.GuiFluidTerminal;
import com.glodblock.github.client.gui.GuiIngredientBuffer;
import com.glodblock.github.client.gui.GuiLargeIngredientBuffer;
import com.glodblock.github.client.gui.GuiSuperStoker;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SPacketSuperStokerUpdate implements IMessage {

    private Map<Integer, IAEStack<?>> list;

    public SPacketSuperStokerUpdate() {}

    public SPacketSuperStokerUpdate(Map<Integer, IAEStack<?>> data) {
        this.list = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.list = new HashMap<>();
        try {
            Util.readAEStackMapFromBuf(this.list, buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            Util.writeAEStackMapToBuf(this.list, buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<SPacketSuperStokerUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketSuperStokerUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiSuperStoker gss) {
                gss.update(message.list);
            }
            return null;
        }
    }
}
