package com.glodblock.github.common.storage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemBaseInfinityStorageCell;
import com.glodblock.github.common.item.ItemFluidVoidStorageCell;
import com.glodblock.github.crossmod.extracells.storage.ProxyFluidCellInventory;
import com.glodblock.github.crossmod.extracells.storage.ProxyFluidStorageCell;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

public class FluidCellInventory implements IFluidCellInventory {

    protected static final String FLUID_TYPE_TAG = "ft";
    protected static final String FLUID_COUNT_TAG = "fc";
    protected static final String FLUID_SLOT = "#";
    protected static final String FLUID_SLOT_COUNT = "@";
    protected IStorageFluidCell cellType;
    protected static String[] fluidSlots;
    protected static String[] fluidSlotCount;
    protected final ItemStack cellItem;
    private final ISaveProvider container;
    private final int MAX_TYPE = 63;
    protected long storedFluidCount;
    protected short storedFluids;
    protected IItemList<IAEFluidStack> cellFluids;
    protected final NBTTagCompound tagCompound;
    public static final int singleByteAmount = 256 * 8;

    public FluidCellInventory(final ItemStack o, final ISaveProvider container) throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        this.cellType = null;
        this.cellItem = o;
        final Item type = this.cellItem.getItem();
        if (type instanceof IStorageFluidCell) {
            this.cellType = (IStorageFluidCell) this.cellItem.getItem();
        }
        if (this.cellType == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        if (!this.cellType.isStorageCell(this.cellItem)) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }

        if (fluidSlots == null) {
            fluidSlots = new String[MAX_TYPE];
            fluidSlotCount = new String[MAX_TYPE];

            for (int x = 0; x < MAX_TYPE; x++) {
                fluidSlots[x] = FLUID_SLOT + x;
                fluidSlotCount[x] = FLUID_SLOT_COUNT + x;
            }
        }

