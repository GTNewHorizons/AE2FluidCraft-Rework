package com.glodblock.github.inventory;

import org.jetbrains.annotations.NotNull;

import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.CraftingGridCache;

public class CraftingGridCacheFluidInventoryProxyCell implements IMEInventoryHandler<IAEFluidStack> {

    private final CraftingGridCache inner;

    public CraftingGridCacheFluidInventoryProxyCell(final CraftingGridCache craftingGridCache) {
        inner = craftingGridCache;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
        return ItemFluidDrop
                .getAeFluidStack((IAEItemStack) inner.injectItems(ItemFluidDrop.newAeStack(input), type, src));
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        return null;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out, int iteration) {
        return out;
    }

    @Override
    public IAEFluidStack getAvailableItem(@NotNull IAEFluidStack request, int iteration) {
        return null;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }

    @Override
    public AccessRestriction getAccess() {
        return inner.getAccess();
    }

    @Override
    public boolean isPrioritized(IAEFluidStack input) {
        return inner.isPrioritized(input);
    }

    @Override
    public boolean canAccept(IAEFluidStack input) {
        return inner.canAccept(ItemFluidDrop.newAeStack(input));
    }

    @Override
    public int getPriority() {
        return inner.getPriority();
    }

    @Override
    public int getSlot() {
        return inner.getSlot();
    }

    @Override
    public boolean validForPass(int i) {
        return inner.validForPass(i);
    }
}
