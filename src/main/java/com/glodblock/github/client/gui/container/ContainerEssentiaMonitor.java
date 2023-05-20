package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

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
import appeng.util.item.AEFluidStack;

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
                ItemStack tis = clickSlot.getStack();
                Map<Integer, IAEFluidStack> tmp = new HashMap<>();
                tmp.put(0, null);
                int index = Util.findItemInPlayerInvSlot(p, clickSlot.getStack());
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(tmp, tis, index));
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    public void postChange(Iterable<IAEFluidStack> change, ItemStack fluidContainer, EntityPlayer player,
            int slotIndex) {
        for (IAEFluidStack fluid : change) {
            IAEFluidStack nfluid = this.monitor.getStorageList().findPrecise(fluid);
            ItemStack out = fluidContainer.copy();
            out.stackSize = 1;
            if (AspectUtil.isEmptyEssentiaContainer(fluidContainer) && AspectUtil.isEssentiaGas(fluid)) {
                // add essentia to jars
                if (nfluid.getStackSize() / AspectUtil.R <= 0) continue;
                final IAEFluidStack toExtract = nfluid.copy();
                MutablePair<Integer, ItemStack> fillStack = AspectUtil
                        .fillEssentiaFromGas(out, toExtract.getFluidStack());
                if (fillStack == null || fillStack.right == null || fillStack.left <= 0) continue;
                toExtract.setStackSize((long) fillStack.left * fluidContainer.stackSize);
                IAEFluidStack tmp = this.host.getFluidInventory()
                        .extractItems(toExtract, Actionable.SIMULATE, this.getActionSource());
                if (tmp == null) continue;
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
            } else if (!AspectUtil.isEmptyEssentiaContainer(fluidContainer)) {
                // add essentia to ae network
                AEFluidStack fluidStack = AspectUtil.getAEGasFromContainer(fluidContainer);
                if (fluidStack == null) {
                    continue;
                }
                final IAEFluidStack aeFluidStack = fluidStack.copy();
                // simulate result is incorrect. so I'm using other solution and ec2 both mod have same issues
                final IAEFluidStack notInserted = this.host.getFluidInventory()
                        .injectItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                MutablePair<Integer, ItemStack> drainStack = AspectUtil
                        .drainEssentiaFromGas(out.copy(), aeFluidStack.getFluidStack());
                if (notInserted != null && notInserted.getStackSize() > 0 && drainStack != null) {
                    if (fluidStack.getStackSize() == notInserted.getStackSize()) continue;
                    aeFluidStack.decStackSize(notInserted.getStackSize());

                    if (drainStack.left > aeFluidStack.getStackSize()
                            && AspectUtil.isEssentiaContainer(drainStack.right)) {
                        aeFluidStack.setStackSize(drainStack.left - aeFluidStack.getStackSize());
                        this.host.getFluidInventory()
                                .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                        continue;
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
                    if (slotIndex == -1) out.stackSize = fluidContainer.stackSize - out.stackSize;
                } else if (drainStack != null) {
                    if (slotIndex == -1) {
                        out.stackSize = fluidContainer.stackSize;
                    } else {
                        out.stackSize = (int) (fluidContainer.stackSize
                                - (aeFluidStack.getStackSize() / drainStack.left));
                    }
                    this.dropItem(drainStack.right, fluidContainer.stackSize); // drop empty item
                }
            } else {
                continue;
            }
            if (slotIndex == -1) {
                player.inventory.getItemStack().stackSize = player.inventory.getItemStack().stackSize - out.stackSize;
                if (player.inventory.getItemStack().stackSize > 0) {
                    FluidCraft.proxy.netHandler.sendTo(
                            new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                            (EntityPlayerMP) player);
                } else {
                    player.inventory.setItemStack(null);
                    FluidCraft.proxy.netHandler
                            .sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
                }
            } else {
                player.inventory.setInventorySlotContents(slotIndex, out.stackSize > 0 ? out : null);
            }
        }
        this.detectAndSendChanges();
    }

    @Override
    protected boolean isEssentiaMode() {
        return true;
    }

}
