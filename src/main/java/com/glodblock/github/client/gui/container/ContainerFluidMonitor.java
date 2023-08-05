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
import net.minecraftforge.fluids.FluidStack;
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
     * Insert fluid into the ME system
     * 
     * @param canSend     the fluidstack to insert
     * @param results     a tuple of at least size 3 to indicate outputs (1), (2), (3). This will be mutated!
     * @param numTanks    number of tanks involved in the operation
     * @param fluidInTank how much fluid is in the tank initially
     * @return the amount of fluid remaining in output (3)
     * @see #insertFluid(ItemStack, EntityPlayer, int, int)
     */
    private int insertME(IAEFluidStack canSend, int[] results, int numTanks, int fluidInTank) {
        final IAEFluidStack notInserted = this.host.getFluidInventory()
                .injectItems(canSend, Actionable.MODULATE, this.getActionSource());

        // Check if we need to handle partial insertion.
        if (notInserted != null && notInserted.getStackSize() > 0) {
            if (canSend.getStackSize() == notInserted.getStackSize()) {
                // No-op for when system is full
                results[REM_IDX] = numTanks;
                return fluidInTank;
            }
            // Populate result list with outputs (1), (2), (3).
            results[REM_IDX] = (int) (notInserted.getStackSize() / fluidInTank);
            results[ACT_IDX] = numTanks - results[REM_IDX];

            // If there is remaining fluid, set partial.
            final int remainder = (int) (notInserted.getStackSize() % fluidInTank);
            if (remainder > 0) {
                results[PARTIAL_IDX] = 1;
                results[ACT_IDX]--;
                return remainder;
            } else {
                results[PARTIAL_IDX] = 0;
            }
        } else {
            // Full operation successful
            results[REM_IDX] = 0;
            results[ACT_IDX] = numTanks;
            results[PARTIAL_IDX] = 0;
        }
        return 0;
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
    private void insertFluid(ItemStack fluidContainer, EntityPlayer player, int slotIndex, int heldContainers) {
        IAEFluidStack canSend;
        int fluidPerContainer;
        ItemStack partialStack = null;
        ItemStack emptyStack = null;
        int[] insertionResults = new int[3];

        // 2 types of containers: IFluidContainerItem and FluidContainerRegistry
        // IFluidContainerItem - items are filled w/ volume (portable tanks)
        // FluidContainerRegistry - items are filled, or not (buckets)
        // The former is easy, the latter takes a bit more work.
        if (fluidContainer.getItem() instanceof IFluidContainerItem fcItem) {
            // Step 1: Find out how much fluid we can insert.
            canSend = AEFluidStack.create(fcItem.getFluid(fluidContainer));
            fluidPerContainer = (int) canSend.getStackSize();
            canSend.setStackSize((long) fluidPerContainer * fluidContainer.stackSize);

            // Step 2: Find out how much fluid we can extract from the container. If this is null or 0, return.
            ItemStack test = fluidContainer.copy();
            test.stackSize = 1;
            FluidStack fluidStack = fcItem.drain(test, 1, false);
            if (fluidStack == null || fluidStack.amount == 0) {
                return;
            }

            // Step 3: Find out how much fluid we can insert into the ME system
            int partialAmount = insertME(canSend, insertionResults, fluidContainer.stackSize, fluidPerContainer);

            // Step 4: Separate the outputs into no drain (1), fully drained (2), and partially drained (3)
            // 4.1: Handle the partial stack
            if (insertionResults[PARTIAL_IDX] > 0) {
                partialStack = fluidContainer.copy();
                partialStack.stackSize = 1;
                fcItem.drain(partialStack, fluidPerContainer - partialAmount, true);
            }

            // 4.2: Handle empty output
            if (insertionResults[ACT_IDX] > 0) {
                emptyStack = fluidContainer.copy();
                emptyStack.stackSize = insertionResults[ACT_IDX];
                fcItem.drain(emptyStack, fluidPerContainer, true);
            }
        } else if (FluidContainerRegistry.isContainer(fluidContainer)) {
            // Step 1: Find out how much fluid we can insert.
            canSend = AEFluidStack.create(FluidContainerRegistry.getFluidForFilledItem(fluidContainer));
            fluidPerContainer = (int) canSend.getStackSize();
            canSend.setStackSize((long) fluidPerContainer * fluidContainer.stackSize);
            // Step 2: Find out how much fluid we can extract from the container. If this is null or 0, return.
            ItemStack emptyTank = FluidContainerRegistry.drainFluidContainer(fluidContainer);
            if (emptyTank == null) {
                return;
            }
            // Step 3: Find out how much fluid we can insert into the ME system
            int partialAmount = insertME(canSend, insertionResults, fluidContainer.stackSize, fluidPerContainer);

            // Step 4: Separate the outputs into no drain (1), fully drained (2), and partially drained (3)
            // 4.1: Handle the partial stack
            if (partialAmount > 0) {
                // We now extract the extra amount from the ME system. Blame simulate not working :P
                int extract = FluidContainerRegistry.getContainerCapacity(fluidContainer) - partialAmount;
                if (extract > 0) {
                    IAEFluidStack toExtract = canSend.copy().setStackSize(extract);
                    this.host.getFluidInventory().extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                }
                insertionResults[PARTIAL_IDX] = 0;
            }

            // 4.2: Handle empty output
            if (insertionResults[ACT_IDX] > 0) {
                emptyStack = FluidContainerRegistry.drainFluidContainer(fluidContainer);
                emptyStack.stackSize = insertionResults[ACT_IDX];
            }
        } else {
            return;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        boolean shouldSendStack = true;
        int emptyTanks = insertionResults[ACT_IDX];
        int extraTanks = heldContainers - emptyTanks - insertionResults[PARTIAL_IDX];
        if (slotIndex == -1) {
            // Item is in mouse hand
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
            } else if (partialStack != null) {
                player.inventory.setItemStack(partialStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        } else {
            // Shift clicked in
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
            } else if (partialStack != null) {
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
     * The extract operation. For input, we have an empty container stack. For outputs, we have the following:
     * <ol>
     * <li>Leftover empty container stack</li>
     * <li>Filled containers (full)</li>
     * <li>Partially filled container x1</li>
     * </ol>
     * In order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void extractFluid(IAEFluidStack fluid, ItemStack fluidContainer, EntityPlayer player, int heldContainers) {
        // Step 1: Check if fluid can actually get filled into the fluidContainer
        if (fluidContainer.getItem() instanceof IFluidContainerItem fcItem) {
            int test = fcItem.fill(fluidContainer, fluid.getFluidStack(), false);
            if (test == 0) {
                return;
            }
        } else if (FluidContainerRegistry.isContainer(fluidContainer)) {
            ItemStack test = FluidContainerRegistry.fillFluidContainer(fluid.getFluidStack(), fluidContainer);
            if (test == null) {
                return;
            }
        } else {
            return;
        }
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
