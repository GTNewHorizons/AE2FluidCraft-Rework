package com.glodblock.github.client.gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerSuperStockReplenisher;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.slots.VirtualMEPatternSlot;
import appeng.client.gui.slots.VirtualMEPhantomSlot;
import appeng.client.gui.slots.VirtualMESlot;
import appeng.core.localization.GuiText;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;

public class GuiSuperStockReplenisher extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/superStockReplenisher.png");
    private final DecimalFormat df = new DecimalFormat("#.#");
    private Map<Integer, IAEStack<?>> list = new HashMap<>();
    private final ContainerSuperStockReplenisher containerSuperStockReplenisher;

    private final VirtualMEPatternSlot[] configFluidsSlots = new VirtualMEPatternSlot[9];
    private final VirtualMEPatternSlot[] configItemsSlots = new VirtualMEPatternSlot[63];

    private final IAEStackInventory configFluids;
    private final IAEStackInventory configItems;

    public GuiSuperStockReplenisher(InventoryPlayer ipl, TileSuperStockReplenisher tile) {
        super(new ContainerSuperStockReplenisher(ipl, tile));

        this.containerSuperStockReplenisher = (ContainerSuperStockReplenisher) inventorySlots;
        this.configFluids = this.containerSuperStockReplenisher.getTile().getConfigFluids();
        this.configItems = this.containerSuperStockReplenisher.getTile().getConfigItems();

        this.ySize = 251;
        this.xSize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.initSlots();
    }

    private void initSlots() {

        final int xo = 8;
        final int yo = 8;

        for (int i = 0; i < 9; i++) {
            VirtualMEPatternSlot slot = new VirtualMEPatternSlot(
                    xo + i * 18,
                    yo,
                    this.configFluids,
                    i,
                    this::acceptType);
            this.configFluidsSlots[i] = slot;
            this.registerVirtualSlots(slot);
        }

        final int iyo = 29;
        for (int y = 0; y < 7; y++) {
            for (int ix = 0; ix < 9; ix++) {
                VirtualMEPatternSlot slot = new VirtualMEPatternSlot(
                        xo + ix * 18,
                        iyo + y * 18,
                        this.configItems,
                        y * 9 + ix,
                        this::acceptType);
                this.configItemsSlots[y * 9 + ix] = slot;
                this.registerVirtualSlots(slot);
            }
        }
    }

    private void drawFillStatus(VirtualMEPatternSlot virtualSlot) {
        final int offsetX = virtualSlot.getX() - 8;
        final int offsetY = virtualSlot.getY() - 12;
        final IAEStack<?> aes = virtualSlot.getAEStack();
        final long stackSize = aes != null ? aes.getStackSize() : 0;
        final int slotIndex = virtualSlot.getStorageName() == StorageName.NONE ? virtualSlot.getSlotIndex()
                : virtualSlot.getSlotIndex() + 100;

        if (stackSize != 0) {
            String s = "0%";
            IAEStack<?> aeStack = list.get(slotIndex);

            if (aeStack != null) s = df.format((float) aeStack.getStackSize() / stackSize * 100) + "%";

            float scale = 0.5f;
            float shiftX = 2;
            float shiftY = 1;
            final float inverseScaleFactor = 1.0f / scale;

            final int X = (int) (((float) offsetX - shiftX + 10.0f) * inverseScaleFactor) + 1;
            final int Y = (int) (((float) offsetY - shiftY + 16.0f - 7.0f * scale) * inverseScaleFactor) + 2;
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            GL11.glScaled(scale, scale, scale);
            this.fontRendererObj.drawStringWithShadow(s, X, Y, 16777215);
            GL11.glScaled(inverseScaleFactor, inverseScaleFactor, inverseScaleFactor);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
        }
    }

    private void drawVirtualSlots() {
        for (VirtualMEPatternSlot slot : this.configFluidsSlots) this.drawFillStatus(slot);
        for (VirtualMEPatternSlot slot : this.configItemsSlots) this.drawFillStatus(slot);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        this.drawVirtualSlots();
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture(TEX_BG);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
    }

    public void update(Map<Integer, IAEStack<?>> map) {
        this.list = map;
    }

    private boolean acceptType(VirtualMEPhantomSlot slot, IAEStackType<?> type, int mouseButton) {
        if (slot.getStorageName() == StorageName.NONE) return type == AEFluidStackType.FLUID_STACK_TYPE;
        else return type == AEItemStackType.ITEM_STACK_TYPE;
    }

    @Override
    protected boolean handleVirtualSlotClick(VirtualMESlot slot, int mouseButton) {
        this.containerSuperStockReplenisher.markDirty();
        return super.handleVirtualSlotClick(slot, mouseButton);
    }
}
