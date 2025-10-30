package com.glodblock.github.common.tile;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.loader.ChannelLoader;
import com.glodblock.github.util.BlockPos;

public class TileCertusQuartzTank extends TileEntity implements IFluidHandler {

    private FluidStack lastBeforeUpdate = null;
    public FluidTank tank = new FluidTank(32000);
    private boolean hasUpdate;

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return this.tank.getFluid() == null || this.tank.getFluid().getFluid() == fluid;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return this.tank.getFluid() == null || this.tank.getFluid().getFluid() == fluid;
    }

    public int getFluidLightLevel() {
        FluidStack tankFluid = tank.getFluid();
        return tankFluid == null || tankFluid.amount == 0 ? 0 : tankFluid.getFluid().getLuminosity(tankFluid);
    }

    public void compareAndUpdate() {
        if (this.worldObj.isRemote) {
            return;
        }

        FluidStack current = this.tank.getFluid();

        if (current == null || this.lastBeforeUpdate == null) {
            if (current != this.lastBeforeUpdate) {
                ChannelLoader.sendPacketToAllPlayers(getDescriptionPacket(), this.worldObj);
                this.lastBeforeUpdate = current == null ? null : current.copy();
            }
            return;
        }

        boolean amountSufficientlyChanged = Math.abs(current.amount - this.lastBeforeUpdate.amount) >= 500;
        boolean isFull = current.amount == this.tank.getCapacity();
        boolean wasFull = this.lastBeforeUpdate.amount == this.tank.getCapacity();
        boolean fullStateChanged = isFull != wasFull;

        if (amountSufficientlyChanged || fullStateChanged) {
            ChannelLoader.sendPacketToAllPlayers(getDescriptionPacket(), this.worldObj);
            this.lastBeforeUpdate = current.copy();
        }
    }

    public FluidStack drain(FluidStack fluid, boolean doDrain, boolean findMainTank) {
        if (fluid == null || this.getFluid() == null || this.getFluid() != fluid.getFluid()) {
            return null;
        }

        if (findMainTank) {
            // Main tank is the highest tank in the column with desired fluid
            TileCertusQuartzTank mainTank = this;
            TileCertusQuartzTank tankAbove;

            while ((tankAbove = mainTank.getTankAbove()) != null) {
                // We can't drain tank with different or null fluid
                if (fluid.getFluid() != tankAbove.getFluid()) {
                    break;
                }
                mainTank = tankAbove;
            }

            return mainTank.drain(fluid, doDrain, false);
        }

        FluidStack drainedFluid = this.tank.drain(fluid.amount, doDrain);
        int drained = drainedFluid != null ? drainedFluid.amount : 0;

        compareAndUpdate();

        if (drained < fluid.amount) {
            TileCertusQuartzTank tankBelow = this.getTankBelow();
            if (tankBelow != null) {
                FluidStack fluidToDrain = new FluidStack(fluid.getFluid(), fluid.amount - drained);
                FluidStack externallyDrained = tankBelow.drain(fluidToDrain, doDrain, false);

                if (externallyDrained != null) {
                    return new FluidStack(fluid.getFluid(), drained + externallyDrained.amount);
                }
            }
        }

        return drainedFluid;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return drain(resource, doDrain, true);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (this.tank.getFluid() == null) return null;

        return drain(from, new FluidStack(this.tank.getFluid(), maxDrain), doDrain);
    }

    public int fill(FluidStack fluid, boolean doFill, boolean findMainTank) {
        if (fluid == null || this.getFluid() != null && this.getFluid() != fluid.getFluid()) {
            return 0;
        }

        if (findMainTank) {
            // Main tank is the lowest tank in the column with desired fluid
            TileCertusQuartzTank mainTank = this;
            TileCertusQuartzTank tankBelow;

            while ((tankBelow = mainTank.getTankBelow()) != null) {
                FluidStack fluidBelow = tankBelow.tank.getFluid();
                // We can't fill already full tank
                if (fluidBelow != null && fluidBelow.amount == tankBelow.tank.getCapacity()) {
                    break;
                }
                // We can't fill tank with different fluid
                if (tankBelow.getFluid() != null && fluid.getFluid() != tankBelow.getFluid()) {
                    break;
                }
                mainTank = tankBelow;
            }

            return mainTank.fill(fluid, doFill, false);
        }

        int filled = this.tank.fill(fluid, doFill);
        compareAndUpdate();

        if (filled < fluid.amount) {
            TileCertusQuartzTank tankAbove = this.getTankAbove();
            if (tankAbove != null) {
                FluidStack fluidToFill = new FluidStack(fluid.getFluid(), fluid.amount - filled);
                return filled + tankAbove.fill(fluidToFill, doFill, false);
            }
        }

        return filled;
    }

    /**
     * An override for {@link IFluidHandler}
     */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return fill(resource, doFill, true);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    public Fluid getFluid() {
        FluidStack tankFluid = this.tank.getFluid();
        return tankFluid != null && tankFluid.amount > 0 ? tankFluid.getFluid() : null;
    }

    public Fluid getRenderFluid() {
        return this.tank.getFluid() != null ? this.tank.getFluid().getFluid() : null;
    }

    public float getRenderScale() {
        return (float) this.tank.getFluidAmount() / this.tank.getCapacity();
    }

    private void onComparatorUpdate(World world, int x, int y, int z, Block block) {
        world.func_147453_f(x, y, z, block);
    }

    @Override
    public void updateEntity() {
        if (this.tank.getFluid() != null) {
            TileCertusQuartzTank below = getTankBelow();
            if (below != null) {
                FluidStack filled = this.tank.getFluid().copy();
                filled.amount = below.fill(this.tank.getFluid(), true, true);
                if (filled.amount >= 0) {
                    this.drain(filled, true, true);
                    this.hasUpdate = true;
                    below.hasUpdate = true;
                }
            }
        }

        if (this.hasUpdate) {
            onComparatorUpdate(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            this.hasUpdate = false;
        }
    }

    private TileCertusQuartzTank getTankAbove() {
        TileEntity tile = new BlockPos(this).getOffSet(0, 1, 0).getTileEntity();
        if (tile instanceof TileCertusQuartzTank tankTile) {
            return tankTile;
        }
        return null;
    }

    private TileCertusQuartzTank getTankBelow() {
        TileEntity tile = new BlockPos(this).getOffSet(0, -1, 0).getTileEntity();
        if (tile instanceof TileCertusQuartzTank tankTile) {
            return tankTile;
        }
        return null;
    }

    public FluidTankInfo[] getTankInfo(boolean countAllTanksInColumn) {
        if (!countAllTanksInColumn) {
            return new FluidTankInfo[] { this.tank.getInfo() };
        }

        final FluidStack tankFluidStack = this.tank.getFluid();
        final Fluid mainFluid = tankFluidStack != null ? tankFluidStack.getFluid() : null;

        int amount = tankFluidStack == null ? 0 : tankFluidStack.amount;
        int capacity = this.tank.getCapacity();

        TileCertusQuartzTank tankAbove = this;
        while ((tankAbove = tankAbove.getTankAbove()) != null) {
            if (tankAbove.getFluid() == null || mainFluid != tankAbove.getFluid()) {
                break;
            }

            FluidTankInfo info = tankAbove.tank.getInfo();
            amount += info.fluid == null ? 0 : info.fluid.amount;
            capacity += info.capacity;
        }

        TileCertusQuartzTank tankBelow = this;
        while ((tankBelow = tankBelow.getTankBelow()) != null) {
            if (mainFluid == null || mainFluid != tankBelow.getFluid()) {
                break;
            }

            FluidTankInfo info = tankBelow.tank.getInfo();
            amount += info.fluid == null ? 0 : info.fluid.amount;
            capacity += info.capacity;
        }

        return new FluidTankInfo[] {
                new FluidTankInfo(mainFluid != null ? new FluidStack(mainFluid, amount) : null, capacity) };
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return getTankInfo(true);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        this.worldObj.markBlockRangeForRenderUpdate(
                this.xCoord,
                this.yCoord,
                this.zCoord,
                this.xCoord,
                this.yCoord,
                this.zCoord);
        readFromNBT(packet.func_148857_g());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readFromNBTWithoutCoords(tag);
    }

    public void readFromNBTWithoutCoords(NBTTagCompound tag) {
        this.tank.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeToNBTWithoutCoords(tag);
    }

    public void writeToNBTWithoutCoords(NBTTagCompound tag) {
        this.tank.writeToNBT(tag);
    }

    public FluidTankInfo[] getInternalFluid() {
        return this.getTankInfo(true);
    }
}
