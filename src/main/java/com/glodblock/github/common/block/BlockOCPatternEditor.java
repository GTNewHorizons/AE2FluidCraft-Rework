package com.glodblock.github.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockOCPatternEditor extends FCBaseBlock {

    public BlockOCPatternEditor() {
        super(Material.iron, NameConst.BLOCK_OC_PATTERN_EDITOR);
        setTileEntity(TileOCPatternEditor.class);
        setFullBlock(true);
        setOpaque(true);
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileOCPatternEditor tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.OC_PATTERN_EDITOR);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockOCPatternEditor register() {
        if (ModAndClassUtil.OC) {
            GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_OC_PATTERN_EDITOR);
            GameRegistry.registerTileEntity(TileOCPatternEditor.class, NameConst.BLOCK_OC_PATTERN_EDITOR);
            setCreativeTab(FluidCraftingTabs.INSTANCE);
            return this;
        }
        return null;
    }
}
