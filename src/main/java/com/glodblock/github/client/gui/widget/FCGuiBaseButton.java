package com.glodblock.github.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class FCGuiBaseButton extends GuiButton {

    // copy from wct button
    protected static final ResourceLocation buttonTextures = new ResourceLocation(
            "minecraft",
            "textures/gui/widgets.png");

    public FCGuiBaseButton(int id, int xPosition, int yPosition, int width, int height, String displayString) {
        super(id, xPosition, yPosition, width, height, displayString);
        this.enabled = true;
        this.visible = true;
        this.id = id;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.displayString = displayString;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(
                    this.xPosition + this.width / 2,
                    this.yPosition,
                    200 - this.width / 2,
                    46 + k * 20,
                    this.width / 2,
                    this.height - 2);
            this.drawTexturedModalRect(
                    this.xPosition + 2,
                    this.yPosition + (this.height - 2),
                    200 - (this.width - 2),
                    64 + k * 20,
                    this.width - 2,
                    2);
            this.drawTexturedModalRect(
                    this.xPosition,
                    this.yPosition + (this.height - 1),
                    200 - this.width,
                    65 + k * 20,
                    this.width,
                    1);
            this.mouseDragged(mc, mouseX, mouseY);
            int l = 14737632;

            if (packedFGColour != 0) {
                l = packedFGColour;
            } else if (!this.enabled) {
                l = 10526880;
            } else if (this.field_146123_n) {
                l = 16777120;
            }

            this.drawCenteredString(
                    fontrenderer,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    l);
        }
    }
}
