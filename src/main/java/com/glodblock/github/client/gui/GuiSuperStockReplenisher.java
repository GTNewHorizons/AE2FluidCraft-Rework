package com.glodblock.github.client.gui;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerSuperStockReplenisher;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketVirtualSlot;
import appeng.util.item.AEItemStack;

public class GuiSuperStockReplenisher extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/superStockReplenisher.png");
    private Map<Integer, IAEStack<?>> list = new HashMap<>();

    public GuiSuperStockReplenisher(InventoryPlayer ipl, TileSuperStockReplenisher tile) {
        super(new ContainerSuperStockReplenisher(ipl, tile));
        this.ySize = 251;
        this.xSize = 216;
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        if (mouseButton == 3) {
            if (slot instanceof SlotFake) {
                if (slot.getHasStack()) {
                    IAEItemStack stack = AEItemStack.create(slot.getStack());
                    ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                    for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                        if (slot.equals(this.inventorySlots.inventorySlots.get(i))) {
                            NetworkHandler.instance
                                    .sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PATTERN_VALUE_AMOUNT));
                            NetworkHandler.instance
                                    .sendToServer(new PacketVirtualSlot(StorageName.CRAFTING_INPUT, i, stack));
                        }
                    }
                    return;
                }
            }
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    @Override
    public void func_146977_a(Slot s) {
        if (s instanceof SlotFake) {
            this.drawFillStatus(s);
        }
        super.func_146977_a(s);
    }

    private void drawFillStatus(Slot slotIn) {
        int offsetX = slotIn.xDisplayPosition - 8;
        int offsetY = slotIn.yDisplayPosition - 11;
        ItemStack itemstack = slotIn.getStack();
        int slotInt = slotIn.slotNumber;
        if (itemstack != null) {
            String s = "0%";
            if (slotInt > 0 && slotInt < 10) {
                IAEStack<?> aeStack = list.get(slotInt - 1);
                if (aeStack != null)
                    s = (int) (((float) aeStack.getStackSize() / ItemFluidPacket.getFluidAmount(itemstack)) * 100)
                            + "%";
            } else if (slotInt > 9 && slotInt < 73) {
                IAEStack<?> aeStack = list.get(slotInt + 90);
                if (aeStack != null && itemstack.stackSize > 0) {
                    long i = aeStack.getStackSize();
                    s = (int) (((float) i / itemstack.stackSize) * 100) + "%";
                }
            }

            float scale = 0.5f;
            float shiftX = 2;
            float shiftY = 1;
            final float inverseScaleFactor = 1.0f / scale;

            final int X = (int) (((float) offsetX - shiftX + 10.0f) * inverseScaleFactor);
            final int Y = (int) (((float) offsetY - shiftY + 16.0f - 7.0f * scale) * inverseScaleFactor);
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            GL11.glScaled(scale, scale, scale);
            this.fontRendererObj.drawStringWithShadow(s, X, Y, 16777215);
            GL11.glScaled(inverseScaleFactor, inverseScaleFactor, inverseScaleFactor);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture(TEX_BG);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
    }

    public void update(Map<Integer, IAEStack<?>> map) {
        this.list = map;
    }
}
