package com.glodblock.github.coremod.registries.adapters;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.api.registries.ILevelViewableAdapter;
import com.glodblock.github.api.registries.LevelItemInfo;
import com.glodblock.github.api.registries.LevelState;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;

public class PartFluidLevelEmitterAdapter implements ILevelViewable, ILevelViewableAdapter {

    private PartFluidLevelEmitter delegate;

    public PartFluidLevelEmitterAdapter() {}

    public ILevelViewable adapt(IGridHost gridHost) {
        if (gridHost instanceof PartFluidLevelEmitter levelEmitter) this.delegate = levelEmitter;
        return this;
    };

    @Override
    public DimensionalCoord getLocation() {
        return delegate.getLocation();
    }

    @Override
    public TileEntity getTile() {
        return delegate.getTile();
    }

    @Override
    public ForgeDirection getSide() {
        return delegate.getSide();
    }

    @Override
    public IInventory getInventoryByName(String name) {
        return delegate.getInventoryByName(name);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return delegate.getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return delegate.getCableConnectionType(dir);
    }

    @Override
    public void securityBreak() {
        delegate.securityBreak();
    }

    @Override
    public String getCustomName() {
        return delegate.getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        return delegate.hasCustomName();
    }

    @Override
    public void setCustomName(String name) {
        delegate.hasCustomName();
    }

    @Override
    public LevelItemInfo[] getLevelItemInfoList() {
        ItemStack stack = delegate.getInventoryByName("config").getStackInSlot(0);
        if (stack == null) return new LevelItemInfo[] { null };
        return new LevelItemInfo[] { new LevelItemInfo(
                stack,
                delegate.getReportingValue(),
                -1,
                delegate.isProvidingStrongPower() > 0 ? LevelState.Craft : LevelState.Idle) };
    }
}
