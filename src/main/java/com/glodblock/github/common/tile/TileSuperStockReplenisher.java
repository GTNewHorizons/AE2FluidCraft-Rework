package com.glodblock.github.common.tile;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileSuperStockReplenisher extends AENetworkInvTile
        implements IAEFluidInventory, IFluidHandler, IPowerChannelState, IGridTickable, ITerminalHost {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final BiggerAppEngInventory invItems = new BiggerAppEngInventory(this, 63);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 9, Integer.MAX_VALUE);
    private final AppEngInternalAEInventory configFluids = new AppEngInternalAEInventory(this, 9);
    private final AppEngInternalAEInventory configItems = new AppEngInternalAEInventory(this, 63);
    private final BaseActionSource source;
    private boolean isPowered;
    private long totalBytes;
    private long storedFluidCount;
    private long storedItemCount;

    public TileSuperStockReplenisher() {
        super(false);
        getProxy().setIdlePowerUsage(4D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
        invItems.setMaxStackSize(Integer.MAX_VALUE);
    }

    private TickRateModulation doWork() {
        for (int i = 0; i < 9; i++) {
            IAEFluidStack invFluid = invFluids.getFluidInSlot(i);
            IAEItemStack configFluid = configFluids.getAEStackInSlot(i);
            if (configFluid != null) {
                IAEFluidStack fs = AEFluidStack.create(ItemFluidPacket.getFluidStack(configFluid));
                if (invFluid == null) requestFluid(fs, i);
                else if (invFluid.equals(fs)) {
                    long invSize = invFluid.getStackSize();
                    long confSize = fs.getStackSize();
                    if (invSize < confSize / 2f) {
                        fs.setStackSize(confSize - invSize);
                        requestFluid(fs, i);
                    } else if (invSize > confSize) {
                        returnFluid(i, invSize - confSize);
                    }
                } else {
                    returnFluid(i, Long.MAX_VALUE);
                }
            } else if (invFluid != null) {
                returnFluid(i, Long.MAX_VALUE);
            }
        }

        for (int i = 0; i < 63; i++) {
            ItemStack invItem = invItems.getStackInSlot(i);
            IAEItemStack configItem = configItems.getAEStackInSlot(i);

            if (configItem != null) {
                IAEItemStack is = configItem.copy();
                if (invItem == null) requestItem(is, i);
                else if (is.equals(invItem)) {
                    int confSize = (int) is.getStackSize();
                    if (invItem.stackSize < confSize / 2f) {
                        is.setStackSize(confSize - invItem.stackSize);
                        requestItem(is, i);
                    } else if (invItem.stackSize > confSize) {
                        returnItem(i, invItem.stackSize - confSize);
                    }
                } else {
                    returnItem(i, Integer.MAX_VALUE);
                }
            } else if (invItem != null) {
                returnItem(i, Integer.MAX_VALUE);
            }
        }

        return TickRateModulation.SAME;
    }

    private void returnFluid(int index, long amount) {
        try {
            IAEFluidStack ias = invFluids.drain(index, amount, true);
            storedFluidCount -= ias.getStackSize();
            IAEFluidStack notInserted = this.getProxy().getStorage().getFluidInventory()
                    .injectItems(ias, Actionable.MODULATE, this.source);
            if (notInserted != null) {
                invFluids.fill(index, notInserted, true);
                storedFluidCount += notInserted.getStackSize();
            }
        } catch (final GridAccessException ignored) {}
    }

    private void requestFluid(IAEFluidStack fs, int index) {
        if (getRemainingFluidCount() == 0) return;
        try {
            long remFluid = getRemainingFluidCount();
            if (fs.getStackSize() > remFluid) fs.setStackSize(remFluid);
            IAEFluidStack extracted = this.getProxy().getStorage().getFluidInventory()
                    .extractItems(fs, Actionable.MODULATE, this.source);
            if (extracted != null) {
                storedFluidCount += extracted.getStackSize();
                invFluids.fill(index, extracted, true);
            }
        } catch (final GridAccessException ignored) {}
    }

    private void returnItem(int index, int amount) {
        try {
            ItemStack is = invItems.getStackInSlot(index);
            if (amount != Integer.MAX_VALUE) {
                is.stackSize = is.stackSize - amount;
                is = is.copy();
                is.stackSize = amount;
                storedItemCount -= is.stackSize;
            } else {
                invItems.setInventorySlotContents(index, null);
            }

            IAEItemStack notInserted = this.getProxy().getStorage().getItemInventory()
                    .injectItems(AEItemStack.create(is), Actionable.MODULATE, this.source);
            if (notInserted != null) {
                ItemStack tempStack = invItems.getStackInSlot(index);
                if (tempStack != null) {
                    tempStack.stackSize = tempStack.stackSize + (int) notInserted.getStackSize();
                    storedItemCount += notInserted.getStackSize();

                    saveChanges();
                } else invItems.setInventorySlotContents(index, notInserted.getItemStack());
            }
        } catch (final GridAccessException ignored) {}
    }

    private void requestItem(IAEItemStack is, int index) {
        if (getRemainingItemCount() == 0) return;
        try {
            long remItem = getRemainingItemCount();
            if (is.getStackSize() > remItem) is.setStackSize(remItem);
            IAEItemStack extracted = this.getProxy().getStorage().getItemInventory()
                    .extractItems(is, Actionable.MODULATE, this.source);
            if (extracted != null) {
                ItemStack tempStack = invItems.getStackInSlot(index);
                if (tempStack != null) {
                    tempStack.stackSize = tempStack.stackSize + (int) extracted.getStackSize();
                    storedItemCount += extracted.getStackSize();

                    saveChanges();
                } else invItems.setInventorySlotContents(index, extracted.getItemStack());
            }
        } catch (final GridAccessException ignored) {}
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return IntStream.rangeClosed(0, 62).toArray();
    }

    @Nonnull
    @Override
    public IInventory getInternalInventory() {
        return invItems;
    }

    public AppEngInternalAEInventory getConfigFluid() {
        return configFluids;
    }

    public AppEngInternalAEInventory getConfigItems() {
        return configItems;
    }

    public AppEngInternalInventory getCell() {
        return cell;
    }

    public long getFreeBytes() {
        return totalBytes - (((storedItemCount + getUnusedItemCount()) / 8)
                + ((storedFluidCount + getUnusedFluidCount()) / 2048));
    }

    public long getRemainingFluidCount() {
        final double remaining = (double) getFreeBytes() * 2048 + this.getUnusedFluidCount();
        if (remaining > Long.MAX_VALUE) return Long.MAX_VALUE;
        return remaining > 0 ? (long) remaining : 0;
    }

    public int getUnusedFluidCount() {
        final int div = (int) (storedFluidCount % 2048);
        if (div == 0) {
            return 0;
        }
        return 2048 - div;
    }

    public long getRemainingItemCount() {
        final long remaining = getFreeBytes() * 8 + this.getUnusedItemCount();

        return remaining > 0 ? remaining : 0;
    }

    public int getUnusedItemCount() {
        final long div = storedItemCount % 8;

        if (div == 0) {
            return 0;
        }

        return (int) (8 - div);
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        switch (mc) {
            case setInventorySlotContents -> {
                if (inv == cell) {
                    if (added != null) {
                        if (added.getItem() instanceof ItemBasicStorageCell ibsc) {
                            totalBytes = ibsc.getBytesLong(added);
                        } else if (added.getItem() instanceof FCBaseItemCell fcbic) {
                            totalBytes = fcbic.getBytes(added);
                        }
                        getProxy().setIdlePowerUsage(Math.sqrt(Math.pow(totalBytes, 0.576D)));
                    } else if (removed != null) {
                        totalBytes = 0;
                        getProxy().setIdlePowerUsage(4d);
                    }
                } else if (inv == invItems) {
                    if (added != null) {
                        storedItemCount += added.stackSize;
                    }

                    if (removed != null) {
                        storedItemCount -= removed.stackSize;
                    }
                }
            }
            case decreaseStackSize -> {
                if (inv == cell) {
                    totalBytes = 0;
                    getProxy().setIdlePowerUsage(4d);
                }

                if (inv == invItems) {
                    storedItemCount -= removed.stackSize;
                }
            }
            case markDirty -> markDirty();
        }

        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            AELog.error(e, "Couldn't wake up level emitter for delayed updates");
        }
    }

    public void fullRefund() {
        for (int i = 0; i < 9; i++) {
            if (invFluids.getFluidStackInSlot(i) != null) {
                returnFluid(i, Long.MAX_VALUE);
            }
        }

        for (int i = 0; i < 63; i++) {
            if (invItems.getStackInSlot(i) != null) {
                returnItem(i, Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public void getDrops(World w, int x, int y, int z, List<ItemStack> drops) {
        if (cell.getStackInSlot(0) != null) drops.add(cell.getStackInSlot(0));
        for (int i = 0; i < 9; i++) {
            ItemStack ifp = ItemFluidPacket.newStack(invFluids.getFluidStackInSlot(i));
            if (ifp != null) drops.add(ifp);
        }
        super.getDrops(w, x, y, z, drops);
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack insertingItem, int side) {
        return false;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        FluidStack fs = invFluids.drain(from, resource, doDrain);
        if (doDrain && fs != null) storedFluidCount -= fs.amount;
        return fs;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack fs = invFluids.drain(from, maxDrain, doDrain);
        if (doDrain && fs != null) storedFluidCount -= fs.amount;
        return fs;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return invFluids.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return invFluids.getTankInfo(from);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return this.invFluids;
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream_TileSuperStockReplenisher(final ByteBuf data) {
        final boolean oldPower = isPowered;
        isPowered = data.readBoolean();
        return isPowered != oldPower;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream_TileSuperStockReplenisher(final ByteBuf data) {
        data.writeBoolean(isActive());
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        invItems.readFromNBT(data, "ItemInv");
        invFluids.readFromNBT(data, "FluidInv");
        configItems.readFromNBT(data, "configItems");
        configFluids.readFromNBT(data, "configFluids");
        cell.readFromNBT(data, "cellHolder");
        totalBytes = data.getLong("totalBytes");
        storedFluidCount = data.getLong("storedFluidCount");
        storedItemCount = data.getLong("storedItemCount");
        getProxy().setIdlePowerUsage(data.getDouble("powerDraw"));
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        invItems.writeToNBT(data, "ItemInv");
        invFluids.writeToNBT(data, "FluidInv");
        configItems.writeToNBT(data, "configItems");
        configFluids.writeToNBT(data, "configFluids");
        cell.writeToNBT(data, "cellHolder");
        data.setLong("totalBytes", totalBytes);
        data.setLong("storedFluidCount", storedFluidCount);
        data.setLong("storedItemCount", storedItemCount);
        data.setDouble("powerDraw", getProxy().getIdlePowerUsage());
        return data;
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        this.updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        this.updatePowerState();
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    private void updatePowerState() {
        boolean newState = false;
        try {
            newState = this.getProxy().isActive()
                    && this.getProxy().getEnergy().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG)
                            > 0.0001;
        } catch (final GridAccessException ignored) {}
        if (newState != this.isPowered) {
            this.isPowered = newState;
            this.markForUpdate();
        }
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(120, 120, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        return doWork();
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }
}
