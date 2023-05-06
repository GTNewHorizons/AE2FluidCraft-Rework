package com.glodblock.github.common.block;

import static net.minecraft.client.gui.GuiScreen.isCtrlKeyDown;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.render.RenderBlockLevelMaintainer;
import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileLevelMaintainer;
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

public class BlockLevelMaintainer extends FCBaseBlock {

    public BlockLevelMaintainer() {
        super(Material.iron, NameConst.BLOCK_LEVEL_MAINTAINER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileLevelMaintainer.class);
    }

    @Override
    public BlockLevelMaintainer register() {
        GameRegistry.registerBlock(this, FCBaseItemBlock.class, NameConst.BLOCK_LEVEL_MAINTAINER);
        GameRegistry.registerTileEntity(TileLevelMaintainer.class, NameConst.BLOCK_LEVEL_MAINTAINER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockLevelMaintainer getRenderer() {
        return new RenderBlockLevelMaintainer();
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileLevelMaintainer tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (Platform.isServer()) {
                if (Util.hasPermission(player, SecurityPermissions.CRAFT, tile)) {
                    InventoryHandler.openGui(
                            player,
                            world,
                            new BlockPos(x, y, z),
                            ForgeDirection.getOrientation(facing),
                            GuiType.LEVEL_MAINTAINER);
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
        if (isShiftKeyDown() && isCtrlKeyDown()) {
            toolTip.add(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_WHO_AM_I));
        } else if (isShiftKeyDown()) {
            toolTip.addAll(RenderUtil.listFormattedStringToWidth(NameConst.i18n(NameConst.TT_LEVEL_MAINTAINER_DESC)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
