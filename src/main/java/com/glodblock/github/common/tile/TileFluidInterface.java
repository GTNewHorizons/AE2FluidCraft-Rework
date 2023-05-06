package com.glodblock.github.common.tile;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.Util;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class TileFluidInterface extends TileInterface implements IDualHost {

    private final IConfigManager dualityConfigManager = getInterfaceDuality().getConfigManager();

    private final DualityFluidInterface fluidDuality = new DualityFluidInterface(this.getProxy(), this) {

        @Override
        public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
            SidelessMode mode = (SidelessMode) dualityConfigManager.getSetting(Settings.SIDELESS_MODE);
            if (mode == SidelessMode.SIDELESS) {
                return this.getTanks().drain(from, maxDrain, doDrain);
            }
            return this.getTanks().drain(from.ordinal(), maxDrain, doDrain);
        }
    };

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 6);

    public TileFluidInterface() {
        super.getInterfaceDuality().getConfigManager().registerSetting(Settings.SIDELESS_MODE, SidelessMode.SIDELESS);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        fluidDuality.onChannelStateChange(c);
        super.stateChange(c);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        fluidDuality.onPowerStateChange(c);
        super.stateChange(c);
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        fluidDuality.gridChanged();
    }

    @Override
    public DualityFluidInterface getDualityFluid() {
        return fluidDuality;
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return fluidDuality.getTanks();
    }

    @Override
    public AppEngInternalAEInventory getConfig() {
        Util.mirrorFluidToPacket(this.config, fluidDuality.getConfig());
        return config;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        TickingRequest item = super.getTickingRequest(node);
        TickingRequest fluid = fluidDuality.getTickingRequest(node);
        return new TickingRequest(
                Math.min(item.minTickRate, fluid.minTickRate),
                Math.max(item.maxTickRate, fluid.maxTickRate),
                item.isSleeping && fluid.isSleeping,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        TickRateModulation item = super.tickingRequest(node, ticksSinceLastCall);
        TickRateModulation fluid = fluidDuality.tickingRequest(node, ticksSinceLastCall);
        if (item.ordinal() >= fluid.ordinal()) {
            return item;
        } else {
            return fluid;
        }
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    protected void writeToStream(ByteBuf data) throws IOException {
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
        }
        getInternalFluid().writeToBuf(data);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = false;
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, config.getStackInSlot(i))) {
                config.setInventorySlotContents(i, stack);
                changed = true;
            }
        }
        fluidDuality.loadConfigFromPacket(this.config);
        changed |= getInternalFluid().readFromBuf(data);
        return changed;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        config.readFromNBT(data, "ConfigInv");
        fluidDuality.loadConfigFromPacket(this.config);
        getInternalFluid().readFromNBT(data, "FluidInv");
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        config.writeToNBT(data, "ConfigInv");
        getInternalFluid().writeToNBT(data, "FluidInv");
        return data;
    }

    @Nullable
    protected ItemStack getItemFromTile(final Object obj) {
        if (obj instanceof TileFluidInterface) {
            return ItemAndBlockHolder.INTERFACE.stack();
        }
        return null;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return getInterfaceDuality().getInstalledUpgrades(u);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return fluidDuality.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return fluidDuality.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return fluidDuality.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluidDuality.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return fluidDuality.getTankInfo(from);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        markForUpdate();
        fluidDuality.onFluidInventoryChanged(inv, slot);
    }

    @Override
    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(
                    id,
                    ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
            fluidDuality.getConfig().setFluidInSlot(id, fluidDuality.getStandardFluid(fluid));
        }
    }

    @Override
    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            getInternalFluid().setFluidInSlot(id, fluid);
        }
    }
}
