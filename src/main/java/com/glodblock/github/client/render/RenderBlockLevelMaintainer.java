package com.glodblock.github.client.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.block.BlockLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;

import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;

public class RenderBlockLevelMaintainer extends BaseBlockRender<BlockLevelMaintainer, TileLevelMaintainer> {

    public RenderBlockLevelMaintainer() {
        super(false, 20);
    }

    @Override
    public boolean renderInWorld(final BlockLevelMaintainer block, final IBlockAccess world, final int x, final int y,
            final int z, final RenderBlocks renderer) {
        final TileLevelMaintainer ti = block.getTileEntity(world, x, y, z);
        final BlockRenderInfo info = block.getRendererInstance();

        if (ti != null && ti.isActive()) {
            final IIcon activeIcon = FCPartsTexture.BlockLevelMaintainer_Active.getIcon();
            final IIcon side = FCPartsTexture.BlockLevelMaintainer.getIcon();
            info.setTemporaryRenderIcons(side, side, activeIcon, side, side, side);
        }

        final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);

        info.setTemporaryRenderIcon(null);

        return fz;
    }
}
