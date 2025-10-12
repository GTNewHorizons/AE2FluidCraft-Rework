package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.gui.container.ContainerFluidStorageBus;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemTerminal;
import com.glodblock.github.inventory.item.IWirelessExtendCard;
import com.glodblock.github.inventory.item.IWirelessMagnetFilter;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketFluidPatternTermBtns implements IMessage {

    private String Name = "";
    private String Value = "";

    public CPacketFluidPatternTermBtns(final String name, final String value) {
        Name = name;
        Value = value;
    }

    public CPacketFluidPatternTermBtns(final String name, final Integer value) {
        Name = name;
        Value = value.toString();
    }

    public CPacketFluidPatternTermBtns(final String name, final boolean value) {
        this(name, value ? 1 : 0);
    }

    public CPacketFluidPatternTermBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int leName = buf.readInt();
        int leVal = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        Name = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < leVal; i++) {
            sb.append(buf.readChar());
        }
        Value = sb.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Name.length());
        buf.writeInt(Value.length());
        for (int i = 0; i < Name.length(); i++) {
            buf.writeChar(Name.charAt(i));
        }
        for (int i = 0; i < Value.length(); i++) {
            buf.writeChar(Value.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidPatternTermBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidPatternTermBtns message, MessageContext ctx) {
            String Name = message.Name;
            String Value = message.Value;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            final EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (Name.startsWith("WirelessTerminal.") && (c instanceof FCBaseContainer)) {
                final FCBaseContainer cpt = (FCBaseContainer) c;
                final IItemTerminal itemTerminal = (IItemTerminal) cpt.getHost();
                final IWirelessExtendCard iwt = (IWirelessExtendCard) itemTerminal;
                switch (Name) {
                    case "WirelessTerminal.Stock":
                        iwt.setRestock(!iwt.isRestock());
                        break;
                    case "WirelessTerminal.MagnetMode":
                        iwt.setMagnetCardNextMode();
                        break;
                    case "WirelessTerminal.OpenMagnet":
                        InventoryHandler.openGui(
                                player,
                                player.worldObj,
                                new BlockPos(
                                        cpt.getPortableCell().getInventorySlot(),
                                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                        -1),
                                ForgeDirection.UNKNOWN,
                                GuiType.WIRELESS_MAGNET_FILTER);
                        break;
                    case "WirelessTerminal.magnet.NBT":
                        ((IWirelessMagnetFilter) itemTerminal).setNBTMode(Value.equals("1"));
                        break;
                    case "WirelessTerminal.magnet.Meta":
                        ((IWirelessMagnetFilter) itemTerminal).setMetaMode(Value.equals("1"));
                        break;
                    case "WirelessTerminal.magnet.Ore":
                        ((IWirelessMagnetFilter) itemTerminal).setOreMode(Value.equals("1"));
                        break;
                    case "WirelessTerminal.magnet.OreDictState":
                        ((IWirelessMagnetFilter) itemTerminal).setOreDictMode(Value.equals("1"));
                        break;
                    case "WirelessTerminal.magnet.OreDictFilter":
                        ((IWirelessMagnetFilter) itemTerminal).setOreDictFilter(Value);
                        break;
                    case "WirelessTerminal.magnet.FilterMode":
                        ((IWirelessMagnetFilter) itemTerminal).setListMode(
                                Value.equals("1") ? WirelessMagnet.ListMode.WhiteList
                                        : WirelessMagnet.ListMode.BlackList);
                        break;
                    case "WirelessTerminal.magnet.clear":
                        ((IWirelessMagnetFilter) itemTerminal).clearConfig();
                        break;
                }
                itemTerminal.saveSettings();
            } else if (Name.startsWith("StorageBus.") && c instanceof ContainerFluidStorageBus ccw) {
                if (Name.equals("StorageBus.Action")) {
                    if (Value.equals("Partition")) {
                        ccw.partition();
                    } else if (Value.equals("Clear")) {
                        ccw.clear();
                    }
                }
            }
            return null;
        }
    }
}
