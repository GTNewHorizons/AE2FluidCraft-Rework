package com.glodblock.github.common.storage;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.item.AEFluidStack;
import appeng.util.prioitylist.PrecisePriorityList;

public class FluidCellInventoryHandler extends MEInventoryHandler<IAEFluidStack>
        implements IFluidCellInventoryHandler, ICellCacheRegistry {

    protected FluidCellInventoryHandler(final IMEInventory<IAEFluidStack> c) {
        super(c, StorageChannel.FLUIDS);

        final IFluidCellInventory ci = this.getCellInv();

        if (ci != null) {
            final IInventory config = ci.getConfigInventory();

            final IItemList<IAEFluidStack> priorityList = AEApi.instance().storage().createFluidList();
            for (int x = 0; x < config.getSizeInventory(); x++) {
                final ItemStack is = config.getStackInSlot(x);
                final FluidStack fluid = Util.getFluidFromItem(is);
                if (fluid != null) {
                    AEFluidStack stack = AEFluidStack.create(fluid);
                    stack.setStackSize(1);
                    priorityList.add(stack);

                    // convert partition on cells in network
                    if (!(is.getItem() instanceof ItemFluidPacket)) {
                        config.setInventorySlotContents(x, ItemFluidPacket.newDisplayStack(fluid));
                        config.markDirty();
                    }
                }
            }
            if (!priorityList.isEmpty()) {
                this.setPartitionList(new PrecisePriorityList<>(priorityList));
            }

            final IInventory upgrades = ci.getUpgradesInventory();
            boolean hasSticky = false;

            for (int x = 0; x < upgrades.getSizeInventory(); x++) {
                final ItemStack is = upgrades.getStackInSlot(x);
                if (is != null && is.getItem() instanceof IUpgradeModule) {
                    final Upgrades u = ((IUpgradeModule) is.getItem()).getType(is);
                    if (u == Upgrades.STICKY) {
                        hasSticky = true;
                        break;
                    }
                }
            }

            if (hasSticky) {
                setSticky(true);
            }
        }
    }

    @Override
    public IFluidCellInventory getCellInv() {
        Object o = this.getInternal();
        if (o instanceof MEPassThrough) {
            o = Ae2Reflect.getInternal((MEPassThrough<?>) o);
        }
        return (IFluidCellInventory) (o instanceof IFluidCellInventory ? o : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<IAEFluidStack> getPartitionInv() {
        return (Iterable<IAEFluidStack>) Ae2Reflect.getPartitionList(this).getItems();
    }

    @Override
    public boolean isPreformatted() {
        return !Ae2Reflect.getPartitionList(this).isEmpty();
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return IncludeExclude.WHITELIST;
    }

    public int getStatusForCell() {
        int val = this.getCellInv().getStatusForCell();

        if ((val == 1 || val == 2) && this.isPreformatted()) {
            val = 3;
        }

        return val;
    }

    @Override
    public boolean canGetInv() {
        IFluidCellInventory cellInv = this.getCellInv();
        if (cellInv instanceof CreativeFluidCellInventory) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public long getTotalBytes() {
        return this.getCellInv().getTotalBytes();
    }

    @Override
    public long getFreeBytes() {
        return this.getCellInv().getFreeBytes();
    }

    @Override
    public long getUsedBytes() {
        return this.getCellInv().getUsedBytes();
    }

    @Override
    public long getTotalTypes() {
        return this.getCellInv().getTotalFluidTypes();
    }

    @Override
    public long getFreeTypes() {
        return this.getCellInv().getRemainingFluidTypes();
    }

    @Override
    public long getUsedTypes() {
        return this.getCellInv().getStoredFluidTypes();
    }

    @Override
    public int getCellStatus() {
        return this.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.FLUID;
    }

    public List<Object> getRestricted() {
        IFluidCellInventory cellInventory = this.getCellInv();
        if (cellInventory instanceof FluidCellInventory ci) {
            return ci.getRestriction();

        }
        return null;
    }
}
