package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import com.glodblock.github.client.textures.FCPartsTexture;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseFormationPlane;
import appeng.util.Platform;

public class PartFluidFormationPlane extends PartBaseFormationPlane implements IGridTickable {

    public PartFluidFormationPlane(ItemStack is) {
        super(is);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(2, 120, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode iGridNode, int ing) {
        return this.doWork() ? TickRateModulation.SLEEP : TickRateModulation.SAME;

    }

    @Override
    public void onNeighborChanged() {
        final TileEntity te = this.getHost().getTile();
        final World w = te.getWorldObj();
        if (w == null) return;
        final ForgeDirection side = this.getSide();

        final int x = te.xCoord + side.offsetX;
        final int y = te.yCoord + side.offsetY;
        final int z = te.zCoord + side.offsetZ;

        final Block worldBlock = w.getBlock(x, y, z);
        this.blocked = worldBlock != null && worldBlock != Blocks.air;

        if (!this.blocked) {
            try {
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
            } catch (GridAccessException ignored) {}
        }
    }

    @Override
    protected void updateHandler() {
        this.onNeighborChanged();
    }

    public boolean doWork() {
        if (this.blocked) return true;

        final DimensionalCoord self = this.getLocation();
        final int x = self.x + side.offsetX;
        final int y = self.y + side.offsetY;
        final int z = self.z + side.offsetZ;

        final World world = this.tile.getWorldObj();
        final Block worldBlock = world.getBlock(x, y, z);
        if (worldBlock != null && worldBlock != Blocks.air) this.blocked = true;

        final IMEMonitor<IAEFluidStack> monitor;
        final IEnergyGrid power;

        try {
            monitor = this.getProxy().getStorage().getFluidInventory();
            power = this.getProxy().getEnergy();
        } catch (GridAccessException ignored) {
            return true;
        }

        if (monitor == null || power == null) return true;

        final BaseActionSource src = new MachineSource(this);

        boolean stockOut = false;

        for (int i = 0; i < this.Config.getSizeInventory(); i++) {
            if (!(this.Config.getAEStackInSlot(i) instanceof IAEFluidStack ifs)) continue;
            final IAEFluidStack fluid = ifs.copy().setStackSize(FluidContainerRegistry.BUCKET_VOLUME);
            final IAEFluidStack canDrain = Platform.poweredExtraction(power, monitor, fluid, src, Actionable.SIMULATE);
            if (canDrain == null || canDrain.getStackSize() < FluidContainerRegistry.BUCKET_VOLUME) {
                stockOut = true;
                continue;
            }
            Platform.poweredExtraction(power, monitor, fluid, src, Actionable.MODULATE);
            final Block fluidWorldBlock = fluid.getFluid().getBlock();
            world.setBlock(x, y, z, fluidWorldBlock);
            world.markBlockForUpdate(x, y, z);
            return true;
        }

        return !stockOut;
    }

    @Override
    public IAEStackType<?> getStackType() {
        return FLUID_STACK_TYPE;
    }

    @Override
    public boolean supportItemDrop() {
        return false;
    }

    @Override
    public boolean supportFuzzy() {
        return false;
    }

    @Override
    public IIcon getActiveIcon() {
        return FCPartsTexture.PartFluidFormationPlaneOn.getIcon();
    }
}
