package com.glodblock.github.common.block;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.render.RenderBlockFluidInterface;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.util.NameConst;

import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseItemBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidInterface extends FCBaseBlock {

    private static final String[] COLORED_TEXTURES = { "fluid_interface", "fluid_interface_a",
            "fluid_interface_arrow" };

    @SideOnly(Side.CLIENT)
    private IIcon[][] coloredTextures;

    public BlockFluidInterface() {
        super(Material.iron, NameConst.BLOCK_FLUID_INTERFACE);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidInterface.class);
        setFeature(EnumSet.of(AEFeature.Core));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockFluidInterface getRenderer() {
        return new RenderBlockFluidInterface();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(final IIconRegister iconRegistry) {
        super.registerBlockIcons(iconRegistry);
        this.coloredTextures = new IIcon[COLORED_TEXTURES.length][AEColor.VALUES.length];

        for (final AEColor color : AEColor.VALID_COLORS) {
            for (int id = 0; id < COLORED_TEXTURES.length; id++) {
                this.coloredTextures[id][color.ordinal()] = iconRegistry
                        .registerIcon("ae2fc:interface/" + COLORED_TEXTURES[id] + "_" + color.name());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getColoredTexture(final int id, final AEColor color) {
        return this.coloredTextures[id][color.ordinal()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        final TileInterface tile = this.getTileEntity(world, x, y, z);
        if (tile != null && tile.getForward() == ForgeDirection.UNKNOWN) {
            final AEColor color = tile.getProxy().getColor();
            if (color != AEColor.Transparent) {
                return this.getColoredTexture(0, color);
            }
        }
        return super.getIcon(world, x, y, z, side);
    }

    @Override
    public boolean onActivated(final World world, final int x, final int y, final int z, final EntityPlayer player,
            final int facing, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileInterface tg = this.getTileEntity(world, x, y, z);
        if (tg != null) {
            if (Platform.isServer()) {
                Platform.openGUI(player, tg, ForgeDirection.getOrientation(facing), GuiBridge.GUI_INTERFACE);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        TileFluidInterface tile = this.getTileEntity(worldIn, x, y, z);
        if (tile != null) {
            tile.getInterfaceDuality().updateRedstoneState();
        }
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final ForgeDirection axis) {
        if (rotatable instanceof TileInterface) {
            ((TileInterface) rotatable).setSide(axis);
        }
    }

    @Override
    public BlockFluidInterface register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_FLUID_INTERFACE);
        GameRegistry.registerTileEntity(TileFluidInterface.class, NameConst.BLOCK_FLUID_INTERFACE);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
