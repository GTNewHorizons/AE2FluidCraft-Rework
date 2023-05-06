package com.glodblock.github.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockLargeIngredientBuffer extends FCBaseBlock {

    public BlockLargeIngredientBuffer() {
        super(Material.iron, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
        setTileEntity(TileLargeIngredientBuffer.class);
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
        TileLargeIngredientBuffer tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.LARGE_INGREDIENT_BUFFER);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockLargeIngredientBuffer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
        GameRegistry.registerTileEntity(TileLargeIngredientBuffer.class, NameConst.BLOCK_LARGE_INGREDIENT_BUFFER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
