package com.glodblock.github.client.gui.container;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.util.Platform;

public class ContainerEssentiaMonitor extends ContainerFluidPortableCell {

    public ContainerEssentiaMonitor(InventoryPlayer ip, IWirelessTerminal monitorable) {
        super(ip, monitorable);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            Slot clickSlot = (Slot) this.inventorySlots.get(idx);
            if ((clickSlot instanceof SlotPlayerInv || clickSlot instanceof SlotPlayerHotBar) && clickSlot.getHasStack()
                    && AspectUtil.isEssentiaContainer(clickSlot.getStack())) {
                int index = Util.findItemInPlayerInvSlot(p, clickSlot.getStack());
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(index));
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    public void postChange(IAEFluidStack clientSelectedFluid, EntityPlayer player, int slotIndex, boolean shift) {
        ItemStack targetStack;
        if (slotIndex == -1) {
            targetStack = player.inventory.getItemStack();
        } else {
            targetStack = player.inventory.getStackInSlot(slotIndex);
        }
        final int containersRequested = shift ? targetStack.stackSize : 1;

        IAEFluidStack storedFluid = this.monitor.getStorageList().findPrecise(clientSelectedFluid);
        ItemStack out = targetStack.copy();
        out.stackSize = 1;
        if (AspectUtil.isEmptyEssentiaContainer(targetStack) && AspectUtil.isEssentiaGas(storedFluid)) {
            // add essentia to jars
            if (storedFluid.getStackSize() / AspectUtil.R <= 0) {
                return;
            }
            final IAEFluidStack toExtract = storedFluid.copy();
            MutablePair<Integer, ItemStack> fillStack = AspectUtil.fillEssentiaFromGas(out, toExtract.getFluidStack());
            if (fillStack == null || fillStack.right == null || fillStack.left <= 0) {
                return;
            }
            toExtract.setStackSize((long) fillStack.left * containersRequested);
            IAEFluidStack tmp = this.host.getFluidInventory()
                    .extractItems(toExtract, Actionable.SIMULATE, this.getActionSource());
            if (tmp == null) {
                return;
            }
            fillStack.right.stackSize = (int) (tmp.getStackSize() / fillStack.left);
            this.dropItem(fillStack.right);
            out.stackSize = fillStack.right.stackSize;
            if (AspectUtil.isEssentiaContainer(fillStack.right)) {
                this.host.getFluidInventory().extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                if ((int) (tmp.getStackSize() % fillStack.left) / AspectUtil.R > 0) {
                    this.dropItem(
                            AspectUtil.setAspectAmount(
                                    fillStack.right,
                                    (int) (tmp.getStackSize() % fillStack.left) / AspectUtil.R,
                                    AspectUtil.getAspectFromGas(toExtract)),
                            1);
                    out.stackSize++;
                }
            }
        } else if (!AspectUtil.isEmptyEssentiaContainer(targetStack)) {
            // add essentia to ae network
            ItemStack containerStack = targetStack.copy();
            containerStack.stackSize = containersRequested;
            IAEFluidStack fluidStack = AspectUtil.getAEGasFromContainer(containerStack);
            if (fluidStack == null) {
                return;
            }
            final IAEFluidStack aeFluidStack = fluidStack.copy();
            // simulate result is incorrect. so I'm using other solution and ec2 both mod have same issues
            final IAEFluidStack notInserted = this.host.getFluidInventory()
                    .injectItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
            MutablePair<Integer, ItemStack> drainStack = AspectUtil
                    .drainEssentiaFromGas(out.copy(), aeFluidStack.getFluidStack());
            if (notInserted != null && notInserted.getStackSize() > 0 && drainStack != null) {
                if (fluidStack.getStackSize() == notInserted.getStackSize()) {
                    return;
                }
                aeFluidStack.decStackSize(notInserted.getStackSize());

                if (drainStack.left > aeFluidStack.getStackSize() && AspectUtil.isEssentiaContainer(drainStack.right)) {
                    aeFluidStack.setStackSize(drainStack.left - aeFluidStack.getStackSize());
                    this.host.getFluidInventory()
                            .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                    return;
                }

                // drop empty item
                this.dropItem(drainStack.right, (int) (aeFluidStack.getStackSize() / drainStack.left));
                out.stackSize = (int) (notInserted.getStackSize() / drainStack.left);
                if (AspectUtil.isEssentiaContainer(drainStack.right)) {
                    if (notInserted.getStackSize() % drainStack.left > 0) {
                        fluidStack.setStackSize((notInserted.getStackSize() % drainStack.left));
                        AspectUtil.fillEssentiaFromGas(drainStack.right, fluidStack.getFluidStack());
                        this.dropItem(drainStack.right, 1);
                    }
                }
                if (slotIndex == -1) out.stackSize = targetStack.stackSize - out.stackSize;
            } else if (drainStack != null) {
                if (slotIndex == -1) {
                    out.stackSize = targetStack.stackSize;
                } else {
                    out.stackSize = (int) (targetStack.stackSize - (aeFluidStack.getStackSize() / drainStack.left));
                }
                this.dropItem(drainStack.right, targetStack.stackSize); // drop empty item
            }
        } else {
            return;
        }
        if (slotIndex == -1) {
            player.inventory.getItemStack().stackSize = player.inventory.getItemStack().stackSize - out.stackSize;
            if (player.inventory.getItemStack().stackSize > 0) {
                FluidCraft.proxy.netHandler.sendTo(
                        new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                        (EntityPlayerMP) player);
            } else {
                player.inventory.setItemStack(null);
                FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
            }
        } else {
            player.inventory.setInventorySlotContents(slotIndex, out.stackSize > 0 ? out : null);
        }
        this.detectAndSendChanges();
    }

    @Override
    protected boolean isEssentiaMode() {
        return true;
    }

}
