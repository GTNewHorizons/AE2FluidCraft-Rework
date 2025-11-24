package com.glodblock.github.client;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketCustomButtonUpdate;
import com.glodblock.github.network.CPacketSwitchGuis;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.helpers.ICustomButtonDataObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class FluidInterfaceButtons implements ICustomButtonDataObject {

    private GuiImgButton sidelessMode;
    private GuiTabButton switcher;
    private final boolean fullBlock;
    public SidelessMode sidelessSate;

    public FluidInterfaceButtons(final boolean fullBlock) {
        this.fullBlock = fullBlock;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initCustomButtons(int guiLeft, int guiTop, int xSize, int ySize, int xOffset, int yOffset,
            List<GuiButton> buttonList) {
        if (fullBlock) {
            this.sidelessMode = new GuiImgButton(
                    guiLeft - 18,
                    guiTop + yOffset,
                    Settings.SIDELESS_MODE,
                    SidelessMode.SIDELESS);
            buttonList.add(this.sidelessMode);
        }

        this.switcher = new GuiTabButton(
                guiLeft + 132,
                guiTop,
                !fullBlock ? ItemAndBlockHolder.FLUID_INTERFACE.stack() : ItemAndBlockHolder.INTERFACE.stack(),
                StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"),
                RenderItem.getInstance());
        buttonList.add(this.switcher);

        FluidCraft.proxy.netHandler.sendToServer(new CPacketCustomButtonUpdate());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean actionPerformedCustomButtons(final GuiButton btn) {
        if (btn == this.switcher) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis());
            return true;
        } else if (fullBlock && btn == this.sidelessMode) {
            final boolean backwards = Mouse.isButtonDown(1);
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.sidelessMode.getSetting(), backwards));
            FluidCraft.proxy.netHandler.sendToServer(new CPacketCustomButtonUpdate());
            return true;
        }

        return false;
    }

    @Override
    public void readData(NBTTagCompound tag) {}

    @Override
    public void writeData(NBTTagCompound tag) {}

    @Override
    public void readByte(ByteBuf buf) {
        this.sidelessMode.set(SidelessMode.values()[buf.readInt()]);
    }

    @Override
    public void writeByte(ByteBuf buf) {
        buf.writeInt(this.sidelessSate.ordinal());
    }

    public void setSidelessMode(SidelessMode sm) {
        this.sidelessSate = sm;
    }
}
