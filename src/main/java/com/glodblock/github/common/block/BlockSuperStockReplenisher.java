package com.glodblock.github.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.api.util.AEColor;
import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSuperStockReplenisher extends FCBaseBlock {

    @SideOnly(Side.CLIENT)
    private IIcon[] coloredTextures;

    public BlockSuperStockReplenisher() {
        super(Material.web, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        setTileEntity(TileSuperStockReplenisher.class);
        setOpaque(true);
        setFullBlock(true);
        this.lightOpacity = 4;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(final IIconRegister iconRegistry) {
        super.registerBlockIcons(iconRegistry);
        this.coloredTextures = new IIcon[AEColor.VALUES.length];

        for (final AEColor color : AEColor.VALID_COLORS) {
            this.coloredTextures[color.ordinal()] = iconRegistry
                    .registerIcon(this.getTextureName().replace(":", ":replenisher/") + "_" + color.name());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        final TileSuperStockReplenisher tile = this.getTileEntity(world, x, y, z);
        if (tile != null && tile.getColor() != AEColor.Transparent) {
            return this.coloredTextures[tile.getColor().ordinal()];
        }
        return super.getIcon(world, x, y, z, side);
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileSuperStockReplenisher tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.GUI_SUPER_STOCK_REPLENISHER);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockSuperStockReplenisher register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        GameRegistry.registerTileEntity(TileSuperStockReplenisher.class, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
