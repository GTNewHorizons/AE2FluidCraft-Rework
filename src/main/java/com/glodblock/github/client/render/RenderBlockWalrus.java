package com.glodblock.github.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tile.TileWalrus;

public class RenderBlockWalrus extends TileEntitySpecialRenderer {

    IModelCustom modelWalrus = AdvancedModelLoader.loadModel(FluidCraft.resource("models/walrus.obj"));
    ResourceLocation textureWalrus = FluidCraft.resource("textures/blocks/walrus.png");

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime) {
        Minecraft.getMinecraft().renderEngine.bindTexture(this.textureWalrus);
        if (!(tileentity instanceof TileWalrus Tile)) return;
        float scale = Tile.getWalrusScale();
        GL11.glPushMatrix();
        int orientation = Tile.getBlockMetadata();
        switch (orientation) {
            case 2:
                GL11.glTranslated(x + 0.5, y, z + 1 - scale * .655); // centering walrus model
                break;
            case 3:
                GL11.glTranslated(x + 0.5, y, z + scale * .655);
                GL11.glRotatef(180, 0, 1, 0);
                break;
            case 4:
                GL11.glTranslated(x + 1 - scale * .655, y, z + 0.5);
                GL11.glRotatef(90, 0, 1, 0);
                break;
            case 5:
                GL11.glTranslated(x + scale * .655, y, z + 0.5);
                GL11.glRotatef(-90, 0, 1, 0);
                break;
        }
        GL11.glScalef(scale, scale, scale);
        this.modelWalrus.renderAll();
        GL11.glPopMatrix();
    }
}
