package com.glodblock.github.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.client.render.RenderBlockFluidBuffer;
import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidBuffer;
import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidBuffer extends FCBaseBlock {

    public BlockFluidBuffer() {
        super(Material.iron, NameConst.BLOCK_FLUID_BUFFER);
        setTileEntity(TileFluidBuffer.class);
        setFullBlock(false);
        setOpaque(false);
        this.lightOpacity = 3;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
            float hitY, float hitZ) {
        ItemStack itemStack = player.inventory.getCurrentItem();
        FluidStack fs = Util.getFluidFromItem(itemStack);
        if (Platform.isServer()) {
            TileFluidBuffer tile = getTileEntity(world, x, y, z);
            if (tile == null) return false;
            if (player.isSneaking() && itemStack == null) return !tile.setFluid(null);
            IAEFluidStack ias = tile.getAEStoreFluidStack();
            if (fs == null && ias != null) {
                player.addChatMessage(
                        new ChatComponentText(
                                Tooltip.fluidFormat(ias.getFluidStack().getLocalizedName(), ias.getStackSize())));
                return false;
            } else {
                tile.setFluid(fs);
                return true;
            }
        }
        return fs != null || (player.isSneaking() && itemStack == null);
    }

    @Override
    public BlockFluidBuffer register() {
        GameRegistry.registerBlock(this, FCBaseItemBlock.class, NameConst.BLOCK_FLUID_BUFFER);
        GameRegistry.registerTileEntity(TileFluidBuffer.class, NameConst.BLOCK_FLUID_BUFFER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockFluidBuffer getRenderer() {
        return new RenderBlockFluidBuffer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
            final boolean advancedToolTips) {
        if (isShiftKeyDown()) {
            toolTip.add(NameConst.i18n(NameConst.TT_FLUID_BUFFER_DESC));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
