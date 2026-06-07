package com.glodblock.github.common.tile;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.gtnewhorizon.gtnhlib.item.ItemStackNBT;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.tile.inventory.IAEStackInventory;
import appeng.tile.inventory.IIAEStackInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;

public class TileSuperStockReplenisher extends AENetworkInvTile implements IAEFluidInventory, IFluidHandler,
        IPowerChannelState, IGridTickable, ITerminalHost, IIAEStackInventory {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final BiggerAppEngInventory invItems = new BiggerAppEngInventory(this, 63);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 9, Integer.MAX_VALUE);
    private final IAEStackInventory configItems = new IAEStackInventory(this, 63, StorageName.CONFIG);
    private final IAEStackInventory configFluids = new IAEStackInventory(this, 9, StorageName.NONE) {

        @Override
        public void readFromNBT(@Nullable NBTTagCompound data, String name) {
            super.readFromNBT(data, name);

            for (int i = 0; i < this.getSizeInventory(); i++) {
                final IAEStack<?> aes = this.getAEStackInSlot(i);
                if (aes instanceof IAEItemStack ais && ais.getItem() instanceof ItemFluidPacket) {
                    final IAEFluidStack ifs = ItemFluidPacket.getFluidAEStack(ais);
                    if (ifs != null && ifs.getStackSize() > Integer.MAX_VALUE) ifs.setStackSize(Integer.MAX_VALUE);
                    this.putAEStackInSlot(i, ifs);
                }
            }
        }
    };
    private final BaseActionSource source;
    private boolean isPowered;
    private long totalBytes;
    private long storedFluidCount;
    private long storedItemCount;
    protected boolean isFullStockMode;
    protected boolean isSlotsAccessible;
    protected boolean modeChange;

    private boolean needReCountStoredFluids = true;
    private boolean needReCountStoredItems = true;

    public TileSuperStockReplenisher() {
        super(false);
        this.getProxy().setIdlePowerUsage(4D);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
        invItems.setMaxStackSize(Integer.MAX_VALUE);
        this.isFullStockMode = false;
        this.isSlotsAccessible = true;
        this.modeChange = false;
    }

    private TickRateModulation doWork() {
        this.fletchFluids();
        this.fletchItems();

        this.needReCountStoredFluids = true;
        this.needReCountStoredItems = true;

        return TickRateModulation.SAME;
    }

    private void fletchFluids() {
        for (int i = 0; i < 9; i++) {
            IAEFluidStack invFluid = invFluids.getFluidInSlot(i);
            IAEStack<?> configFluid = configFluids.getAEStackInSlot(i);
            if (configFluid instanceof IAEFluidStack fs) {
                IAEFluidStack ifs = fs.copy();
                if (invFluid == null) requestFluid(ifs, i);
                else if (invFluid.equals(ifs)) {
                    long invSize = invFluid.getStackSize();
                    long confSize = ifs.getStackSize();
                    if (!this.isFullStockMode() && (invSize < confSize / 2f)
                            || (this.isFullStockMode() && (invSize < confSize))) {
                        ifs.setStackSize(confSize - invSize);
                        requestFluid(ifs, i);
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
    }

    private void fletchItems() {
        for (int i = 0; i < 63; i++) {
            ItemStack invItem = invItems.getStackInSlot(i);
            IAEStack<?> configItem = configItems.getAEStackInSlot(i);
            if (configItem instanceof IAEItemStack ais) {
                IAEItemStack is = ais.copy();
                if (invItem == null) requestItem(is, i);
                else if (is.equals(invItem)) {
                    int invSize = invItem.stackSize;
                    int confSize = (int) is.getStackSize();
                    if ((!this.isFullStockMode() && (invSize < confSize / 2f))
                            || ((this.isFullStockMode()) && (invSize < confSize))) {
                        is.setStackSize(confSize - invSize);
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
    }

    private void countFluids() {
        if (this.needReCountStoredFluids) {
            this.needReCountStoredFluids = false;
            this.storedFluidCount = 0;
            for (int i = 0; i < this.invFluids.getSlots(); i++) {
                final IAEFluidStack fs = this.invFluids.getFluidInSlot(i);
                if (fs != null) this.storedFluidCount += fs.getStackSize();
            }
        }
    }

    private void countItems() {
        if (this.needReCountStoredItems) {
            this.needReCountStoredItems = false;
            this.storedItemCount = 0;
            for (int i = 0; i < this.invItems.getSizeInventory(); i++) {
                final ItemStack is = this.invItems.getStackInSlot(i);
                if (is != null) this.storedItemCount += is.stackSize;
            }
        }
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
                checkSlotsAccessible();
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
                checkSlotsAccessible();
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
            } else {
                invItems.setInventorySlotContents(index, null);
            }

            this.storedItemCount -= is.stackSize;

            IAEItemStack notInserted = this.getProxy().getStorage().getItemInventory()
                    .injectItems(AEItemStack.create(is), Actionable.MODULATE, this.source);
            if (notInserted != null) {
                if (invItems.getStackInSlot(index) != null) {
                    ItemStack tempStack = invItems.getStackInSlot(index).copy();
                    tempStack.stackSize = tempStack.stackSize + (int) notInserted.getStackSize();
                    invItems.setInventorySlotContents(index, tempStack);
                    checkSlotsAccessible();
                } else {
                    invItems.setInventorySlotContents(index, notInserted.getItemStack());
                    checkSlotsAccessible();
                }
                this.storedItemCount += notInserted.getStackSize();
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
                if (invItems.getStackInSlot(index) != null) {
                    ItemStack tempStack = invItems.getStackInSlot(index).copy();
                    tempStack.stackSize = tempStack.stackSize + (int) extracted.getStackSize();
                    invItems.setInventorySlotContents(index, tempStack);
                    checkSlotsAccessible();

                    // saveChanges();
                } else {
                    invItems.setInventorySlotContents(index, extracted.getItemStack());
                    checkSlotsAccessible();
                }

                this.storedItemCount += extracted.getStackSize();
            }
        } catch (final GridAccessException ignored) {}
    }

    public boolean isFullStockMode() {
        return this.isFullStockMode;
    }

    public void setFullStockMode(boolean fullStockMode) {

        if (this.isFullStockMode == fullStockMode) return;

        if (fullStockMode) this.modeChange = true;

        this.isFullStockMode = fullStockMode;
        checkSlotsAccessible();
        this.modeChange = false;
    }

    public boolean isSlotsAccessible() {
        return this.isSlotsAccessible;
    }

    public boolean hasModeChanged() {
        return this.modeChange;
    }

    public void setSlotsAccessible() {
        if (((this.isFullStockMode() && this.isFullyStocked()) && (!this.isSlotsAccessible()))
                || (!this.isFullStockMode())) {
            try {
                this.getProxy().getGrid().postEvent(
                        new MENetworkStorageEvent(
                                this.getProxy().getStorage().getItemInventory(),
                                this.invItems.getMEInventory().getStackType()));
            } catch (GridAccessException ignored) {}

            this.isSlotsAccessible = true;

        } else if (((this.isFullStockMode() && this.needsFullyStocked()) && (this.isSlotsAccessible()))
                || (this.hasModeChanged())) {
                    try {
                        this.getProxy().getGrid().postEvent(
                                new MENetworkStorageEvent(
                                        this.getProxy().getStorage().getItemInventory(),
                                        this.invItems.getMEInventory().getStackType()));
                    } catch (GridAccessException ignored) {}

                    this.isSlotsAccessible = false;
                    this.isSlotsAccessible = false;

                } else
            if ((this.isFullStockMode() && this.isFullyStocked()) && (this.isSlotsAccessible())) {
                try {
                    this.getProxy().getGrid().postEvent(
                            new MENetworkStorageEvent(
                                    this.getProxy().getStorage().getItemInventory(),
                                    this.invItems.getMEInventory().getStackType()));
                } catch (GridAccessException ignored) {}

            }
                } else
            if ((this.isFullStockMode() && this.isFullyStocked()) && (this.isSlotsAccessible())) {
                try {
                    this.getProxy().getGrid().postEvent(
                            new MENetworkStorageEvent(
                                    this.getProxy().getStorage().getItemInventory(),
                                    this.invItems.getMEInventory().getStackType()));
                } catch (GridAccessException ignored) {}

            }

    }

    public void checkSlotsAccessible() {
        this.setSlotsAccessible();
        this.notifyNeighbors();
    }

    private boolean needsFullyStocked() {
        int configSlots = 0;
        int emptySlots = 0;

        for (int i = 0; i < 9; i++) {
            IAEStack<?> config = configFluids.getAEStackInSlot(i);

            if (config instanceof IAEFluidStack cfg) {
                configSlots++;
                IAEFluidStack inv = invFluids.getFluidInSlot(i);

                if (inv == null) {
                    emptySlots++;
                }
            }
        }

        for (int i = 0; i < 63; i++) {
            IAEStack<?> config = configItems.getAEStackInSlot(i);

            if (config instanceof IAEItemStack cfg) {
                configSlots++;
                ItemStack inv = invItems.getStackInSlot(i);

                if (inv == null) {
                    emptySlots++;
                }
            }
        }

        return (emptySlots == configSlots) && (configSlots > 0);
    }

    private boolean isFullyStocked() {

        for (int i = 0; i < 9; i++) {
            IAEStack<?> config = configFluids.getAEStackInSlot(i);

            if (config instanceof IAEFluidStack cfg) {
                IAEFluidStack cfgFis = cfg.copy();
                IAEFluidStack inv = invFluids.getFluidInSlot(i);

                if (inv == null || inv.getStackSize() < cfgFis.getStackSize()) {
                    return false;
                }
            }
        }

        for (int i = 0; i < 63; i++) {
            IAEStack<?> config = configItems.getAEStackInSlot(i);

            if (config instanceof IAEItemStack cfg) {
                IAEItemStack cfgIs = cfg.copy();
                ItemStack inv = invItems.getStackInSlot(i);

                if (inv == null || inv.stackSize < (int) cfgIs.getStackSize()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return ((this.isSlotsAccessible()) ? IntStream.rangeClosed(0, 62).toArray() : new int[0]);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStackIn, int side) {
        return this.isSlotsAccessible();
    }

    public void notifyNeighbors() {
        if (this != null && this.getWorldObj() != null) {
            this.markDirty();
            this.getWorldObj().notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            this.getWorldObj().markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Nonnull
    @Override
    public IInventory getInternalInventory() {
        return invItems;
    }

    public IAEStackInventory getConfigFluids() {
        return this.configFluids;
    }

    public IAEStackInventory getConfigItems() {
        return this.configItems;
    }

    public AppEngInternalInventory getCell() {
        return cell;
    }

    public long getFreeBytes() {
        this.countFluids();
        this.countItems();

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
                }
            }
            case decreaseStackSize -> {
                if (inv == cell) {
                    totalBytes = 0;
                    getProxy().setIdlePowerUsage(4d);
                }
            }
            case markDirty -> markDirty();
        }

        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            AELog.error(e, "Couldn't wake up level emitter for delayed updates");
        }

        checkSlotsAccessible();
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
        this.fullRefund();

        if (cell.getStackInSlot(0) != null) drops.add(cell.getStackInSlot(0));
        final ItemStack container = AEApi.instance().definitions().items().itemMEStackPacket().maybeStack(1).get();

        for (int i = 0; i < 9; i++) {
            final FluidStack fs = this.invFluids.getFluidStackInSlot(i);
            if (fs == null) continue;
            final ItemStack temp = container.copy();
            Platform.writeStackNBT(AEFluidStack.create(fs), ItemStackNBT.get(temp));
            drops.add(temp);
        }

        for (int i = 0; i < 63; i++) {
            final ItemStack is = this.invItems.getStackInSlot(i);
            if (is == null) continue;
            final ItemStack temp = container.copy();
            Platform.writeStackNBT(AEItemStack.create(is), ItemStackNBT.get(temp));
            drops.add(temp);
        }
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
        return (this.isFullStockMode() && !this.isSlotsAccessible()) ? null : invFluids.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return (this.isFullStockMode() && !this.isSlotsAccessible()) ? null : invFluids.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return (this.isFullStockMode() && !this.isSlotsAccessible()) ? false : invFluids.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return (this.isFullStockMode() && !this.isSlotsAccessible()) ? new FluidTankInfo[0]
                : invFluids.getTankInfo(from);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        this.markDirty();
        checkSlotsAccessible();
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
        isFullStockMode = data.getBoolean("isFullStockMode");
        checkSlotsAccessible();
        isSlotsAccessible = data.getBoolean("isSlotsAccessible");
        totalBytes = data.getLong("totalBytes");
        getProxy().setIdlePowerUsage(data.getDouble("powerDraw"));
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        invItems.writeToNBT(data, "ItemInv");
        invFluids.writeToNBT(data, "FluidInv");
        configItems.writeToNBT(data, "configItems");
        configFluids.writeToNBT(data, "configFluids");
        cell.writeToNBT(data, "cellHolder");
        data.setBoolean("isFullStockMode", isFullStockMode);
        data.setBoolean("isSlotsAccessible", isSlotsAccessible);
        data.setLong("totalBytes", totalBytes);
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

    @Override
    public void saveAEStackInv() {
        this.markDirty();
    }

    @Override
    public IAEStackInventory getAEInventoryByName(StorageName name) {
        return switch (name) {
            case NONE -> this.configFluids;
            case CONFIG -> this.configItems;
            default -> null;
        };
    }

    @Override
    public Packet getDescriptionPacket() {

        NBTTagCompound tag = new NBTTagCompound();

        this.writeToNBT(tag);

        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {

        this.readFromNBT(pkt.func_148857_g());
    }
}
