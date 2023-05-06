package com.glodblock.github.crossmod.opencomputers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;

import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;

public class DriverLevelMaintainer extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileLevelMaintainer.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection forgeDirection) {
        return new DriverLevelMaintainer.Environment((TileLevelMaintainer) world.getTileEntity(x, y, z));
    }

    public static class Environment extends ManagedTileEntityEnvironment<TileLevelMaintainer> implements NamedBlock {

        public Environment(TileLevelMaintainer tileEntity) {
            super(tileEntity, NameConst.BLOCK_LEVEL_MAINTAINER);
        }

        private int getSlot(Arguments args, IInventory inv, int index, int def) {
            if (index >= 0 && index < args.count()) {
                int slot = args.checkInteger(index) - 1;
                if (slot < 0 || slot >= inv.getSizeInventory()) {
                    throw new IllegalArgumentException("Invalid slot");
                }
                return slot;
            } else {
                return def;
            }
        }

        private boolean isDone(Arguments args) {
            int slot = args.checkInteger(0) - 1;
            if (slot < 0 || slot >= TileLevelMaintainer.REQ_COUNT) {
                throw new IllegalArgumentException("Invalid slot");
            }
            return tileEntity.requests.isDone(slot);
        }

        private boolean isEnable(Arguments args) {
            int slot = args.checkInteger(0) - 1;
            if (slot < 0 || slot >= TileLevelMaintainer.REQ_COUNT) {
                throw new IllegalArgumentException("Invalid slot");
            }
            return tileEntity.requests.isEnable(slot);
        }

        private boolean setEnable(Arguments args) {
            if (args.count() != 2) {
                throw new IllegalArgumentException("Invalid args!");
            } else {
                int slot = args.checkInteger(0) - 1;
                if (slot < 0 || slot >= TileLevelMaintainer.REQ_COUNT) {
                    throw new IllegalArgumentException("Invalid slot");
                }
                boolean enable = args.checkBoolean(1);
                tileEntity.requests.setEnable(slot, enable);
                return true;
            }
        }

        private void updateOrder(int slot, int quantity, int batch) {
            tileEntity.updateQuantity(slot, quantity);
            tileEntity.updateBatchSize(slot, batch);
        }

        @Callback(doc = "function([slot:number]):table -- Get the slot status.")
        public Object[] getSlot(Context context, Arguments args) {
            IInventory config = tileEntity.getInventory();
            int slot = getSlot(args, config, 0, 0);
            ItemStack stack = config.getStackInSlot(slot);
            if (stack != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("damage", stack.getItemDamage());
                result.put("hasTag", stack.hasTagCompound());
                result.put("label", stack.getDisplayName());
                result.put("maxDamage", stack.getMaxDamage());
                result.put("name", GameRegistry.findUniqueIdentifierFor(stack.getItem()).toString());
                result.put("quantity", tileEntity.requests.getQuantity(slot));
                result.put("batch", tileEntity.requests.getBatchSize(slot));
                result.put("isFluid", ItemFluidDrop.isFluidStack(stack));
                if (ItemFluidDrop.isFluidStack(stack)) result.put("fluid", ItemFluidDrop.getFluidStack(stack));
                result.put("isEnable", tileEntity.requests.isEnable(slot));
                result.put("isDone", tileEntity.requests.isDone(slot));
                return new Object[] { result };
            }
            return new Object[] { null };
        }

        private void setSlot(int slot, String address, int index) {
            Node node = node().network().node(address);
            if (node instanceof Component) {
                if (node.host() instanceof Database) {
                    ItemStack dbStack = ((Database) node.host()).getStackInSlot(index);
                    if (dbStack == null) {
                        throw new IllegalArgumentException("Invalid slot");
                    }
                    dbStack.stackSize = 1;
                    ((AeItemStackHandler) tileEntity.getInventory()).getAeInventory()
                            .setStack(slot, AEItemStack.create(dbStack));
                } else {
                    throw new IllegalArgumentException("Not a database");
                }
            } else {
                throw new IllegalArgumentException("No such component");
            }
        }

        @Callback(
                doc = "function(slot:number[,database:address[,index:number]],quantity:number,batch:number):boolean -- Configuration the slot.")
        public Object[] setSlot(Context context, Arguments args) {
            int slot;
            String address;
            int index;
            int quantity;
            int batch;
            slot = args.checkInteger(0) - 1;

            if (slot < 0 || slot >= TileLevelMaintainer.REQ_COUNT) {
                throw new IllegalArgumentException("Invalid slot");
            }
            if (args.count() == 5) {
                address = args.checkString(1);
                index = args.checkInteger(2) - 1;
                quantity = args.checkInteger(3);
                batch = args.checkInteger(4);
                setSlot(slot, address, index);
                updateOrder(slot, quantity, batch);
            } else if (args.count() == 3) {
                quantity = args.checkInteger(1);
                batch = args.checkInteger(2);
                updateOrder(slot, quantity, batch);
            } else {
                throw new IllegalArgumentException("Invalid args");
            }
            return new Object[] { true };
        }

        private boolean isActive() {
            return tileEntity.isActive();
        }

        @Callback(doc = "function(slot:number):boolean -- Get the crafting task state.")
        public Object[] isDone(Context context, Arguments args) {
            return new Object[] { this.isDone(args) };
        }

        @Callback(doc = "function(slot:number):boolean -- Get the crafting state of slot.")
        public Object[] isEnable(Context context, Arguments args) {
            return new Object[] { this.isEnable(args) };
        }

        @Callback(doc = "function(slot:number,value:boolean):boolean -- Set the crafting state of slot.")
        public Object[] setEnable(Context context, Arguments args) {
            return new Object[] { this.setEnable(args) };
        }

        @Callback(doc = "function():boolean Get Level Maintainer state")
        public Object[] active(Context context, Arguments args) {
            return new Object[] { this.isActive() };
        }

        @Override
        public String preferredName() {
            return NameConst.BLOCK_LEVEL_MAINTAINER;
        }

        @Override
        public int priority() {
            return 6;
        }
    }

    public static class Provider implements EnvironmentProvider {

        Provider() {}

        @Override
        public Class<?> getEnvironment(ItemStack itemStack) {
            if (itemStack != null && itemStack.isItemEqual(ItemAndBlockHolder.LEVEL_MAINTAINER.stack())) {
                return DriverLevelMaintainer.Environment.class;
            }
            return null;
        }
    }
}
