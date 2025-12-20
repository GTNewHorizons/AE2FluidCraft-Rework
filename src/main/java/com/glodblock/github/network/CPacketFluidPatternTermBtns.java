package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.UltraTerminalButtons;
import com.glodblock.github.client.gui.container.ContainerMagnetFilter;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessMagnetFilter;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import appeng.container.PrimaryGui;
import appeng.container.interfaces.IContainerSubGui;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.helpers.ICustomButtonProvider;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketFluidPatternTermBtns implements IMessage {

    private Command command;
    private String value = "";

    public enum Command {
        RESTOCK,
        MAGNET_MODE,
        MAGNET_OPEN_FILTER,
        MAGNET_FILTER_NBT,
        MAGNET_FILTER_META,
        MAGNET_FILTER_ORE,
        MAGNET_FILTER_OREDICT_STATE,
        MAGNET_FILTER_OREDICT_FILTER,
        MAGNET_FILTER_MODE,
        MAGNET_FILTER_CLEAR
    }

    public CPacketFluidPatternTermBtns(final Command command, final String value) {
        this.command = command;
        this.value = value;
    }

    public CPacketFluidPatternTermBtns(final Command command, final Integer value) {
        this.command = command;
        this.value = value.toString();
    }

    public CPacketFluidPatternTermBtns(final Command command, final boolean value) {
        this(command, value ? 1 : 0);
    }

    public CPacketFluidPatternTermBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.command = Command.values()[buf.readInt()];

        int leVal = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leVal; i++) {
            sb.append(buf.readChar());
        }
        this.value = sb.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.command.ordinal());

        buf.writeInt(this.value.length());
        for (int i = 0; i < this.value.length(); i++) {
            buf.writeChar(this.value.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidPatternTermBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidPatternTermBtns message, MessageContext ctx) {
            final Command command = message.command;
            final String value = message.value;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            if (c instanceof ContainerMagnetFilter cmf && cmf.getTarget() instanceof IWirelessMagnetFilter iwmf) {
                switch (command) {
                    case MAGNET_FILTER_NBT -> iwmf.setNBTMode(value.equals("1"));
                    case MAGNET_FILTER_META -> iwmf.setMetaMode(value.equals("1"));
                    case MAGNET_FILTER_ORE -> iwmf.setOreMode(value.equals("1"));
                    case MAGNET_FILTER_OREDICT_STATE -> iwmf.setOreDictMode(value.equals("1"));
                    case MAGNET_FILTER_OREDICT_FILTER -> iwmf.setOreDictFilter(value);
                    case MAGNET_FILTER_MODE -> iwmf.setListMode(value.equals("1"));
                    case MAGNET_FILTER_CLEAR -> iwmf.clearConfig();
                }
                iwmf.saveSettings();
            } else if (c instanceof AEBaseContainer abc && abc.getTarget() instanceof ICustomButtonProvider icbp) {

                switch (command) {
                    case RESTOCK -> {
                        if (icbp.getDataObject() instanceof UltraTerminalButtons utb) {
                            utb.toggleRestock();
                        }
                    }
                    case MAGNET_MODE -> {
                        if (icbp.getDataObject() instanceof UltraTerminalButtons utb) {
                            utb.toggleMagnetMode();
                        }
                    }
                    case MAGNET_OPEN_FILTER -> {
                        final PrimaryGui pGui = abc.createPrimaryGui();
                        final int nextGui = abc.getSwitchAbleGuiNext();

                        InventoryHandler.openGui(
                                player,
                                player.worldObj,
                                new BlockPos(
                                        ((IInventorySlotAware) abc.getTarget()).getInventorySlot(),
                                        Util.GuiHelper.encodeType(0, Util.GuiHelper.GuiType.ITEM),
                                        -1),
                                ForgeDirection.UNKNOWN,
                                GuiType.WIRELESS_MAGNET_FILTER);

                        if (player.openContainer instanceof IContainerSubGui sg) {
                            sg.setPrimaryGui(pGui);
                        }

                        if (player.openContainer instanceof AEBaseContainer ab) {
                            ab.setSwitchAbleGuiNext(nextGui);
                        }
                    }
                }

                icbp.writeCustomButtonData();

                FluidCraft.proxy.netHandler.sendTo(new SPacketCustomButtonUpdate(icbp.getDataObject()), player);
            }
            return null;
        }
    }
}
