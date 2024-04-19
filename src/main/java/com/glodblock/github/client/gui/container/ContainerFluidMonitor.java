package com.glodblock.github.client.gui.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.base.FCContainerMonitor;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketMEFluidInvUpdate;
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

public class ContainerFluidMonitor extends FCContainerMonitor<IAEFluidStack> {

    protected final IItemList<IAEFluidStack> fluids = AEApi.instance().storage().createFluidList();
    /**
     * Index of tuple which indicates remaining tanks that are remaining (not emptied/filled)
     */
    protected static final int REM_IDX = 0;
    /**
     * Index of tuple which indicates remaining tanks that are acted upon (emptied/filled)
     */
    protected static final int ACT_IDX = 1;
    /**
     * Index of tuple which indicates tanks that are partially emptied/filled
     */
    protected static final int PARTIAL_IDX = 2;

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
                int index = Util.findItemInPlayerInvSlot(p, clickSlot.getStack());
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(index));
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
            SPacketMEFluidInvUpdate piu = new SPacketMEFluidInvUpdate();
            piu.addAll(toSend);
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayerMP) {
                    FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
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
            SPacketMEFluidInvUpdate piu = new SPacketMEFluidInvUpdate();
            piu.addAll(toSend);
            FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
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
    public void postChange(IAEFluidStack fluid, EntityPlayer player, int slotIndex, boolean shift) {
        ItemStack targetStack;
        if (slotIndex == -1) {
            targetStack = player.inventory.getItemStack();
        } else {
            targetStack = player.inventory.getStackInSlot(slotIndex);
        }
        // The primary output itemstack
        if (Util.FluidUtil.isEmpty(targetStack) && fluid != null) {
            // Situation 1.a: Empty fluid container, and nonnull slot
            extractFluid(fluid, player, slotIndex, shift);
        } else if (!Util.FluidUtil.isEmpty(targetStack)) {
            // Situation 2.a: We are holding a non-empty container.
            insertFluid(player, slotIndex, shift);
            // End of situation 2.a
        }
        // No op (Any other situation)
        this.detectAndSendChanges();
    }

    /**
     * The insert operation. For input, we have a filled container stack. For outputs, we have the following:
     * <ol>
     * <li>Leftover filled container stack</li>
     * <li>Empty containers</li>
     * <li>Partially filled container x1</li>
     * </ol>
     * In order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void insertFluid(EntityPlayer player, int slotIndex, boolean shift) {
        final ItemStack targetStack;
        if (slotIndex == -1) {
            targetStack = player.inventory.getItemStack();
        } else {
            targetStack = player.inventory.getStackInSlot(slotIndex);
        }
        final int containersRequestedToInsert = shift ? targetStack.stackSize : 1;

        // Step 1: Determine container characteristics and verify fluid to be extractable
        final int fluidPerContainer;
        final FluidStack fluidStackPerContainer;
        final boolean partialInsertSupported;
        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            ItemStack test = targetStack.copy();
            test.stackSize = 1;
            fluidStackPerContainer = fcItem.drain(test, Integer.MAX_VALUE, false);
            if (fluidStackPerContainer == null || fluidStackPerContainer.amount == 0) {
                return;
            }

            fluidPerContainer = fluidStackPerContainer.amount;
            partialInsertSupported = true;
        } else if (FluidContainerRegistry.isContainer(targetStack)) {
            ItemStack emptyTank = FluidContainerRegistry.drainFluidContainer(targetStack);
            if (emptyTank == null) {
                return;
            }

            fluidStackPerContainer = FluidContainerRegistry.getFluidForFilledItem(targetStack);
            fluidPerContainer = fluidStackPerContainer.amount;
            partialInsertSupported = false;
        } else {
            return;
        }

        // Step 2: determine network capacity
        final IAEFluidStack totalFluid = AEFluidStack.create(fluidStackPerContainer);
        totalFluid.setStackSize((long) fluidPerContainer * containersRequestedToInsert);

        final IAEFluidStack notInsertable = this.host.getFluidInventory()
                .injectItems(totalFluid, Actionable.SIMULATE, this.getActionSource());

        final long insertableFluid;
        if (notInsertable == null || notInsertable.getStackSize() == 0) {
            insertableFluid = totalFluid.getStackSize();
        } else {
            long insertable = totalFluid.getStackSize() - notInsertable.getStackSize();
            if (partialInsertSupported) {
                insertableFluid = insertable;
            } else {
                // avoid remainder
                insertableFluid = insertable - (insertable % fluidPerContainer);
            }
        }
        totalFluid.setStackSize(insertableFluid);

        // Step 3: perform insert
        final long totalInserted;
        final IAEFluidStack notInserted = this.host.getFluidInventory()
                .injectItems(totalFluid, Actionable.MODULATE, this.getActionSource());
        if (notInserted != null && notInserted.getStackSize() > 0) {
            // User has a setup that causes discrepancy between simulation and modulation. Likely double storage bus.
            long total = totalFluid.getStackSize() - notInserted.getStackSize();
            if (partialInsertSupported) {
                totalInserted = total;
            } else {
                // We cant have partially filled containers -> user will receive a fluid packet as last resort
                long overflowAmount = fluidPerContainer - (total % fluidPerContainer);
                IAEFluidStack overflow = AEFluidStack.create(fluidStackPerContainer);
                overflow.setStackSize(overflowAmount);
                dropItem(ItemFluidPacket.newStack(overflow));
                totalInserted = total + overflowAmount;
            }
        } else {
            totalInserted = totalFluid.getStackSize();
        }

        // Step 4: calculate outputs
        final int emptiedTanks = (int) (totalInserted / fluidPerContainer);
        final int partialDrain = (int) (totalInserted % fluidPerContainer);
        final int partialTanks = partialDrain > 0 && partialInsertSupported ? 1 : 0;
        final int usedTanks = emptiedTanks + partialTanks;
        final int untouchedTanks = targetStack.stackSize - usedTanks;

        final ItemStack emptiedTanksStack;
        final ItemStack partialTanksStack;

        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            if (emptiedTanks > 0) {
                emptiedTanksStack = targetStack.copy();
                emptiedTanksStack.stackSize = 1;
                fcItem.drain(emptiedTanksStack, fluidPerContainer, true);
                emptiedTanksStack.stackSize = emptiedTanks;
            } else {
                emptiedTanksStack = null;
            }
            if (partialTanks > 0) {
                partialTanksStack = targetStack.copy();
                partialTanksStack.stackSize = 1;
                fcItem.drain(partialTanksStack, partialDrain, true);
            } else {
                partialTanksStack = null;
            }
        } else {
            if (emptiedTanks > 0) {
                emptiedTanksStack = FluidContainerRegistry.drainFluidContainer(targetStack);
                emptiedTanksStack.stackSize = emptiedTanks;
            } else {
                emptiedTanksStack = null;
            }
            // Not possible > see Step 2 and Step 3
            partialTanksStack = null;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        boolean shouldSendStack = true;
        if (slotIndex == -1) {
            // Item is in mouse hand
            if (untouchedTanks > 0) {
                targetStack.stackSize = untouchedTanks;
                adjustStack(targetStack);
                dropItem(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (emptiedTanksStack != null) {
                adjustStack(emptiedTanksStack);
                player.inventory.setItemStack(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (partialTanksStack != null) {
                player.inventory.setItemStack(partialTanksStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        } else {
            // Shift clicked in
            if (untouchedTanks > 0) {
                targetStack.stackSize = untouchedTanks;
                adjustStack(targetStack);
                dropItem(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (emptiedTanksStack != null) {
                adjustStack(emptiedTanksStack);
                player.inventory.setInventorySlotContents(slotIndex, emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (partialTanksStack != null) {
                player.inventory.setInventorySlotContents(slotIndex, partialTanksStack);
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
     * The extract operation. For input, we have an empty container stack. For outputs, we have the following:
     * <ol>
     * <li>Leftover empty container stack</li>
     * <li>Filled containers (full)</li>
     * <li>Partially filled container x1</li>
     * </ol>
     * In order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void extractFluid(IAEFluidStack clientRequestedFluid, EntityPlayer player, int slotIndex, boolean shift) {
        if (slotIndex != -1) {
            // shift-click from inventory cant fill fluids
            return;
        }
        final ItemStack targetStack = player.inventory.getItemStack();
        final int containersRequestedToExtract = shift ? targetStack.stackSize : 1;

        final FluidStack clientRequestedFluidStack = clientRequestedFluid.getFluidStack();
        clientRequestedFluidStack.amount = Integer.MAX_VALUE;

        // Step 1: Determine container characteristics and verify fluid to be insertable
        final int fluidPerContainer;
        final boolean partialInsertSupported;
        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            ItemStack testStack = targetStack.copy();
            testStack.stackSize = 1;
            fluidPerContainer = fcItem.fill(testStack, clientRequestedFluidStack, false);
            if (fluidPerContainer == 0) {
                return;
            }
            partialInsertSupported = true;
        } else if (FluidContainerRegistry.isContainer(targetStack)) {
            fluidPerContainer = FluidContainerRegistry.getContainerCapacity(clientRequestedFluidStack, targetStack);
            partialInsertSupported = false;
        } else {
            return;
        }

        // Step 2: determine fluid in network
        final IAEFluidStack totalRequestedFluid = clientRequestedFluid.copy();
        totalRequestedFluid.setStackSize((long) fluidPerContainer * containersRequestedToExtract);

        final IAEFluidStack availableFluid = this.host.getFluidInventory()
                .extractItems(totalRequestedFluid, Actionable.SIMULATE, this.getActionSource());
        if (availableFluid == null || availableFluid.getStackSize() == 0) {
            return;
        }

        if (availableFluid.getStackSize() != totalRequestedFluid.getStackSize() && !partialInsertSupported) {
            availableFluid.decStackSize(availableFluid.getStackSize() % fluidPerContainer);
        }

        // Step 3: perform extract
        final IAEFluidStack extracted = this.host.getFluidInventory()
                .extractItems(availableFluid, Actionable.MODULATE, this.getActionSource());
        final long totalExtracted = extracted != null ? extracted.getStackSize() : 0;

        // Step 4: calculate outputs
        final int filledTanks = (int) (totalExtracted / fluidPerContainer);
        final int partialFill = (int) (totalExtracted % fluidPerContainer);
        final int partialTanks = partialFill > 0 && partialInsertSupported ? 1 : 0;
        final int usedTanks = filledTanks + partialTanks;
        final int untouchedTanks = targetStack.stackSize - usedTanks;

        final ItemStack filledTanksStack;
        final ItemStack partialTanksStack;

        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            if (filledTanks > 0) {
                filledTanksStack = targetStack.copy();
                filledTanksStack.stackSize = 1;
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = fluidPerContainer;
                fcItem.fill(filledTanksStack, toInsert, true);
                filledTanksStack.stackSize = filledTanks;
            } else {
                filledTanksStack = null;
            }
            if (partialTanks > 0) {
                partialTanksStack = targetStack.copy();
                partialTanksStack.stackSize = 1;
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = partialFill;
                fcItem.fill(partialTanksStack, toInsert, true);
            } else {
                partialTanksStack = null;
            }
        } else {
            if (filledTanks > 0) {
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = fluidPerContainer;
                filledTanksStack = FluidContainerRegistry.fillFluidContainer(toInsert, targetStack);
                filledTanksStack.stackSize = filledTanks;
            } else {
                filledTanksStack = null;
            }
            if (partialFill > 0) {
                // User has a setup that causes discrepancy between simulation and modulation. Likely double storage
                // bus.
                // We cant have partially filled containers -> user will receive a fluid packet as last resort
                IAEFluidStack overflow = extracted.copy();
                overflow.setStackSize(partialFill);
                dropItem(ItemFluidPacket.newStack(overflow));
            }
            partialTanksStack = null;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        // We can assume slotIndex == -1, since we don't actually allow extraction via shift click.
        boolean shouldSendStack = true;
        if (untouchedTanks > 0) {
            ItemStack emptyStack = player.inventory.getItemStack();
            emptyStack.stackSize = untouchedTanks;
            adjustStack(emptyStack);
            dropItem(filledTanksStack);
            dropItem(partialTanksStack);
        } else if (filledTanksStack != null) {
            adjustStack(filledTanksStack);
            player.inventory.setItemStack(filledTanksStack);
            dropItem(partialTanksStack);
        } else if (partialTanksStack != null) {
            player.inventory.setItemStack(partialTanksStack);
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
        if (stack != null && stack.stackSize > stack.getMaxStackSize()) {
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
