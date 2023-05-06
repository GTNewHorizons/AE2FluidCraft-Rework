package com.glodblock.github.common.block;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.IRegister;

import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class FCBaseBlock extends AEBaseTileBlock implements IRegister<FCBaseBlock> {

    public FCBaseBlock(Material mat, String name) {
        super(mat);
        this.setBlockName(name);
        this.setBlockTextureName(FluidCraft.MODID + ":" + name);
    }

    @Override
    public void setTileEntity(final Class<? extends TileEntity> clazz) {
        AEBaseTile.registerTileItem(clazz, new BlockStackSrc(this, 0, ActivityState.Enabled));
        super.setTileEntity(clazz);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public void setFeature(final EnumSet<AEFeature> f) {
        super.setFeature(f);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {}

    public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
            boolean advancedToolTips) {
        this.addInformation(itemStack, player, toolTip, advancedToolTips);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
