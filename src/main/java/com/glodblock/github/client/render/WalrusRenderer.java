package com.glodblock.github.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.glodblock.github.FluidCraft;
import com.gtnewhorizon.gtnhlib.client.model.wavefront.WavefrontVBOBuilder;
import com.gtnewhorizon.gtnhlib.client.renderer.vao.IVertexArrayObject;

public final class WalrusRenderer {

    private static IVertexArrayObject walrus;
    private static final ResourceLocation textureWalrus = FluidCraft.resource("textures/blocks/walrus.png");

    public static void load() {
        walrus = WavefrontVBOBuilder.compileToVBO(FluidCraft.resource("models/walrus.obj"));
    }

    public static void render() {
        Minecraft.getMinecraft().renderEngine.bindTexture(textureWalrus);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        walrus.render();
    }
}
