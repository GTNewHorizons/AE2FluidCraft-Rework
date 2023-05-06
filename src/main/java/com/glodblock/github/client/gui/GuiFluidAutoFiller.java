package com.glodblock.github.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidAutoFiller;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.util.NameConst;

import appeng.client.gui.AEBaseMEGui;
import appeng.core.localization.GuiText;

public class GuiFluidAutoFiller extends AEBaseMEGui {

    protected EntityPlayer player;
    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_auto_filler.png");

    public GuiFluidAutoFiller(InventoryPlayer ipl, TileFluidAutoFiller tile) {
        super(new ContainerFluidAutoFiller(ipl, tile));
        this.player = ipl.player;
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_AUTO_FILLER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }
}
