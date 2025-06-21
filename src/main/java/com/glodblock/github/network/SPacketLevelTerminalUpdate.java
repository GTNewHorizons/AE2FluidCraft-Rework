package com.glodblock.github.network;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.api.registries.LevelItemInfo;
import com.glodblock.github.api.registries.LevelState;
import com.glodblock.github.client.gui.GuiLevelTerminal;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketLevelTerminalUpdate implements IMessage {

    private final List<PacketEntry> commands = new ArrayList<>();

    @Override
    public void fromBytes(ByteBuf buf) {
        int entryNum = buf.readInt();

        for (int i = 0; i < entryNum; i++) {
            PacketType type = PacketType.values()[buf.readByte()];

            switch (type) {
                case ADD -> this.commands.add(new PacketAdd(buf));
                case REMOVE -> this.commands.add(new PacketRemove(buf));
                case RENAME -> this.commands.add(new PacketRename(buf));
                case DISCONNECT -> this.commands.add(new PacketDisconnect(buf));
                case OVERWRITE_SLOT -> this.commands.add(new PacketOverwriteSlot(buf));
                case OVERWITE_ALL_SLOT -> this.commands.add(new PacketOverwirteAllSlot(buf));
            }
        }

        if (AEConfig.instance.isFeatureEnabled(AEFeature.PacketLogging)) {
            AELog.info(
                    " <- Received commands " + this.commands.size()
                            + " : "
                            + this.commands.stream().map(packetEntry -> packetEntry.getClass().getSimpleName())
                                    .collect(Collectors.groupingBy(String::new, Collectors.counting())));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(commands.size());
        for (PacketEntry entry : this.commands) {
            buf.writeByte(entry.getType().ordinal());
            entry.write(buf);
        }

        if (AEConfig.instance.isFeatureEnabled(AEFeature.PacketLogging)) {
            AELog.info(
                    " <- Sent commands " + this.commands.size()
                            + " : "
                            + this.commands.stream().map(packetEntry -> packetEntry.getClass().getSimpleName())
                                    .collect(Collectors.groupingBy(String::new, Collectors.counting())));
            if (AEConfig.instance.isFeatureEnabled(AEFeature.DebugLogging)) {
                AELog.info(" -> Sent commands: " + this.commands);
            }
        }
    }

    public void addNewEntry(long id, int x, int y, int z, int dim, int side, int rows, int rowSize, String name,
            LevelItemInfo[] infolist) {
        this.commands.add(new PacketAdd(id, x, y, z, dim, side, rows, rowSize, name, infolist));
    }

    public void addRemoveEntry(long id) {
        this.commands.add(new PacketRemove(id));
    }

    public void addRenameEntry(long id, String newName) {
        this.commands.add(new PacketRename(id, newName));
    }

    public void addDisconnectEntry() {
        this.commands.add(new PacketDisconnect());
    }

    public void addOverwriteSlotEntry(long id, int index, LevelItemInfo info) {
        this.commands.add(new PacketOverwriteSlot(id, index, info));
    }

    public void addOverwriteAllSlotEntry(long id, LevelItemInfo[] info) {
        this.commands.add(new PacketOverwirteAllSlot(id, info));
    }

    public static class Handler implements IMessageHandler<SPacketLevelTerminalUpdate, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketLevelTerminalUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

            if (gs instanceof GuiLevelTerminal levelTerminal) {
                levelTerminal.receivePackets(message.commands);
            }

            return null;
        }
    }

    protected enum PacketType {
        ADD,
        REMOVE,
        RENAME,
        DISCONNECT,
        OVERWRITE_SLOT,
        OVERWITE_ALL_SLOT,
    }

    public abstract static class PacketEntry {

        public final long entryId;

        protected PacketEntry(long entryId) {
            this.entryId = entryId;
        }

        public PacketEntry(ByteBuf buf) {
            this.entryId = buf.readLong();
        }

        protected abstract PacketType getType();

        protected void write(ByteBuf buf) {
            buf.writeLong(this.entryId);
        }
    }

    public static class PacketAdd extends PacketEntry {

        public int x, y, z, dim, side;
        public int rows, rowSize;
        public String name;
        public LevelItemInfo[] infolist;

        PacketAdd(long id, int x, int y, int z, int dim, int side, int rows, int rowSize, String name,
                LevelItemInfo[] infolist) {
            super(id);
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.side = side;
            this.rows = rows;
            this.rowSize = rowSize;
            this.name = name;
            this.infolist = infolist;
        }

        public PacketAdd(ByteBuf buf) {
            super(buf);
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            this.dim = buf.readInt();
            this.side = buf.readInt();
            this.rows = buf.readInt();
            this.rowSize = buf.readInt();
            this.name = ByteBufUtils.readUTF8String(buf);
            int infoSize = buf.readInt();
            infolist = new LevelItemInfo[infoSize];
            for (int i = 0; i < infoSize; i++) {
                if (!buf.readBoolean()) continue;
                ItemStack stack = ByteBufUtils.readItemStack(buf);
                long quantity = buf.readLong();
                long batchSize = buf.readLong();
                LevelState state = LevelState.values()[buf.readInt()];
                infolist[i] = new LevelItemInfo(stack, quantity, batchSize, state);
            }
        }

        @Override
        protected PacketType getType() {
            return PacketType.ADD;
        }

        @Override
        protected void write(ByteBuf buf) {
            super.write(buf);
            buf.writeInt(this.x);
            buf.writeInt(this.y);
            buf.writeInt(this.z);
            buf.writeInt(this.dim);
            buf.writeInt(this.side);
            buf.writeInt(this.rows);
            buf.writeInt(this.rowSize);
            ByteBufUtils.writeUTF8String(buf, this.name);
            buf.writeInt(this.infolist.length);
            for (int i = 0; i < this.infolist.length; i++) {
                LevelItemInfo info = this.infolist[i];
                buf.writeBoolean(info != null);
                if (info == null) continue;
                // System.out.println("Writing info: " + info.stack != null);
                ByteBufUtils.writeItemStack(buf, info.stack);
                buf.writeLong(info.quantity);
                buf.writeLong(info.batchSize);
                buf.writeInt(info.state.ordinal());
            }
        }
    }

    public static class PacketRemove extends PacketEntry {

        PacketRemove(long id) {
            super(id);
        }

        PacketRemove(ByteBuf buf) {
            super(buf);
        }

        @Override
        protected PacketType getType() {
            return PacketType.REMOVE;
        }

        @Override
        protected void write(ByteBuf buf) {
            super.write(buf);
        }
    }

    public static class PacketRename extends PacketEntry {

        public String newName;

        PacketRename(long id, String newName) {
            super(id);
            this.newName = newName;
        }

        PacketRename(ByteBuf buf) {
            super(buf);
            this.newName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        protected PacketType getType() {
            return PacketType.RENAME;
        }

        @Override
        protected void write(ByteBuf buf) {
            super.write(buf);
            ByteBufUtils.writeUTF8String(buf, this.newName);
        }
    }

    public static class PacketDisconnect extends PacketEntry {

        PacketDisconnect() {
            super(0); // Ignored id
        }

        PacketDisconnect(ByteBuf buf) {
            super(buf);
        }

        @Override
        protected PacketType getType() {
            return PacketType.DISCONNECT;
        }
    }

    public static class PacketOverwriteSlot extends PacketEntry {

        public int index;
        public LevelItemInfo info;

        PacketOverwriteSlot(long id, int index, LevelItemInfo info) {
            super(id);
            this.index = index;
            this.info = info;
        }

        PacketOverwriteSlot(ByteBuf buf) {
            super(buf);
            this.index = buf.readInt();
            if (buf.readBoolean()) {
                this.info = new LevelItemInfo(
                        ByteBufUtils.readItemStack(buf),
                        buf.readLong(),
                        buf.readLong(),
                        LevelState.values()[buf.readInt()]);
            } else {
                this.info = null;
            }
        }

        @Override
        protected PacketType getType() {
            return PacketType.OVERWRITE_SLOT;
        }

        @Override
        protected void write(ByteBuf buf) {
            super.write(buf);
            buf.writeInt(index);
            buf.writeBoolean(info != null);
            ByteBufUtils.writeItemStack(buf, info.stack);
            buf.writeLong(info.quantity);
            buf.writeLong(info.batchSize);
            buf.writeInt(info.state.ordinal());
        }
    }

    public static class PacketOverwirteAllSlot extends PacketEntry {

        public LevelItemInfo[] infoList;

        PacketOverwirteAllSlot(long id, LevelItemInfo[] info) {
            super(id);
            this.infoList = info;
        }

        PacketOverwirteAllSlot(ByteBuf buf) {
            super(buf);
            int infoSize = buf.readInt();
            this.infoList = new LevelItemInfo[infoSize];
            for (int i = 0; i < infoSize; i++) {
                if (!buf.readBoolean()) continue;
                ItemStack stack = ByteBufUtils.readItemStack(buf);
                long quantity = buf.readLong();
                long batchSize = buf.readLong();
                LevelState state = LevelState.values()[buf.readInt()];
                this.infoList[i] = new LevelItemInfo(stack, quantity, batchSize, state);
            }
        }

        @Override
        protected PacketType getType() {
            return PacketType.OVERWITE_ALL_SLOT;
        }

        @Override
        protected void write(ByteBuf buf) {
            super.write(buf);
            buf.writeInt(this.infoList.length);
            for (int i = 0; i < this.infoList.length; i++) {
                LevelItemInfo info = this.infoList[i];
                buf.writeBoolean(info != null);
                if (info == null) continue;
                buf.writeInt(i);
                ByteBufUtils.writeItemStack(buf, info.stack);
                buf.writeLong(info.quantity);
                buf.writeLong(info.batchSize);
                buf.writeInt(info.state.ordinal());
            }
        }
    }
}
