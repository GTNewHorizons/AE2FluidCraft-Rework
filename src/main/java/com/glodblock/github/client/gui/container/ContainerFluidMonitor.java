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

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.base.FCContainerMonitor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketMEUpdateBuffer;
import com.glodblock.github.util.Util;

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

    /**
     * Two operations, insert and extract.
     *
     * @param slotIndex The slot. If index == -1, the cursor held itemstack. Else, slotIndex indicates which slot was
     *                  shift clicked on.
     */
    public void postChange(Iterable<IAEFluidStack> change, ItemStack fluidContainer, EntityPlayer player,
            int slotIndex) {
        int cursorItems;
        if (slotIndex == -1) {
            cursorItems = player.inventory.getItemStack().stackSize;
        } else {
            cursorItems = player.inventory.getStackInSlot(slotIndex).stackSize;
        }
        for (IAEFluidStack fluid : change) {
            // The primary output itemstack
            if (Util.FluidUtil.isEmpty(fluidContainer) && fluid != null) {
                // Situation 1.a: Empty fluid container, and nonnull slot
                extractFluid(fluid, fluidContainer, player, cursorItems);
            } else if (!Util.FluidUtil.isEmpty(fluidContainer)) {
                // Situation 2.a: We are holding a non-empty container.
                insertFluid(fluidContainer, player, slotIndex, cursorItems);
                // End of situation 2.a
            }
            // No op (Any other situation)
        }
        this.detectAndSendChanges();
    }

    /**
     * The insert operation. For input, we have a filled container stack. For outputs, we have the following: 1.
     * Leftover filled container stack - primary output. 2. Empty containers 3. Partially filled container x1 In order
     * above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void insertFluid(ItemStack fluidContainer, EntityPlayer player, int slotIndex, int heldContainers) {
        IAEFluidStack fluid;
        int fluidInTank;
        if (fluidContainer.getItem() instanceof IFluidContainerItem) {
            fluid = AEFluidStack.create(((IFluidContainerItem) fluidContainer.getItem()).getFluid(fluidContainer));
            fluidInTank = (int) fluid.getStackSize();
        } else if (FluidContainerRegistry.isContainer(fluidContainer)) {
            fluid = AEFluidStack.create(FluidContainerRegistry.getFluidForFilledItem(fluidContainer));
            fluidInTank = (int) fluid.getStackSize();
        } else {
            return;
        }
        // Simulate to know how much will not be inserted
        IAEFluidStack canSend = fluid.copy();
        canSend.setStackSize((long) fluidInTank * fluidContainer.stackSize);
        final IAEFluidStack notInserted = this.host.getFluidInventory()
                .injectItems(canSend, Actionable.MODULATE, this.getActionSource());
        int emptyTanks = fluidContainer.stackSize;
        ItemStack partialStack = fluidContainer.copy();
        if (notInserted != null && notInserted.getStackSize() > 0) {
            // Here, we cannot insert everything into the system.
            if (canSend.getStackSize() == notInserted.getStackSize()) return; // System was full, no op
            // Now "refill" the buckets that couldn't enter the system
            int refilled = (int) (notInserted.getStackSize() / fluidInTank); // # of full tanks left
            emptyTanks -= refilled;
            final int remainder = (int) (notInserted.getStackSize() % fluidInTank); // partial tank
            // Now handle remaining if applicable
            if (remainder > 0) {
                notInserted.setStackSize(remainder);
                partialStack.stackSize = 1;
                if (partialStack.getItem() instanceof IFluidContainerItem) {
                    ((IFluidContainerItem) partialStack.getItem()).drain(partialStack, fluidInTank - remainder, true);
                } else if (FluidContainerRegistry.isContainer(fluidContainer)) {
                    // For "whole containers" (need to round down, no remainders)
                    ItemStack container = FluidContainerRegistry
                            .fillFluidContainer(notInserted.getFluidStack(), fluidContainer);
                    if (container == null) {
                        // whatever, lets just not have the ME system get filled with any of its fluid
                        emptyTanks++;
                        notInserted.setStackSize(fluidInTank - remainder);
                        this.host.getFluidInventory()
                                .extractItems(notInserted, Actionable.MODULATE, this.getActionSource());
                        partialStack.stackSize = 0;
                    } else {
                        partialStack = container;
                    }
                }
                emptyTanks -= partialStack.stackSize;
            } else {
                partialStack.stackSize = 0;
            }
        } else {
            partialStack.stackSize = 0;
        }
        // Now that the 3 outputs are handled, we can now actually put the fluids in
        ItemStack emptyStack = fluidContainer.copy();
        emptyStack.stackSize = 1;
        if (emptyStack.getItem() instanceof IFluidContainerItem) {
            ((IFluidContainerItem) emptyStack.getItem()).drain(emptyStack, fluidInTank, true);
        } else {
            emptyStack = FluidContainerRegistry.drainFluidContainer(emptyStack);
        }
        emptyStack.stackSize = emptyTanks;
        int extraTanks = heldContainers - emptyTanks - partialStack.stackSize;
        // Done. Put the output in the inventory or ground, and update stack size.
        boolean shouldSendStack = true;
        if (slotIndex == -1) {
            if (extraTanks > 0) {
                ItemStack stack = player.inventory.getItemStack();
                stack.stackSize = extraTanks;
                adjustStack(stack);
                dropItem(emptyStack);
                dropItem(partialStack);
            } else if (emptyTanks != 0) {
                adjustStack(emptyStack);
                player.inventory.setItemStack(emptyStack);
                dropItem(partialStack);
            } else if (partialStack.stackSize != 0) {
                player.inventory.setItemStack(partialStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        } else {
            ItemStack stack = player.inventory.getStackInSlot(slotIndex);
            if (extraTanks > 0) {
                stack.stackSize = extraTanks;
                adjustStack(stack);
                dropItem(emptyStack);
                dropItem(partialStack);
            } else if (emptyTanks != 0) {
                adjustStack(emptyStack);
                player.inventory.setInventorySlotContents(slotIndex, emptyStack);
                dropItem(partialStack);
            } else if (partialStack.stackSize != 0) {
                player.inventory.setInventorySlotContents(slotIndex, partialStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        }
        if (shouldSendStack) {
            FluidCraft.proxy.netHandler.sendTo(
                    new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                    (EntityPlayerMP) player);
        } else {
            FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);

        }
    }

    /**
     * The extract operation. For input, we have an empty container stack. For outputs, we have the following: 1.
     * Leftover empty container stack - primary output. 2. Filled containers (full) 3. Partially filled container x1 In
     * order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void extractFluid(IAEFluidStack fluid, ItemStack fluidContainer, EntityPlayer player, int heldContainers) {
        IAEFluidStack storedFluid = this.monitor.getStorageList().findPrecise(fluid);
        if (storedFluid == null || storedFluid.getStackSize() <= 0) return;
        int capacity = Util.FluidUtil.getCapacity(fluidContainer, storedFluid.getFluid());
        if (capacity == 0) return;
        // The fluidstack that we will try to extract from the system.
        final IAEFluidStack canExtract = storedFluid.copy();
        // Maximum amount of fluid we can extract with the given container(s). Guarantees that we won't
        // run into issues w/ remainder fluid later.
        canExtract.setStackSize((long) capacity * fluidContainer.stackSize);
        IAEFluidStack actualExtract = this.host.getFluidInventory()
                .extractItems(canExtract, Actionable.MODULATE, this.getActionSource());
        if (actualExtract == null) return;
        // Calculate the number of full fluid containers we extracted
        long toExtract = actualExtract.getStackSize();
        int filledTanks = (int) (toExtract / capacity);
        int remainder = (int) (toExtract % capacity);
        int emptyTanks = heldContainers - filledTanks;
        // Fill the filled tanks
        ItemStack filledStack = fluidContainer.copy();
        filledStack.stackSize = 1;
        if (filledTanks > 0) {
            if (filledStack.getItem() instanceof IFluidContainerItem) {
                int amt = ((IFluidContainerItem) filledStack.getItem())
                        .fill(filledStack, actualExtract.getFluidStack(), true);
                assert amt == capacity;
            } else {
                // Should not be NPE since we affirmed that we have enough capacity.
                filledStack = FluidContainerRegistry.fillFluidContainer(actualExtract.getFluidStack(), filledStack);
                assert capacity == FluidContainerRegistry.getContainerCapacity(filledStack);
            }
            filledStack.stackSize = filledTanks;
        } else {
            filledStack.stackSize = 0;
        }
        // Calculate the remaining fluid to extract, if any
        ItemStack partialStack = fluidContainer.copy();
        if (remainder > 0) {
            actualExtract.setStackSize(remainder);
            partialStack.stackSize = 1;
            if (partialStack.getItem() instanceof IFluidContainerItem) {
                int amt = ((IFluidContainerItem) partialStack.getItem())
                        .fill(partialStack, actualExtract.getFluidStack(), true);
                assert amt == remainder;
                emptyTanks--;
            } else {
                ItemStack dummy = FluidContainerRegistry
                        .fillFluidContainer(actualExtract.getFluidStack(), partialStack);
                if (dummy == null) {
                    // Failed to fill partial...
                    partialStack.stackSize = 0;
                } else {
                    partialStack = dummy;
                    emptyTanks--;
                }
            }
        } else {
            partialStack.stackSize = 0;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        // We can assume slotIndex == -1, since we don't actually allow extraction via shift click.
        boolean shouldSendStack = true;
        if (emptyTanks > 0) {
            ItemStack emptyStack = player.inventory.getItemStack();
            emptyStack.stackSize = emptyTanks;
            adjustStack(emptyStack);
            dropItem(filledStack);
            dropItem(partialStack);
        } else if (filledStack.stackSize != 0) {
            adjustStack(filledStack);
            player.inventory.setItemStack(filledStack);
            dropItem(partialStack);
        } else if (partialStack.stackSize != 0) {
            player.inventory.setItemStack(partialStack);
        } else {
            player.inventory.setItemStack(null);
            shouldSendStack = false;
        }
        if (shouldSendStack) {
            FluidCraft.proxy.netHandler.sendTo(
                    new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                    (EntityPlayerMP) player);
        } else {
            FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
        }
    }

    void adjustStack(ItemStack stack) {
        if (stack.stackSize > stack.getMaxStackSize()) {
            dropItem(stack, stack.stackSize - stack.getMaxStackSize());
            stack.stackSize = stack.getMaxStackSize();
        }
    }

    protected boolean isEssentiaMode() {
        return false;
    }

    @Override
    protected boolean isWirelessTerminal() {
        return false;
    }
}
