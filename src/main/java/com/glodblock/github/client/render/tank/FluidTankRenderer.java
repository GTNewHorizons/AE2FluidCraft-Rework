package com.glodblock.github.client.render.tank;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.util.RenderUtil;

public class FluidTankRenderer {

    public static final int DISPLAY_STAGES = 100;
    private static final Map<Fluid, int[]> flowingRenderCache = new HashMap<>();
    private static final Map<Fluid, int[]> stillRenderCache = new HashMap<>();
    private static final RenderEntityBlock.RenderInfo liquidBlock = new RenderEntityBlock.RenderInfo();

    public static void onTextureReload() {
        for (int[] ia : flowingRenderCache.values()) {
            for (int i : ia) {
                GL11.glDeleteLists(i, 1);
            }
        }
        flowingRenderCache.clear();

        for (int[] ia : stillRenderCache.values()) {
            for (int i : ia) {
                GL11.glDeleteLists(i, 1);
            }
        }
        stillRenderCache.clear();
    }

    public static IIcon getFluidTexture(FluidStack fluidStack, boolean flowing) {
        if (fluidStack == null) {
            return null;
        }
        return getFluidTexture(fluidStack.getFluid(), flowing);
    }

    public static IIcon getFluidTexture(Fluid fluid, boolean flowing) {
        if (fluid == null) {
            return null;
        }
        IIcon icon = flowing ? fluid.getFlowingIcon() : fluid.getStillIcon();
        if (icon == null) {
            icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager()
                    .getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
        }
        return icon;
    }

    public static void setColorForFluidStack(FluidStack fluidstack) {
        if (fluidstack == null) {
            return;
        }
        int color = fluidstack.getFluid().getColor(fluidstack);
        RenderUtil.setGLColorFromInt(color);
    }

    public static int[] getFluidDisplayLists(FluidStack fluidStack, World world, boolean flowing) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return null;
        }
        Map<Fluid, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
        int[] diplayLists = cache.get(fluid);
        if (diplayLists != null) {
            return diplayLists;
        }

        diplayLists = new int[DISPLAY_STAGES];

        if (fluid.getBlock() != null) {
            liquidBlock.baseBlock = fluid.getBlock();
        } else {
            liquidBlock.baseBlock = Blocks.water;
        }
        liquidBlock.texture = getFluidTexture(fluidStack, flowing);

        cache.put(fluid, diplayLists);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        for (int s = 0; s < DISPLAY_STAGES; ++s) {
            diplayLists[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(diplayLists[s], GL11.GL_COMPILE);

            liquidBlock.minX = 0.01f;
            liquidBlock.minY = 0;
            liquidBlock.minZ = 0.01f;

            liquidBlock.maxX = 0.99f;
            liquidBlock.maxY = Math.max(s, 1) / (float) DISPLAY_STAGES;
            liquidBlock.maxZ = 0.99f;

            RenderEntityBlock.INSTANCE.renderBlock(liquidBlock);
            GL11.glEndList();
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);

        return diplayLists;
    }

}
