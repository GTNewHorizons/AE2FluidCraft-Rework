package com.glodblock.github.common.parts;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseAnnihilationPlane;
import appeng.util.Platform;

public class PartFluidAnnihilationPlane extends PartBaseAnnihilationPlane {

    public PartFluidAnnihilationPlane(ItemStack is) {
        super(is);
    }

    @Override
    public void onNeighborChanged() {
        final DimensionalCoord self = this.getLocation();
        final IMEMonitor<IAEFluidStack> monitor;
        final IEnergyGrid power;

        try {
            monitor = this.getProxy().getStorage().getFluidInventory();
            power = this.getProxy().getEnergy();
        } catch (GridAccessException ignored) {
            return;
        }

        if (monitor == null || power == null) return;
        final World world = this.tile.getWorldObj();
        final ForgeDirection side = getSide();
        final Block fluidBlock = world.getBlock(self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ);
        final int meta = world.getBlockMetadata(self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ);
        final BaseActionSource src = new MachineSource(this);

        if (fluidBlock instanceof IFluidBlock block) {
            final FluidStack drained = block
                    .drain(world, self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ, false);
            if (drained == null) return;
            final IAEFluidStack toInject = Util.FluidUtil.createAEFluidStack(drained);
            final IAEFluidStack notInjected = Platform
                    .poweredInsert(power, monitor, toInject, src, Actionable.SIMULATE);
            if (notInjected != null) return;
            Platform.poweredInsert(power, monitor, toInject, src, Actionable.MODULATE);
            block.drain(world, self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ, true);
        } else if (meta == 0) {
            if (fluidBlock == Blocks.flowing_water) {
                final IAEFluidStack toInject = Util.FluidUtil.createAEFluidStack(FluidRegistry.WATER);
                final IAEFluidStack notInjected = Platform
                        .poweredInsert(power, monitor, toInject, src, Actionable.SIMULATE);
                if (notInjected != null) return;
                Platform.poweredInsert(power, monitor, toInject, src, Actionable.MODULATE);
                world.setBlockToAir(self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ);
            } else if (fluidBlock == Blocks.flowing_lava) {
                final IAEFluidStack toInject = Util.FluidUtil.createAEFluidStack(FluidRegistry.LAVA);
                final IAEFluidStack notInjected = Platform
                        .poweredInsert(power, monitor, toInject, src, Actionable.SIMULATE);
                if (notInjected != null) return;
                Platform.poweredInsert(power, monitor, toInject, src, Actionable.MODULATE);
                world.setBlockToAir(self.x + side.offsetX, self.y + side.offsetY, self.z + side.offsetZ);
            }
        }
    }

    @Override
    public IIcon getActiveIcon() {
        return FCPartsTexture.PartFluidAnnihilationPlaneOn.getIcon();
    }
}
