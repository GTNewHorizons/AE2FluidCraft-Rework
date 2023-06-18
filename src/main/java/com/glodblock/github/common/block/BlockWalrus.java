package com.glodblock.github.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.BaseItemBlockContainer;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileWalrus;
import com.glodblock.github.util.NameConst;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWalrus extends BaseBlockContainer {

    public BlockWalrus() {
        super(Material.clay);
        setHardness(2.0F);
        setResistance(10.0F);
        setBlockName(NameConst.BLOCK_WALRUS);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileWalrus();
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack) {
        int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }

        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        }

        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }

        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
        switch (ForgeDirection.getOrientation(blockAccess.getBlockMetadata(x, y, z))) {
            case NORTH -> setBlockBounds(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F);
            case EAST -> setBlockBounds(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F);
            case SOUTH -> setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F);
            case WEST -> setBlockBounds(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            default -> setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public BlockWalrus register() {
        GameRegistry.registerBlock(this, BaseItemBlockContainer.class, NameConst.BLOCK_WALRUS);
        GameRegistry.registerTileEntity(TileWalrus.class, NameConst.BLOCK_WALRUS);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {

        if (isShiftKeyDown()) {
            toolTip.add(NameConst.i18n(NameConst.TT_WALRUS_DESC));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
