package com.glodblock.github.util;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import org.apache.commons.lang3.tuple.MutablePair;

public final class Util {

    public static EnumFacing from(ForgeDirection direction) {
        switch (direction) {
            case WEST:
                return EnumFacing.EAST;
            case EAST:
                return EnumFacing.WEST;
            case UNKNOWN:
                return null;
            default: {
                int o = direction.ordinal();
                return EnumFacing.values()[o];
            }
        }
    }

    public static ForgeDirection from(EnumFacing direction) {
        switch (direction) {
            case WEST:
                return ForgeDirection.EAST;
            case EAST:
                return ForgeDirection.WEST;
            default: {
                int o = direction.ordinal();
                return ForgeDirection.values()[o];
            }
        }
    }

    public static int findItemInPlayerInvSlot(EntityPlayer player, ItemStack itemStack) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i] == itemStack) return i;
        }
        return -1;
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGrid grid) {
        return grid == null || hasPermission(player, permission, (ISecurityGrid) grid.getCache(ISecurityGrid.class));
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridHost host) {
        return hasPermission(player, permission, host, ForgeDirection.UNKNOWN);
    }

    public static boolean hasPermission(
            EntityPlayer player, SecurityPermissions permission, IGridHost host, ForgeDirection side) {
        return host == null || hasPermission(player, permission, host.getGridNode(side));
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridNode host) {
        return host == null || hasPermission(player, permission, host.getGrid());
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IPart part) {
        return part == null || hasPermission(player, permission, part.getGridNode());
    }

    public static boolean hasPermission(
            EntityPlayer player, SecurityPermissions permission, ISecurityGrid securityGrid) {
        return player == null
                || permission == null
                || securityGrid == null
                || securityGrid.hasPermission(player, permission);
    }

    public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
        if (size == 0 || itemStack == null) return null;
        ItemStack copy = itemStack.copy();
        copy.stackSize = size;
        return copy;
    }

    public static AEFluidStack getAEFluidFromItem(ItemStack stack) {
        if (stack != null
                && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
            if (stack.getItem() instanceof IFluidContainerItem) {
                FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                if (fluid != null) {
                    AEFluidStack fluid0 = AEFluidStack.create(fluid.copy());
                    fluid0.setStackSize(fluid0.getStackSize() * stack.stackSize);
                    return fluid0;
                }
            }
            if (FluidContainerRegistry.isContainer(stack)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (fluid != null) {
                    AEFluidStack fluid0 = AEFluidStack.create(fluid.copy());
                    fluid0.setStackSize(fluid0.getStackSize() * stack.stackSize);
                    return fluid0;
                }
            }
        }
        return null;
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (stack != null
                && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
            if (stack.getItem() instanceof IFluidContainerItem) {
                FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
            if (FluidContainerRegistry.isContainer(stack)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
        }
        return null;
    }

    public static boolean isFluidPacket(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemFluidPacket;
    }

    public static FluidStack cloneFluidStack(FluidStack fluidStack) {
        if (fluidStack != null) return fluidStack.copy();
        return null;
    }

    public static String getFluidModID(Fluid fluid) {
        String name = FluidRegistry.getDefaultFluidName(fluid);
        try {
            return name.split(":")[0];
        } catch (Exception e) {
            return "";
        }
    }

    public static ModContainer getFluidMod(Fluid fluid) {
        return GameData.findModOwner(String.format("%s:%s", getFluidModID(fluid), fluid.getName()));
    }

    public static int getFluidID(Fluid fluid) {
        return GameData.getBlockRegistry().getId(fluid.getBlock());
    }

    public static String getFluidModName(Fluid fluid) {
        try {
            ModContainer mod = getFluidMod(fluid);
            return mod == null ? "Minecraft" : mod.getName();
        } catch (Exception e) {
            return "";
        }
    }

    public static IAEFluidStack loadFluidStackFromNBT(final NBTTagCompound i) {
        // Fuck ae2
        final FluidStack t = FluidRegistry.getFluidStack(i.getString("FluidName"), 1);
        final AEFluidStack fluid = AEFluidStack.create(t);
        fluid.setStackSize(i.getLong("Cnt"));
        fluid.setCountRequestable(i.getLong("Req"));
        fluid.setCraftable(i.getBoolean("Craft"));
        return fluid;
    }

    public static boolean areFluidsEqual(FluidStack fluid1, FluidStack fluid2) {
        if (fluid1 == null || fluid2 == null) return false;
        return fluid1.isFluidEqual(fluid2);
    }

    public static class FluidUtil {
        public static IAEFluidStack createAEFluidStack(Fluid fluid) {
            return createAEFluidStack(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME));
        }

        public static IAEFluidStack createAEFluidStack(Fluid fluid, long amount) {
            return createAEFluidStack(fluid.getID(), amount);
        }

        public static IAEFluidStack createAEFluidStack(FluidStack fluid) {
            return AEApi.instance().storage().createFluidStack(fluid);
        }

        public static IAEFluidStack createAEFluidStack(int fluidId, long amount) {
            return createAEFluidStack(new FluidStack(FluidRegistry.getFluid(fluidId), 1))
                    .setStackSize(amount);
        }

        public static boolean isEmpty(ItemStack itemStack) {
            if (itemStack == null) return false;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                FluidStack content = ((IFluidContainerItem) item).getFluid(itemStack);
                return content == null || content.amount <= 0;
            }
            return FluidContainerRegistry.isEmptyContainer(itemStack);
        }

        public static boolean isFilled(ItemStack itemStack) {
            if (itemStack == null) return false;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                FluidStack content = ((IFluidContainerItem) item).getFluid(itemStack);
                return content != null && content.amount > 0;
            }
            return FluidContainerRegistry.isFilledContainer(itemStack);
        }

        public static boolean isFluidContainer(ItemStack itemStack) {
            if (itemStack == null) return false;
            Item item = itemStack.getItem();
            return item instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(itemStack);
        }

        public static FluidStack getFluidFromContainer(ItemStack itemStack) {
            if (itemStack == null) return null;

            ItemStack container = itemStack.copy();
            Item item = container.getItem();
            if (item instanceof IFluidContainerItem) {
                return ((IFluidContainerItem) item).getFluid(container);
            } else {
                return FluidContainerRegistry.getFluidForFilledItem(container);
            }
        }

        public static int getCapacity(ItemStack itemStack, Fluid fluid) {
            if (itemStack == null) return 0;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                IFluidContainerItem fluidContainerItem = (IFluidContainerItem) item;
                int capacity = fluidContainerItem.getCapacity(itemStack);
                FluidStack existing = fluidContainerItem.getFluid(itemStack);
                if (existing != null) {
                    if (!existing.getFluid().equals(fluid)) {
                        return 0;
                    }
                    capacity -= existing.amount;
                }
                return capacity;
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                return FluidContainerRegistry.getContainerCapacity(new FluidStack(fluid, Integer.MAX_VALUE), itemStack);
            }
            return 0;
        }

        public static ItemStack clearFluid(ItemStack itemStack) {
            if (itemStack == null) return null;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                ((IFluidContainerItem) item)
                        .drain(itemStack, ((IFluidContainerItem) item).getFluid(itemStack).amount, true);
                return itemStack;
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                return FluidContainerRegistry.drainFluidContainer(itemStack);
            }
            return null;
        }

        public static MutablePair<Integer, ItemStack> drainStack(ItemStack itemStack, FluidStack fluid) {
            if (itemStack == null) return null;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                FluidStack drained = ((IFluidContainerItem) item).drain(itemStack, fluid.amount, true);
                int amountDrained = drained != null && drained.getFluid() == fluid.getFluid() ? drained.amount : 0;
                return new MutablePair<Integer, ItemStack>(amountDrained, itemStack);
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                FluidStack content = FluidContainerRegistry.getFluidForFilledItem(itemStack);
                int amountDrained = content != null && content.getFluid() == fluid.getFluid() ? content.amount : 0;
                return new MutablePair<Integer, ItemStack>(
                        amountDrained, FluidContainerRegistry.drainFluidContainer(itemStack));
            }

            return null;
        }

        public static MutablePair<Integer, ItemStack> fillStack(ItemStack itemStack, FluidStack fluid) {
            if (itemStack == null || itemStack.stackSize != 1) return null;
            Item item = itemStack.getItem();
            // If its a fluid container item instance
            if (item instanceof IFluidContainerItem) {
                // Call the fill method on it.
                int filled = ((IFluidContainerItem) item).fill(itemStack, fluid, true);

                // Return the filled itemstack.
                return new MutablePair<Integer, ItemStack>(filled, itemStack);
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                // Fill it through the fluidcontainer registry.
                ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(fluid, itemStack);
                // get the filled fluidstack.
                FluidStack filled = FluidContainerRegistry.getFluidForFilledItem(filledContainer);
                // Return filled container and fill amount.
                return new MutablePair<Integer, ItemStack>(filled != null ? filled.amount : 0, filledContainer);
            }
            return null;
        }
    }
}
