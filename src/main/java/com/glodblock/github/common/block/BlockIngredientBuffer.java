package com.glodblock.github.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockIngredientBuffer extends FCBaseBlock {

    public BlockIngredientBuffer() {
        super(Material.iron, NameConst.BLOCK_INGREDIENT_BUFFER);
        setTileEntity(TileIngredientBuffer.class);
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
        TileIngredientBuffer tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.INGREDIENT_BUFFER);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockIngredientBuffer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_INGREDIENT_BUFFER);
        GameRegistry.registerTileEntity(TileIngredientBuffer.class, NameConst.BLOCK_INGREDIENT_BUFFER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
