package com.glodblock.github.coremod.hooker;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.CraftingGridCacheFluidInventoryProxyCell;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2Reflect;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.IGuiTooltipHandler;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.InventoryAdaptor;

public class CoreModHooks {

    public static InventoryCrafting wrapCraftingBuffer(InventoryCrafting inv) {
        return new FluidConvertingInventoryCrafting(
                inv.eventHandler,
                inv.inventoryWidth,
                inv.getSizeInventory() / inv.inventoryWidth);
    }

    public static IAEItemStack wrapFluidPacketStack(IAEItemStack stack) {
        if (stack.getItem() == ItemAndBlockHolder.PACKET) {
            IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack.getItemStack()));
            if (dropStack != null) {
                return dropStack;
            }
        }
        return stack;
    }

    public static ItemStack removeFluidPackets(InventoryCrafting inv, int index) {
        ItemStack stack = inv.getStackInSlot(index);
        if (stack != null && stack.getItem() instanceof ItemFluidPacket) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
            return ItemFluidDrop.newStack(fluid);
        } else {
            return stack;
        }
    }

    @Nullable
    public static InventoryAdaptor wrapInventory(@Nullable TileEntity tile, ForgeDirection face) {
        return tile != null ? FluidConvertingInventoryAdaptor.wrap(tile, face) : null;
    }

    public static long getCraftingByteCost(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop ? (long) Math.ceil(stack.getStackSize() / 1000D)
                : stack.getStackSize();
    }

    public static long getFluidDropsByteCost(long totalBytes, long originByte, IAEItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            return (long) Math.ceil(originByte / 1000D) + totalBytes;
        }
        return originByte + totalBytes;
    }

    public static ItemStack displayFluid(IAEItemStack aeStack) {
        if (aeStack.getItemStack() != null && aeStack.getItemStack().getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(aeStack.getItemStack());
            return ItemFluidPacket.newDisplayStack(fluid);
        } else return aeStack.getItemStack();
    }

    public static long getFluidSize(IAEItemStack aeStack) {
        if (aeStack.getItemStack() != null && aeStack.getItemStack().getItem() instanceof ItemFluidDrop) {
            return (long) Math.max(aeStack.getStackSize() / 1000D, 1);
        } else return aeStack.getStackSize();
    }

    public static List<IMEInventoryHandler> craftingGridCacheGetCellArray(final CraftingGridCache instance,
            final StorageChannel channel) {
        // Equivalent to original function, but using the proxy for fluid channel
        return channel == StorageChannel.ITEMS ? Arrays.asList(instance)
                : Arrays.asList(new CraftingGridCacheFluidInventoryProxyCell(instance));
    }

    public static void storeFluidItem(CraftingCPUCluster instance) {
        final IGrid g = Ae2Reflect.getGrid(instance);

        if (g == null) {
            return;
        }

        final IStorageGrid sg = g.getCache(IStorageGrid.class);
        final IMEInventory<IAEItemStack> ii = sg.getItemInventory();
        final IMEInventory<IAEFluidStack> jj = sg.getFluidInventory();
        final MECraftingInventory inventory = Ae2Reflect.getCPUInventory(instance);

        for (IAEItemStack is : inventory.getItemList()) {
            is = inventory.extractItems(is.copy(), Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));

            if (is != null) {
                Ae2Reflect.postCPUChange(instance, is, Ae2Reflect.getCPUSource(instance));
                if (is.getItem() instanceof ItemFluidDrop) {
                    IAEFluidStack fluidDrop = ItemFluidDrop.getAeFluidStack(is);
                    fluidDrop = jj.injectItems(fluidDrop, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                    if (fluidDrop == null) {
                        is = null;
                    } else {
                        is.setStackSize(fluidDrop.getStackSize());
                    }
                } else {
                    is = ii.injectItems(is, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
                }
            }

            if (is != null) {
                inventory.injectItems(is, Actionable.MODULATE, Ae2Reflect.getCPUSource(instance));
            }
        }

        if (inventory.getItemList().isEmpty()) {
            Ae2Reflect.setCPUInventory(instance, new MECraftingInventory());
        }

        Ae2Reflect.markCPUDirty(instance);
    }

    public static ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (gui instanceof IGuiTooltipHandler guiTooltipHandler) {
            return guiTooltipHandler.getHoveredStack();
        }
        return null;
    }

    public static boolean shouldShowTooltip(GuiContainer gui) {
        if (gui instanceof GuiFluidCraftConfirm guiCraftConfirm) {
            return guiCraftConfirm.getHoveredStack() == null;
        }
        return true;
    }
}
