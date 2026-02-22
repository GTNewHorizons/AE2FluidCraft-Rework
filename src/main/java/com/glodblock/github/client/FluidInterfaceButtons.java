package com.glodblock.github.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.input.Mouse;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.network.CPacketCustomButtonUpdate;
import com.glodblock.github.network.CPacketSwitchGuis;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.helpers.ICustomButtonDataObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class FluidInterfaceButtons implements ICustomButtonDataObject {

    private GuiImgButton sidelessMode;
    private GuiFCImgButton switcher;
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

        this.switcher = new GuiFCImgButton(guiLeft + 132 + 6, guiTop - 4, "SWITCH_FLUID_INTERFACE", "NORMAL", false) {

            @Override
            public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
                this.set("NORMAL");
                super.drawButton(mc, mouseX, mouseY);
            }
        };
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
