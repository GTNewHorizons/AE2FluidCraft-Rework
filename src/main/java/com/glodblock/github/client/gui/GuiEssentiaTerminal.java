package com.glodblock.github.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerEssentiaMonitor;
import com.glodblock.github.client.me.EssentiaRepo;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectRender;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.util.Ae2ReflectClient;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.client.render.AppEngRenderItem;

public class GuiEssentiaTerminal extends GuiFluidMonitor {

    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    protected EntityPlayer player;

    public GuiEssentiaTerminal(InventoryPlayer inventoryPlayer, IWirelessTerminal te) {
        super(inventoryPlayer, te, new ContainerEssentiaMonitor(inventoryPlayer, te));
        this.repo = new EssentiaRepo(getScrollBar(), this);
        this.player = inventoryPlayer.player;
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        if (AspectUtil.getAspectFromJar(stack) != null) {
            setSearchString(Objects.requireNonNull(AspectUtil.getAspectFromJar(stack)).getName(), true);
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
            AspectRender.drawAspect(
                    this.player,
                    slot.xDisplayPosition,
                    slot.yDisplayPosition,
                    this.zLevel,
                    AspectUtil.getAspectFromGas(fluidStack),
                    fluidStack.amount);
            IAEItemStack gas = stack.copy().setStackSize(stack.getStackSize() / AspectUtil.R);
            stackSizeRenderer.setAeStack(gas);
            stackSizeRenderer.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    gas.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
            return false;
        }
        return true;
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (slot instanceof SlotME && AspectUtil.isEssentiaContainer(player.inventory.getItemStack())) {
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

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        } else {
            super.actionPerformed(btn);
        }
    }

    public void update(ItemStack itemStack) {
        this.player.inventory.setItemStack(itemStack);
    }

}
