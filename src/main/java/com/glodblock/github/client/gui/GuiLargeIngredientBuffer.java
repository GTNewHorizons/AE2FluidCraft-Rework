package com.glodblock.github.client.gui;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLargeIngredientBuffer;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.gui.ButtonMouseHandler;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.gui.TankMouseHandler;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;

public class GuiLargeIngredientBuffer extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/large_ingredient_buffer.png");
    private static final int TANK_X = 13, TANK_X_OFF = 22, TANK_Y = 18;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 37;

    private final ContainerLargeIngredientBuffer cont;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiLargeIngredientBuffer(InventoryPlayer ipl, TileLargeIngredientBuffer tile) {
        super(new ContainerLargeIngredientBuffer(ipl, tile));
        this.cont = (ContainerLargeIngredientBuffer) inventorySlots;
        this.ySize = 222;
        for (int i = 0; i < 7; i++) {
            mouseRegions.addRegion(
                    TANK_X + TANK_X_OFF * i,
                    TANK_Y,
                    TANK_WIDTH,
                    TANK_HEIGHT,
                    new TankMouseHandler(cont.getTile().getInternalFluid(), i));
            mouseRegions.addRegion(
                    TANK_X + 10 + 22 * i,
                    TANK_Y + TANK_HEIGHT + 2,
                    7,
                    7,
                    ButtonMouseHandler.dumpTank(cont, i));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj
                .drawString(getGuiDisplayName(I18n.format(NameConst.GUI_LARGE_INGREDIENT_BUFFER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        IAEFluidTank fluidInv = cont.getTile().getInternalFluid();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        for (int i = 0; i < 7; i++) {
            RenderUtil.renderFluidIntoGui(
                    this,
                    TANK_X + i * TANK_X_OFF,
                    TANK_Y,
                    TANK_WIDTH,
                    TANK_HEIGHT,
                    fluidInv.getFluidInSlot(i),
                    fluidInv.getTankInfo(ForgeDirection.UNKNOWN)[i].capacity);
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mouseRegions.render(mouseX, mouseY);
    }

    public void update(int slot, IAEFluidStack aeFluidStack) {
        cont.getTile().getInternalFluid().setFluidInSlot(slot, aeFluidStack);
    }
}
