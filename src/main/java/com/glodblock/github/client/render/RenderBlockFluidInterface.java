package com.glodblock.github.client.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.block.BlockFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;

import appeng.api.util.AEColor;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.tile.misc.TileInterface;

public class RenderBlockFluidInterface extends BaseBlockRender<BlockFluidInterface, TileFluidInterface> {

    public RenderBlockFluidInterface() {
        super(false, 20);
    }

    @Override
    public boolean renderInWorld(final BlockFluidInterface block, final IBlockAccess world, final int x, final int y,
            final int z, final RenderBlocks renderer) {
        final TileInterface ti = block.getTileEntity(world, x, y, z);
        final BlockRenderInfo info = block.getRendererInstance();

        if (ti != null) {
            final AEColor color = ti.getProxy().getColor();
            if (ti.getForward() != ForgeDirection.UNKNOWN) {
                final IIcon face = color == AEColor.Transparent ? block.getIcon(0, 0)
                        : block.getColoredTexture(0, color);
                final IIcon alternate = color == AEColor.Transparent ? FCPartsTexture.BlockInterfaceAlternate.getIcon()
                        : block.getColoredTexture(1, color);
                final IIcon side = color == AEColor.Transparent
                        ? FCPartsTexture.BlockFluidInterfaceAlternate_Arrow.getIcon()
                        : block.getColoredTexture(2, color);
                info.setTemporaryRenderIcons(alternate, face, side, side, side, side);
            }
        }

        final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);

        info.setTemporaryRenderIcon(null);

        return fz;
    }
}
