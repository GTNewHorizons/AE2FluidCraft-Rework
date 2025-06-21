package com.glodblock.github.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockSuperStockReplenisher extends FCBaseBlock {

    public BlockSuperStockReplenisher() {
        super(Material.web, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        setTileEntity(TileSuperStockReplenisher.class);
        setOpaque(true);
        setFullBlock(true);
        this.lightOpacity = 4;
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
    public void breakBlock(World w, int x, int y, int z, Block a, int b) {
        final TileSuperStockReplenisher tss = this.getTileEntity(w, x, y, z);
        if (tss != null) {
            tss.fullRefund();
        }
        super.breakBlock(w, x, y, z, a, b);
    }

    @Override
    public BlockSuperStockReplenisher register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        GameRegistry.registerTileEntity(TileSuperStockReplenisher.class, NameConst.BLOCK_SUPER_STOCK_REPLENISHER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
