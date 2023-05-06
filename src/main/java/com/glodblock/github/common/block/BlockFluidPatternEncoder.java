package com.glodblock.github.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockFluidPatternEncoder extends FCBaseBlock {

    public BlockFluidPatternEncoder() {
        super(Material.iron, NameConst.BLOCK_FLUID_PATTERN_ENCODER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidPatternEncoder.class);
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileFluidPatternEncoder tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.FLUID_PATTERN_ENCODER);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockFluidPatternEncoder register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_FLUID_PATTERN_ENCODER);
        GameRegistry.registerTileEntity(TileFluidPatternEncoder.class, NameConst.BLOCK_FLUID_PATTERN_ENCODER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
