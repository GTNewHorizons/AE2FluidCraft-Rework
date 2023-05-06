package com.glodblock.github.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.item.AEFluidStack;
import io.netty.buffer.ByteBuf;

public class TileFluidBuffer extends AENetworkTile implements IAEFluidInventory, IFluidHandler, IPowerChannelState {

    private final AEFluidInventory invFluids = new AEFluidInventory(this, 1, Integer.MAX_VALUE);
    private final BaseActionSource source;
    private boolean isPowered;

    public TileFluidBuffer() {
        getProxy().setIdlePowerUsage(2D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
    }

    public boolean setFluid(FluidStack fs) {
        if (fs == null) {
            this.invFluids.setFluidInSlot(0, null);
            return false;
        }
        try {
            IAEFluidStack ias = this.getProxy().getStorage().getFluidInventory().getStorageList()
                    .findPrecise(AEFluidStack.create(fs));
            this.invFluids.setFluidInSlot(0, ias);
            if (ias != null) return true;
        } catch (final GridAccessException e) {
            // :P
        }
        return false;
    }

    @Override
    public int fill(ForgeDirection forgeDirection, FluidStack fluidStack, boolean b) {
        IAEFluidStack ifs = this.getAEFluidStack();
        if (ifs != null && ifs.getFluid() == fluidStack.getFluid()) {
            try {
                IAEFluidStack notInserted = this.getProxy().getStorage().getFluidInventory().injectItems(
                        AEFluidStack.create(fluidStack),
                        b ? Actionable.MODULATE : Actionable.SIMULATE,
                        this.source);
                if (notInserted != null) return fluidStack.amount -= notInserted.getStackSize();
                return fluidStack.amount;
            } catch (final GridAccessException e) {
                // :P
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection forgeDirection, FluidStack fluidStack, boolean b) {
        FluidStack fs = this.getFluidStack();
        if (fs != null && fs.getFluid() == fluidStack.getFluid()) {
            return this.drainFluid(fluidStack, b ? Actionable.MODULATE : Actionable.SIMULATE);
        } else {
            return null;
        }
    }

    public FluidStack drainFluid(FluidStack fs, Actionable actionable) {
        if (fs == null || fs.amount <= 0) return null;
        try {
            IAEFluidStack ias = AEFluidStack.create(fs);
            IAEFluidStack extracted = this.getProxy().getStorage().getFluidInventory()
                    .extractItems(ias, actionable, this.source);
            if (extracted == null) return null;
            return extracted.getFluidStack();
        } catch (final GridAccessException e) {
            // :P
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection forgeDirection, int maxDrain, boolean b) {
        FluidStack fs = this.getFluidStack();
        if (maxDrain == 0 || fs == null) {
            return null;
        }
        FluidStack tmp = fs.copy();
        tmp.amount = maxDrain;
        return this.drainFluid(tmp, b ? Actionable.MODULATE : Actionable.SIMULATE);
    }

    public IAEFluidStack getAEStoreFluidStack() {
        try {
            return this.getProxy().getStorage().getFluidInventory().getStorageList()
                    .findPrecise(this.getAEFluidStack());
        } catch (final GridAccessException e) {
            // :P
        }
        return null;
    }

    public FluidStack getFluidStack() {
        return this.getInternalFluid().getFluidStackInSlot(0);
    }

    public IAEFluidStack getAEFluidStack() {
        return this.getInternalFluid().getFluidInSlot(0);
    }

    @Override
    public boolean canFill(ForgeDirection forgeDirection, Fluid fluid) {
        FluidStack fs = this.getFluidStack();
        return fs != null && fs.getFluid() == fluid;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return getInternalFluid().canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTankInfo[] FluidTankInfos = getInternalFluid().getTankInfo(from);
        FluidTankInfo[] tmp = FluidTankInfos.clone();
        for (FluidTankInfo ft : tmp) {
            if (ft.fluid == null) continue;
            ft.fluid.amount = 0;
        }
        return tmp;
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        invFluids.writeToNBT(data, "FluidInv");
        return data;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        invFluids.readFromNBT(data, "FluidInv");
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream(final ByteBuf data) {
        final boolean oldPower = this.isPowered;
        this.isPowered = data.readBoolean();
        return this.isPowered != oldPower;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream(final ByteBuf data) {
        data.writeBoolean(this.isActive());
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        this.updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        this.updatePowerState();
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
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        markForUpdate();
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return this.invFluids;
    }

    public void updateFluidStore() {
        IAEFluidStack iaf = this.getAEStoreFluidStack();
        if (iaf != null) {
            this.invFluids.setFluidInSlot(0, iaf);
        }
    }
}
