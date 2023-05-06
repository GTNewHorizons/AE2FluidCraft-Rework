package com.glodblock.github.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidAutoFiller extends FCBaseBlock {

    public BlockFluidAutoFiller() {
        super(Material.iron, NameConst.BLOCK_FLUID_AUTO_FILLER);
        setTileEntity(TileFluidAutoFiller.class);
        setFullBlock(false);
        setOpaque(false);
        this.lightOpacity = 3;
    }

    @Override
    public BlockFluidAutoFiller register() {
        GameRegistry.registerBlock(this, FCBaseItemBlock.class, NameConst.BLOCK_FLUID_AUTO_FILLER);
        GameRegistry.registerTileEntity(TileFluidAutoFiller.class, NameConst.BLOCK_FLUID_AUTO_FILLER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileFluidAutoFiller tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (Platform.isServer()) {
                if (Util.hasPermission(player, SecurityPermissions.INJECT, tile)) {
                    InventoryHandler.openGui(
                            player,
                            world,
                            new BlockPos(x, y, z),
                            ForgeDirection.getOrientation(facing),
                            GuiType.FLUID_AUTO_FILLER);
                } else {
                    player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {
        if (isShiftKeyDown()) {
            toolTip.addAll(
                    RenderUtil.listFormattedStringToWidth(
                            StatCollector.translateToLocal(NameConst.TT_FLUID_AUTO_FILLER)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
