package com.glodblock.github.client.gui.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.MutablePair;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.base.FCContainerMonitor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketMEUpdateBuffer;
import com.glodblock.github.util.Util;

public class ContainerFluidMonitor extends FCContainerMonitor<IAEFluidStack> {

    protected final IItemList<IAEFluidStack> fluids = AEApi.instance().storage().createFluidList();

    public ContainerFluidMonitor(final InventoryPlayer ip, final ITerminalHost monitorable) {
        this(ip, monitorable, true);
    }

    protected ContainerFluidMonitor(final InventoryPlayer ip, final ITerminalHost monitorable,
            final boolean bindInventory) {
        super(ip, monitorable, bindInventory);
        if (Platform.isServer()) {
            this.serverCM = monitorable.getConfigManager();
            if (monitorable instanceof IGridHost) {
                final IGridNode node = ((IGridHost) monitorable).getGridNode(ForgeDirection.UNKNOWN);
                if (node != null) {
                    this.networkNode = node;
                    final IGrid g = node.getGrid();
                    if (g != null) {
                        this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                        IStorageGrid storageGrid = g.getCache(IStorageGrid.class);
                        this.monitor = storageGrid.getFluidInventory();
                        if (this.monitor == null) {
                            this.setValidContainer(false);
                        } else {
                            this.monitor.addListener(this, null);
                        }
                    }
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.monitor = monitorable.getFluidInventory();
                this.monitor.addListener(this, null);
                this.setPowerSource((IEnergySource) monitorable);
            }
        } else {
            this.monitor = null;
        }

        this.canAccessViewCells = false;
        if (monitorable instanceof IViewCellStorage) {
            for (int y = 0; y < 5; y++) {
                this.cellView[y] = new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.VIEW_CELL,
                        ((IViewCellStorage) monitorable).getViewCellStorage(),
                        y,
                        206,
                        y * 18 + 8,
                        this.getInventoryPlayer());
                this.cellView[y].setAllowEdit(this.canAccessViewCells);
                this.addSlotToContainer(this.cellView[y]);
            }
        }

        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient() && !isEssentiaMode()) {
            Slot clickSlot = (Slot) this.inventorySlots.get(idx);
            if ((clickSlot instanceof SlotPlayerInv || clickSlot instanceof SlotPlayerHotBar) && clickSlot.getHasStack()
                    && Util.FluidUtil.isFluidContainer(clickSlot.getStack())) {
                ItemStack tis = clickSlot.getStack();
                Map<Integer, IAEFluidStack> tmp = new HashMap<>();
                tmp.put(0, ItemFluidDrop.getAeFluidStack(AEItemStack.create(tis)));
                int index = Util.findItemInPlayerInvSlot(p, clickSlot.getStack());
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(tmp, tis, index));
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    protected void processItemList() {
        if (!this.fluids.isEmpty()) {
            final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();
            List<IAEFluidStack> toSend = new ArrayList<>();
            for (final IAEFluidStack is : this.fluids) {
                final IAEFluidStack send = monitorCache.findPrecise(is);
                if (send != null) {
                    toSend.add(send);
                } else {
                    is.setStackSize(0);
                    toSend.add(is);
                }
            }
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayerMP) {
                    SPacketMEUpdateBuffer.scheduleFluidUpdate((EntityPlayerMP) c, toSend);
                }
            }
            this.fluids.resetStatus();
        }
    }

    @Override
    protected void queueInventory(final ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayerMP && this.monitor != null) {
            final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();
            List<IAEFluidStack> toSend = new ArrayList<>();
            for (final IAEFluidStack is : monitorCache) {
                toSend.add(is);
            }
            SPacketMEUpdateBuffer.scheduleFluidUpdate((EntityPlayerMP) c, toSend);
        }
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
            BaseActionSource actionSource) {
        for (final IAEFluidStack is : change) {
            this.fluids.add(is);
        }
    }

    protected void dropItem(ItemStack is) {
        if (is == null || is.stackSize <= 0) return;
        ItemStack itemStack = is.copy();
        int i = itemStack.getMaxStackSize();
        while (itemStack.stackSize > 0) {
            if (i > itemStack.stackSize) {
                if (!getPlayerInv().addItemStackToInventory(itemStack.copy())) {
                    getPlayerInv().player.entityDropItem(itemStack.copy(), 0);
                }
                break;
            } else {
                itemStack.stackSize -= i;
                ItemStack item = itemStack.copy();
                item.stackSize = i;
                if (!getPlayerInv().addItemStackToInventory(item)) {
                    getPlayerInv().player.entityDropItem(item, 0);
                }
            }
        }
    }

    protected void dropItem(ItemStack itemStack, int stackSize) {
        if (itemStack == null || itemStack.stackSize <= 0) return;
        ItemStack is = itemStack.copy();
        is.stackSize = stackSize;
        this.dropItem(is);
    }

    public void postChange(Iterable<IAEFluidStack> change, ItemStack fluidContainer, EntityPlayer player,
            int slotIndex) {
        for (IAEFluidStack fluid : change) {
            IAEFluidStack nfluid = this.monitor.getStorageList().findPrecise(fluid);
            ItemStack out = fluidContainer.copy();
            out.stackSize = 1;
            if (Util.FluidUtil.isEmpty(fluidContainer) && fluid != null) {
                // add fluid to tanks
                if (nfluid.getStackSize() <= 0) continue;
                final IAEFluidStack toExtract = nfluid.copy();
                MutablePair<Integer, ItemStack> fillStack = Util.FluidUtil.fillStack(out, toExtract.getFluidStack());
                if (fillStack.right == null || fillStack.left <= 0) continue;
                toExtract.setStackSize((long) fillStack.left * fluidContainer.stackSize);
                // The real result may don't match simulation result.
                if (fillStack.right.getItem() instanceof IFluidContainerItem) {
                    IAEFluidStack real = this.host.getFluidInventory()
                            .extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                    if (real == null) continue;
                    // Refill the container
                    out = fluidContainer.copy();
                    out.stackSize = 1;
                    fillStack = Util.FluidUtil.fillStack(out, real.getFluidStack());
                    fillStack.right.stackSize = (int) (real.getStackSize() / fillStack.left);
                    out.stackSize = fillStack.right.stackSize;
                    if ((int) (real.getStackSize() % fillStack.left) > 0) {
                        this.dropItem(
                                Util.FluidUtil.setFluidContainerAmount(
                                        fillStack.right,
                                        (int) (real.getStackSize() % fillStack.left)),
                                1);
                        out.stackSize++;
                    }
                } else if (FluidContainerRegistry.isContainer(fillStack.right)) {
                    IAEFluidStack real = this.host.getFluidInventory()
                            .extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                    if (real == null) continue;
                    // Refill the container
                    out = fluidContainer.copy();
                    out.stackSize = 1;
                    fillStack = Util.FluidUtil.fillStack(out, real.getFluidStack());
                    fillStack.right.stackSize = (int) (real.getStackSize() / fillStack.left);
                    out.stackSize = fillStack.right.stackSize;
                    toExtract.setStackSize((long) fillStack.right.stackSize * fillStack.left);
                }
                this.dropItem(fillStack.right);
            } else if (!Util.FluidUtil.isEmpty(fluidContainer)) {
                // add fluid to ae network
                AEFluidStack fluidStack = Util.getAEFluidFromItem(fluidContainer);
                final IAEFluidStack aeFluidStack = fluidStack.copy();
                // simulate result is incorrect. so I'm using other solution and ec2 both mod have same issues
                final IAEFluidStack notInserted = this.host.getFluidInventory()
                        .injectItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                MutablePair<Integer, ItemStack> drainStack = Util.FluidUtil
                        .drainStack(out.copy(), aeFluidStack.getFluidStack());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    if (fluidStack.getStackSize() == notInserted.getStackSize()) continue;
                    aeFluidStack.decStackSize(notInserted.getStackSize());

                    if (drainStack.left > aeFluidStack.getStackSize()
                            && FluidContainerRegistry.isContainer(drainStack.right)) {
                        aeFluidStack.setStackSize(drainStack.left - aeFluidStack.getStackSize());
                        this.host.getFluidInventory()
                                .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                        continue;
                    }

                    this.dropItem(drainStack.right, (int) (aeFluidStack.getStackSize() / drainStack.left)); // drop
                                                                                                            // empty
                                                                                                            // item
                    out.stackSize = (int) (notInserted.getStackSize() / drainStack.left);
                    if (drainStack.right.getItem() instanceof IFluidContainerItem) {
                        if (notInserted.getStackSize() % drainStack.left > 0) {
                            fluidStack.setStackSize((notInserted.getStackSize() % drainStack.left));
                            ((IFluidContainerItem) drainStack.right.getItem())
                                    .fill(drainStack.right, fluidStack.getFluidStack(), true);
                            this.dropItem(drainStack.right, 1);
                        }
                    } else if (FluidContainerRegistry.isContainer(drainStack.right)) {
                        if (notInserted.getStackSize() % drainStack.left > 0) {
                            aeFluidStack.setStackSize(aeFluidStack.getStackSize() % drainStack.left);
                            this.host.getFluidInventory()
                                    .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                            out.stackSize++;
                        }
                    }
                    if (slotIndex == -1) out.stackSize = fluidContainer.stackSize - out.stackSize;
                } else {
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

    protected boolean isEssentiaMode() {
        return false;
    }

    @Override
    protected boolean isWirelessTerminal() {
        return false;
    }
}
