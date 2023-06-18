package com.glodblock.github.crossmod.opencomputers;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.parts.IPartHost;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import li.cil.oc.server.network.Component;

public class DriverDualFluidInterface implements SidedBlock {

    @Override
    public boolean worksWith(World world, int x, int y, int z, ForgeDirection side) {
        TileEntity te = world.getTileEntity(x, y, z);
        return isDualFluidInterface(te, side);
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection side) {
        TileEntity te = world.getTileEntity(x, y, z);
        return new DriverDualFluidInterface.Environment(getInterface(te, side));
    }

    public static class Environment extends ManagedTileEntityEnvironment<DualityFluidInterface> implements NamedBlock {

        public Environment(DualityFluidInterface tileEntity) {
            super(tileEntity, NameConst.BLOCK_FLUID_INTERFACE);
        }

        @Callback(
                doc = "function(index:number):table -- Get the configuration of the dual fluid interface on the specified slot.")
        public Object[] getFluidInterfaceConfiguration(Context context, Arguments args) {
            int index = args.checkInteger(0);
            if (index >= 0 && index < DualityFluidInterface.NUMBER_OF_TANKS) {
                FluidStack fluid = tileEntity.getConfig().getFluidStackInSlot(index);
                return new Object[] { fluid == null ? null : new FluidStack(fluid.getFluid(), 1000) };
            }
            throw new IllegalArgumentException("invalid slot");
        }

        @Callback(
                doc = "function(index:number[, database:address, entry:number]):boolean -- Configure the filter in fluid interface on the specified slot.")
        public Object[] setFluidInterfaceConfiguration(Context context, Arguments args) {
            int index = args.checkInteger(0);
            String address;
            int entry;
            if (args.count() == 3) {
                address = args.checkString(1);
                entry = args.checkInteger(2);
            } else {
                tileEntity.getConfig().setFluidInSlot(index, null);
                context.pause(0.5);
                return new Object[] { true };
            }
            Node node = node().network().node(address);
            if (!(node instanceof Component)) throw new IllegalArgumentException("no such component");
            if (!(node.host() instanceof Database database)) throw new IllegalArgumentException("not a database");
            if (index >= 0 && index < DualityFluidInterface.NUMBER_OF_TANKS) {
                ItemStack data = database.getStackInSlot(entry - 1);
                if (data == null) {
                    tileEntity.getConfig().setFluidInSlot(index, null);
                } else {
                    FluidStack fluid = Util.getFluidFromItem(data);
                    tileEntity.getConfig().setFluidInSlot(index, tileEntity.getStandardFluid(fluid));
                }
                context.pause(0.5);
                return new Object[] { true };
            }
            throw new IllegalArgumentException("invalid slot");
        }

        @Override
        public String preferredName() {
            return NameConst.BLOCK_FLUID_INTERFACE;
        }

        @Override
        public int priority() {
            return 0;
        }
    }

    private static boolean isDualFluidInterface(TileEntity te, ForgeDirection face) {
        if (te != null) {
            if (te instanceof TileFluidInterface) {
                return true;
            }
            if (te instanceof IPartHost) {
                return ((IPartHost) te).getPart(face) instanceof PartFluidInterface;
            }
        }
        return false;
    }

    private static DualityFluidInterface getInterface(TileEntity te, ForgeDirection face) {
        if (te != null) {
            if (te instanceof TileFluidInterface) {
                return ((TileFluidInterface) te).getDualityFluid();
            }
            if (te instanceof IPartHost && ((IPartHost) te).getPart(face) instanceof PartFluidInterface) {
                return ((PartFluidInterface) ((IPartHost) te).getPart(face)).getDualityFluid();
            }
        }
        return null;
    }

    public static class Provider implements EnvironmentProvider {

        Provider() {}

        @Override
        public Class<?> getEnvironment(ItemStack itemStack) {
            if (itemStack != null && (itemStack.isItemEqual(ItemAndBlockHolder.FLUID_INTERFACE.stack())
                    || itemStack.isItemEqual(ItemAndBlockHolder.INTERFACE.stack()))) {
                return DriverDualFluidInterface.Environment.class;
            }
            return null;
        }
    }
}
