package com.glodblock.github.common.parts;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.base.FCPart;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IClickableInTerminal;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.core.sync.GuiBridge;

public class PartLevelTerminal extends FCPart implements IClickableInTerminal {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartLevelTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartLevelTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartLevelTerminal_Dark;

    private Util.DimensionalCoordSide tile;

    public PartLevelTerminal(ItemStack is) {
        super(is, true);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("clickedInterface")) {
            NBTTagCompound tileMsg = (NBTTagCompound) data.getTag("clickedInterface");
            this.tile = Util.DimensionalCoordSide.readFromNBT(tileMsg);
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.tile == null) return;

        NBTTagCompound tileMsg = new NBTTagCompound();
        tile.writeToNBT(tileMsg);
        data.setTag("clickedInterface", tileMsg);
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    @Override
    public boolean isBooting() {
        return super.isBooting();
    }

    @Override
    public GuiBridge getGui() {
        return GuiBridge.GUI_ME;
    }

    @Override
    public void setClickedInterface(Util.DimensionalCoordSide tile) {
        this.tile = tile;
        this.getHost().markForSave();
    }

    @Override
    public Util.DimensionalCoordSide getClickedInterface() {
        return this.tile;
    }

    @Override
    protected void openGui(EntityPlayer player) {
        InventoryHandler.openGui(
                player,
                this.getHost().getTile().getWorldObj(),
                new BlockPos(this.getHost().getTile()),
                Objects.requireNonNull(this.getSide()),
                GuiType.LEVEL_TERMINAL);
    }
}