        this.container = container;
        this.tagCompound = Platform.openNbtData(o);
        this.storedFluids = this.tagCompound.getShort(FLUID_TYPE_TAG);
        this.storedFluidCount = this.tagCompound.getLong(FLUID_COUNT_TAG);
        this.cellFluids = null;
    }

    public static IMEInventoryHandler<IAEFluidStack> getCell(final ItemStack o, final ISaveProvider container2) {
        try {
            if (o.getItem() instanceof ItemBaseInfinityStorageCell) {
                return new FluidCellInventoryHandler(new CreativeFluidCellInventory(o, container2));
            } else if (o.getItem() instanceof ProxyFluidStorageCell) {
                return new FluidCellInventoryHandler(new ProxyFluidCellInventory(o, container2));
            } else if (o.getItem() instanceof ItemFluidVoidStorageCell) {
                return new FluidCellInventoryHandler(new FluidVoidStorageCellInventory(o, container2));
            } else {
                return new FluidCellInventoryHandler(new FluidCellInventory(o, container2));
            }
        } catch (final AppEngException e) {
            return null;
        }
    }

    public static boolean isCell(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        final Item type = itemStack.getItem();
        if (type instanceof IStorageFluidCell) {
            return ((IStorageFluidCell) type).isStorageCell(itemStack);
        }
        return false;
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return this.cellType.getIdleDrain(is);
    }

    @Override
    public IInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.cellItem);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.cellItem);
    }

    @Override
    public boolean canHoldNewFluid() {
        final long bytesFree = this.getFreeBytes();
        return (bytesFree > this.getBytesPerType()
                || (bytesFree == this.getBytesPerType() && this.getUnusedFluidCount() > 0))
                && this.getRemainingFluidTypes() > 0;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getUsedBytes() {
        final long bytesForFluidCount = (this.getStoredFluidCount() + this.getUnusedFluidCount()) / singleByteAmount;
        return this.getStoredFluidTypes() * this.getBytesPerType() + bytesForFluidCount;
    }

    @Override
    public long getTotalFluidTypes() {
        return this.cellType.getTotalTypes(this.cellItem);
    }

    @Override
    public long getStoredFluidCount() {
        return this.storedFluidCount;
    }

    @Override
    public long getStoredFluidTypes() {
        return this.storedFluids;
    }

    @Override
    public long getRemainingFluidTypes() {
        final long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
        final long baseOnTotal = this.getTotalFluidTypes() - this.getStoredFluidTypes();
        return Math.min(basedOnStorage, baseOnTotal);
    }

    @Override
    public long getRemainingFluidCount() {
        final long remaining = this.getFreeBytes() * singleByteAmount + this.getUnusedFluidCount();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    public int getUnusedFluidCount() {
        final int div = (int) (this.getStoredFluidCount() % singleByteAmount);
        if (div == 0) {
            return 0;
        }
        return singleByteAmount - div;
    }

    @Override
    public int getStatusForCell() {
        if (this.getUsedBytes() == 0) {
            return 1;
        }
        if (this.canHoldNewFluid()) {
            return 2;
        }
        if (this.getRemainingFluidCount() > 0) {
            return 3;
        }
        return 4;
    }

    protected void loadCellFluids() {
        if (this.cellFluids == null) {
            this.cellFluids = AEApi.instance().storage().createFluidList();
        }
        this.cellFluids.resetStatus(); // clears totals and stuff.
        final int types = (int) this.getStoredFluidTypes();
        for (int x = 0; x < types; x++) {
            final FluidStack t = FluidStack.loadFluidStackFromNBT(this.tagCompound.getCompoundTag(fluidSlots[x]));
            final AEFluidStack aet = AEFluidStack.create(t);
            if (aet != null) {
                aet.setStackSize(this.tagCompound.getLong(fluidSlotCount[x]));
                if (aet.getStackSize() > 0) {
                    this.cellFluids.add(aet);
                }
            }
        }
    }

    protected IItemList<IAEFluidStack> getCellFluids() {
        if (this.cellFluids == null) {
            this.loadCellFluids();
        }
        return this.cellFluids;
    }

    private void updateFluidCount(final long delta) {
        this.storedFluidCount += delta;
        this.tagCompound.setLong(FLUID_COUNT_TAG, this.storedFluidCount);
    }

    private void saveChanges() {
        long fluidCount = 0;
        int x = 0;

        for (final IAEFluidStack v : this.cellFluids) {
            fluidCount += v.getStackSize();
            final NBTBase c = this.tagCompound.getTag(fluidSlots[x]);
            if (c instanceof NBTTagCompound) {
                v.writeToNBT((NBTTagCompound) c);
            } else {
                final NBTTagCompound g = new NBTTagCompound();
                v.writeToNBT(g);
                this.tagCompound.setTag(fluidSlots[x], g);
            }
            this.tagCompound.setLong(fluidSlotCount[x], v.getStackSize());
            x++;
        }

        final short oldStoredFluids = this.storedFluids;
        this.storedFluids = (short) this.cellFluids.size();

        if (this.cellFluids.isEmpty()) {
            this.tagCompound.removeTag(FLUID_TYPE_TAG);
        } else {
            this.tagCompound.setShort(FLUID_TYPE_TAG, this.storedFluids);
        }

        this.storedFluidCount = fluidCount;

        if (fluidCount == 0) {
            this.tagCompound.removeTag(FLUID_COUNT_TAG);
        } else {
            this.tagCompound.setLong(FLUID_COUNT_TAG, fluidCount);
        }

        // clean any old crusty stuff...
        for (; x < oldStoredFluids && x < MAX_TYPE; x++) {
            this.tagCompound.removeTag(fluidSlots[x]);
            this.tagCompound.removeTag(fluidSlotCount[x]);
        }

        if (this.container != null) {
            this.container.saveChanges(this);
        }
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        final IAEFluidStack l = this.getCellFluids().findPrecise(input);

        if (l != null) {
            final long remainingFluidSlots = this.getRemainingFluidCount();

            if (remainingFluidSlots < 0) {
                return input;
            }

            if (input.getStackSize() > remainingFluidSlots) {
                final IAEFluidStack r = input.copy();
                r.setStackSize(r.getStackSize() - remainingFluidSlots);
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + remainingFluidSlots);
                    this.updateFluidCount(remainingFluidSlots);
                    this.saveChanges();
                }
                return r;
            } else {
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + input.getStackSize());
                    this.updateFluidCount(input.getStackSize());
                    this.saveChanges();
                }
                return null;
            }
        }

        if (this.canHoldNewFluid()) // room for new type, and for at least one item!
        {
            final long remainingFluidCount = this.getRemainingFluidCount()
                    - ((long) this.getBytesPerType() * singleByteAmount);

            if (remainingFluidCount > 0) {
                if (input.getStackSize() > remainingFluidCount) {
                    final IAEFluidStack toReturn = input.copy();
                    toReturn.decStackSize(remainingFluidCount);
                    if (mode == Actionable.MODULATE) {
                        IAEFluidStack toWrite = input.copy();
                        toWrite.setStackSize(remainingFluidCount);
                        this.cellFluids.add(toWrite);
                        this.updateFluidCount(toWrite.getStackSize());
                        this.saveChanges();
                    }
                    return toReturn;
                }
                if (mode == Actionable.MODULATE) {
                    this.updateFluidCount(input.getStackSize());
                    this.cellFluids.add(input);
                    this.saveChanges();
                }
                return null;
            }
        }

        return input;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }

        final long size = request.getStackSize();

        IAEFluidStack results = null;

        final IAEFluidStack l = this.getCellFluids().findPrecise(request);

        if (l != null) {
            results = l.copy();

            if (l.getStackSize() <= size) {
                results.setStackSize(l.getStackSize());

                if (mode == Actionable.MODULATE) {
                    this.updateFluidCount(-l.getStackSize());
                    l.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                results.setStackSize(size);

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    this.updateFluidCount(-size);
                    this.saveChanges();
                }
            }
        }

        return results;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out, int iteration) {
        for (final IAEFluidStack i : this.getCellFluids()) {
            out.add(i);
        }
        return out;
    }

    @Override
    public IAEFluidStack getAvailableItem(@Nonnull IAEFluidStack request, int iteration) {
        return this.getCellFluids().findPrecise(request);
    }

    @Override
    public List<IAEFluidStack> getContents() {
        List<IAEFluidStack> ret = new ArrayList<>();
        for (IAEFluidStack fluid : this.getCellFluids()) {
            ret.add(fluid);
        }
        return ret;
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }
}
