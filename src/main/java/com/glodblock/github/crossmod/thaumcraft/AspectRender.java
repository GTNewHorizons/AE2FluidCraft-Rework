package com.glodblock.github.crossmod.thaumcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.util.RenderUtil;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;

public class AspectRender {

    public static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation(
            "thaumcraft",
            "textures/aspects/_unknown.png");

    public static void drawAspect(EntityPlayer player, int posX, int posY, float zLevel, Aspect aspect, long amount) {
        if (aspect == null || amount <= 0) {
            return;
        }
        if (AspectUtil.isPlayerDiscoveredAspect(player, aspect)) {
            UtilsFX.drawTag(posX, posY, aspect, 0, 0, zLevel);
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(UNKNOWN_TEXTURE);
            RenderUtil.setGLColorFromInt(aspect.getColor());
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            UtilsFX.drawTexturedQuadFull(posX, posY, zLevel);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

}
