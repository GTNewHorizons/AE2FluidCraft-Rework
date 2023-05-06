package com.glodblock.github.client.gui;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerFluidMonitor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.Util;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.client.render.AppEngRenderItem;
import appeng.container.slot.AppEngSlot;

public class GuiFluidTerminal extends GuiFluidMonitor {

    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    protected EntityPlayer player;
    public ContainerFluidMonitor container;

    public GuiFluidTerminal(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te, new ContainerFluidMonitor(inventoryPlayer, te));
        this.container = new ContainerFluidMonitor(inventoryPlayer, te);
        this.player = inventoryPlayer.player;
        this.showViewBtn = false;
    }

    public GuiFluidTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te,
            final ContainerFluidMonitor c) {
        super(inventoryPlayer, te, c);
        this.container = c;
        this.player = inventoryPlayer.player;
        this.showViewBtn = false;
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        if (s.isPlayerSide()) {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
        } else {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 3;
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot0(s)) super.func_146977_a(s);
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotME) {
            IAEItemStack stack = ((SlotME) slot).getAEStack();
            if (stack == null) return true;
            FluidStack fluidStack = ItemFluidDrop.getFluidStack(slot.getStack());
            this.drawWidget(slot.xDisplayPosition, slot.yDisplayPosition, fluidStack.getFluid());
            stackSizeRenderer.setAeStack(stack);
            stackSizeRenderer.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    stack.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
            return false;
        }
        return true;
    }

    private void drawWidget(int posX, int posY, Fluid fluid) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(1, 1, 1);
        if (fluid != null && fluid.getIcon() != null) {
            GL11.glColor3f(
                    (fluid.getColor() >> 16 & 0xFF) / 255.0F,
                    (fluid.getColor() >> 8 & 0xFF) / 255.0F,
                    (fluid.getColor() & 0xFF) / 255.0F);
            drawTexturedModelRectFromIcon(posX, posY, fluid.getIcon(), 16, 16);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (slot instanceof SlotME && Util.FluidUtil.isFluidContainer(player.inventory.getItemStack())) {
            Map<Integer, IAEFluidStack> tmp = new HashMap<>();
            ItemStack itemStack = this.player.inventory.getItemStack().copy();
            tmp.put(0, ItemFluidDrop.getAeFluidStack(((SlotME) slot).getAEStack()));
            if (!isShiftKeyDown()) {
                itemStack.stackSize = 1;
            }
            FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(tmp, itemStack));
        }
        if (mouseButton == 3 && slot instanceof SlotME) {
            return;
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    public void update(ItemStack itemStack) {
        this.player.inventory.setItemStack(itemStack);
    }
}
