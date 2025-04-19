package com.glodblock.github.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileSuperStoker;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockSuperStoker extends FCBaseBlock {

    public BlockSuperStoker() {
        super(Material.web, NameConst.BLOCK_SUPER_STOKER);
        setTileEntity(TileSuperStoker.class);
        setOpaque(false);
        setFullBlock(false);
        this.lightOpacity = 4;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileSuperStoker tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.GUI_SUPER_STOKER);
            }
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World w, int x, int y, int z, Block a, int b) {
        final TileSuperStoker tss = this.getTileEntity(w, x, y, z);
        if (tss != null) {
            tss.fullRefund();
        }
        super.breakBlock(w, x, y, z, a, b);
    }

    @Override
    public BlockSuperStoker register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_SUPER_STOKER);
        GameRegistry.registerTileEntity(TileSuperStoker.class, NameConst.BLOCK_SUPER_STOKER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
