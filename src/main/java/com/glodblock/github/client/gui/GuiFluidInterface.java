package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidInterface;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.gui.TankMouseHandler;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

public class GuiFluidInterface extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/interface_fluid.png");
    private static final int TANK_X = 35, TANK_X_OFF = 18, TANK_Y = 53;
    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 68;
    private final ContainerFluidInterface cont;

    private GuiTabButton switcher;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiFluidInterface(InventoryPlayer ipl, IDualHost tile) {
        super(new ContainerFluidInterface(ipl, tile));
        this.cont = (ContainerFluidInterface) inventorySlots;
        this.ySize = 231;
        this.addMouseRegin();
    }

    private void addMouseRegin() {
        for (int i = 0; i < 6; i++) {
            mouseRegions.addRegion(
                    TANK_X + TANK_X_OFF * i,
                    TANK_Y,
                    TANK_WIDTH,
                    TANK_HEIGHT,
                    new TankMouseHandler(cont.getTile().getInternalFluid(), i));
        }
    }

    @Override
    public void func_146977_a(final Slot s) {
        try {
            GuiContainer.class.getDeclaredMethod("func_146977_a_original", Slot.class).invoke(this, s);
        } catch (final Exception ignore) {}
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.switcher = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                isPart() ? ItemAndBlockHolder.FLUID_INTERFACE.stack() : ItemAndBlockHolder.INTERFACE.stack(),
                StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"),
                itemRender);
        this.buttonList.add(this.switcher);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_INTERFACE)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        IAEFluidTank fluidInv = cont.getTile().getInternalFluid();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        for (int i = 0; i < 6; i++) {
            if (!isPart()) {
                fontRendererObj.drawString(dirName(i), TANK_X + i * TANK_X_OFF + 5, 22, 0x404040);
            }
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

    public String dirName(int face) {
        return I18n.format(NameConst.GUI_FLUID_INTERFACE + "." + face);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        if (btn == this.switcher) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.DUAL_INTERFACE));
        }
    }

    public void update(int id, IAEFluidStack stack) {
        if (id >= 100) {
            cont.getTile().setConfig(id - 100, stack);
        } else {
            cont.getTile().setFluidInv(id, stack);
        }
    }

    private boolean isPart() {
        return this.cont.getTile() instanceof PartFluidInterface;
    }
}
