package com.glodblock.github.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;

public class MEMonitorIFluidHandler implements IMEMonitor<IAEFluidStack> {

    private final IFluidHandler handler;
    private final ForgeDirection side;
    private IItemList<IAEFluidStack> cache = AEApi.instance().storage().createFluidList();
    private final HashMap<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
    private BaseActionSource mySource;
    private StorageFilter mode;

    public MEMonitorIFluidHandler(IFluidHandler handler, ForgeDirection side) {
        this.mode = StorageFilter.EXTRACTABLE_ONLY;
        this.handler = handler;
        this.side = side;
    }

    public MEMonitorIFluidHandler(IFluidHandler handler) {
        this.mode = StorageFilter.EXTRACTABLE_ONLY;
        this.handler = handler;
        this.side = ForgeDirection.UNKNOWN;
    }

    public void addListener(IMEMonitorHandlerReceiver<IAEFluidStack> l, Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    public void removeListener(IMEMonitorHandlerReceiver<IAEFluidStack> l) {
        this.listeners.remove(l);
    }

    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
        int filled = this.handler.fill(this.side, input.getFluidStack(), type == Actionable.MODULATE);

        if (type == Actionable.MODULATE) {
            this.onTick();
        }

        if ((long) filled == input.getStackSize()) {
            return null;
        }

        IAEFluidStack o = input.copy();
        o.setStackSize(input.getStackSize() - (long) filled);
        return o;
    }

    public IAEFluidStack extractItems(IAEFluidStack request, Actionable type, BaseActionSource src) {
        FluidStack removed = this.handler.drain(this.side, request.getFluidStack(), type == Actionable.MODULATE);
        if (removed != null && removed.amount != 0) {
            IAEFluidStack o = request.copy();
            o.setStackSize(removed.amount);
            if (type == Actionable.MODULATE) {
                IAEFluidStack cachedStack = this.cache.findPrecise(request);
                if (cachedStack != null) {
                    cachedStack.decStackSize(o.getStackSize());
                    this.postDifference(Collections.singletonList(o.copy().setStackSize(-o.getStackSize())));
                }
            }
            return o;
        } else {
            return null;
        }
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }

    // *Decompiled Stuff*//

    public TickRateModulation onTick() {
        boolean changed = false;
        List<IAEFluidStack> changes = new ArrayList<>();
        FluidTankInfo[] tankProperties = this.handler.getTankInfo(this.side);
        IItemList<IAEFluidStack> currentlyOnStorage = AEApi.instance().storage().createFluidList();

        if (tankProperties != null) {
            for (FluidTankInfo tankProperty : tankProperties) {
                if (this.mode != StorageFilter.EXTRACTABLE_ONLY || this.handler.drain(this.side, 1, false) != null) {
                    currentlyOnStorage.add(AEFluidStack.create(tankProperty.fluid));
                }
            }
        }

        Iterator<?> var9 = this.cache.iterator();

        IAEFluidStack is;
        while (var9.hasNext()) {
            is = (IAEFluidStack) var9.next();
            is.setStackSize(-is.getStackSize());
        }

        var9 = currentlyOnStorage.iterator();

        while (var9.hasNext()) {
            is = (IAEFluidStack) var9.next();
            this.cache.add(is);
        }

        var9 = this.cache.iterator();

        while (var9.hasNext()) {
            is = (IAEFluidStack) var9.next();
            if (is.getStackSize() != 0L) {
                changes.add(is);
            }
        }

        this.cache = currentlyOnStorage;
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            changed = true;
        }

        return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    private void postDifference(Iterable<IAEFluidStack> a) {
        if (a != null) {
            Iterator<?> i = this.listeners.entrySet().iterator();

            while (i.hasNext()) {
                Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = (Map.Entry) i.next();
                IMEMonitorHandlerReceiver key = l.getKey();
                if (key.isValid(l.getValue())) {
                    key.postChange(this, a, this.getActionSource());
                } else {
                    i.remove();
                }
            }
        }
    }

    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    public boolean isPrioritized(IAEFluidStack input) {
        return false;
    }

    public boolean canAccept(IAEFluidStack input) {
        return true;
    }

    public int getPriority() {
        return 0;
    }

    public int getSlot() {
        return 0;
    }

    public boolean validForPass(int i) {
        return true;
    }

    public IItemList<IAEFluidStack> getAvailableItems(IItemList out, int iteration) {

        for (IAEFluidStack fs : this.cache) {
            out.addStorage(fs);
        }

        return out;
    }

    @Override
    public IAEFluidStack getAvailableItem(@Nonnull IAEFluidStack request, int iteration) {
        return this.cache.findPrecise(request);
    }

    public IItemList<IAEFluidStack> getStorageList() {
        return this.cache;
    }

    private StorageFilter getMode() {
        return this.mode;
    }

    public void setMode(StorageFilter mode) {
        this.mode = mode;
    }

    private BaseActionSource getActionSource() {
        return this.mySource;
    }

    public void setActionSource(BaseActionSource mySource) {
        this.mySource = mySource;
    }
}
