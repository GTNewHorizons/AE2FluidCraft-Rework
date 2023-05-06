package com.glodblock.github.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.common.block.BlockFluidBuffer;
import com.glodblock.github.common.tile.TileFluidBuffer;

import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;

public class RenderBlockFluidBuffer extends BaseBlockRender<BlockFluidBuffer, TileFluidBuffer> {

    public RenderBlockFluidBuffer() {
        super(false, 20);
    }

    @Override
    public boolean renderInWorld(final BlockFluidBuffer block, final IBlockAccess world, final int x, final int y,
            final int z, final RenderBlocks renderer) {
        final TileFluidBuffer ti = block.getTileEntity(world, x, y, z);
        final BlockRenderInfo info = block.getRendererInstance();
        this.renderFluid(ti, x, y, z, renderer);
        final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);
        info.setTemporaryRenderIcon(null);
        return fz;
    }

    private void renderFluid(TileFluidBuffer tileEntity, double x, double y, double z, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        if (tileEntity != null && tileEntity.getFluidStack() != null) {
            Fluid storedFluid = tileEntity.getFluidStack().getFluid();
            if (storedFluid != null) {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                Block id = Block.getBlockById(FluidRegistry.WATER.getID());
                IIcon fluidIcon = storedFluid.getIcon();
                if (fluidIcon == null) fluidIcon = FluidRegistry.LAVA.getIcon();
                tessellator.setColorRGBA_F(
                        (storedFluid.getColor() >> 16 & 0xFF) / 255.0F,
                        (storedFluid.getColor() >> 8 & 0xFF) / 255.0F,
                        (storedFluid.getColor() & 0xFF) / 255.0F,
                        1.0F);
                tessellator.setNormal(0.0F, -1F, 0.0F);
                renderer.renderFaceYNeg(id, x, y, z, fluidIcon);
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                renderer.renderFaceYPos(id, x, y, z, fluidIcon);
                tessellator.setNormal(0.0F, 0.0F, -1F);
                renderer.renderFaceZNeg(id, x, y, z, fluidIcon);
                tessellator.setNormal(0.0F, 0.0F, 1.0F);
                renderer.renderFaceZPos(id, x, y, z, fluidIcon);
                tessellator.setNormal(-1F, 0.0F, 0.0F);
                renderer.renderFaceXNeg(id, x, y, z, fluidIcon);
                tessellator.setNormal(1.0F, 0.0F, 0.0F);
                renderer.renderFaceXPos(id, x, y, z, fluidIcon);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }
}
