package com.glodblock.github.util;

import static com.glodblock.github.common.item.ItemBaseWirelessTerminal.infinityBoosterCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.item.IFluidPortableCell;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.container.AEBaseContainer;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.me.storage.NullInventory;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import baubles.api.BaublesApi;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import io.netty.buffer.ByteBuf;

public final class Util {

    public static int drainItemPower(AEBaseContainer c, InventoryPlayer ip, int slot, int ticks, double pm,
            IFluidPortableCell wt) {
        if (slot != -1) {
            final ItemStack currentItem = getWirelessTerminal(ip.player, slot);
            if (wt != null) {
                if (currentItem != wt.getItemStack()) {
                    if (currentItem != null) {
                        if (Platform.isSameItem(wt.getItemStack(), currentItem)) {
                            if (GuiHelper.decodeInvType(slot).getLeft() == GuiHelper.InvType.PLAYER_INV) {
                                ip.setInventorySlotContents(slot, wt.getItemStack());
                            } else {
                                BaublesApi.getBaubles(ip.player).setInventorySlotContents(
                                        GuiHelper.decodeInvType(slot).getRight(),
                                        wt.getItemStack());
                            }
                        } else {
                            c.setValidContainer(false);
                        }
                    } else {
                        c.setValidContainer(false);
                    }
                }
            } else {
                c.setValidContainer(false);
            }
        } else {
            c.setValidContainer(false);
        }
        ticks++;
        if (ticks > 10 && wt != null) {
            wt.extractAEPower(pm * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            ticks = 0;
        }
        return ticks;
    }

    public static boolean hasInfinityBoosterCard(ItemStack is) {
        if (ModAndClassUtil.WCT && is.getItem() instanceof ItemBaseWirelessTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            return data.hasKey(infinityBoosterCard) && data.getBoolean(infinityBoosterCard);
        }
        return false;
    }

    public static IGridNode getWirelessGrid(ItemStack is) {
        if (is.getItem() instanceof ToolWirelessTerminal) {
            String key = ((ToolWirelessTerminal) is.getItem()).getEncryptionKey(is);
            IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable()
                    .getLocatableBy(Long.parseLong(key));
            if (securityTerminal == null) return null;
            return securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
        }
        return null;
    }

    public static IMEInventoryHandler<?> getWirelessInv(ItemStack is, EntityPlayer player, StorageChannel channel) {
        if (Platform.isClient()) return new NullInventory<>();
        IGridNode gridNode = getWirelessGrid(is);
        if (gridNode == null) return null;
        IGrid grid = gridNode.getGrid();
        if (grid == null) return null;
        boolean canConnect = false;
        if (hasInfinityBoosterCard(is)) {
            canConnect = true;
        } else {
            for (IGridNode node : grid.getMachines(TileWireless.class)) {
                IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
                if (accessPoint.isActive() && accessPoint.getLocation().getDimension() == player.dimension) {
                    WorldCoord distance = accessPoint.getLocation()
                            .subtract((int) player.posX, (int) player.posY, (int) player.posZ);
                    int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
                    if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
                        canConnect = true;
                        break;
                    }
                }
            }
        }
        if (canConnect) {
            IStorageGrid gridCache = grid.getCache(IStorageGrid.class);
            if (gridCache != null) {
                if (channel == StorageChannel.FLUIDS) {
                    return gridCache.getFluidInventory();
                } else {
                    return gridCache.getItemInventory();
                }
            }
        }
        return null;
    }

    public static ItemStack getWirelessTerminal(EntityPlayer player, int x) {
        ImmutablePair<GuiHelper.InvType, Integer> result = GuiHelper.decodeInvType(x);
        if (result.getLeft() == GuiHelper.InvType.PLAYER_INV) {
            return player.inventory.getStackInSlot(result.getRight());
        } else {
            return BaublesApi.getBaubles(player).getStackInSlot(result.getRight());
        }
    }

    public static ImmutablePair<Integer, ItemStack> getUltraWirelessTerm(EntityPlayer player) {
        int invSize = player.inventory.getSizeInventory();

        if (invSize <= 0) {
            return null;
        }
        for (int i = 0; i < invSize; ++i) {
            ItemStack is = player.inventory.getStackInSlot(i);
            if (is != null && is.getItem() instanceof ItemWirelessUltraTerminal) {
                return new ImmutablePair<>(i, is);
            }
        }
        IInventory handler = BaublesApi.getBaubles(player);
        if (handler != null) {
            invSize = handler.getSizeInventory();
            for (int i = 0; i < invSize; ++i) {
                ItemStack is = handler.getStackInSlot(i);
                if (is != null && is.getItem() instanceof ItemWirelessUltraTerminal) {
                    return new ImmutablePair<>(GuiHelper.encodeType(i, GuiHelper.InvType.PLAYER_BAUBLES), is);
                }
            }
        }
        return null;
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

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridHost host,
            ForgeDirection side) {
        return host == null || hasPermission(player, permission, host.getGridNode(side));
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridNode host) {
        return host == null || hasPermission(player, permission, host.getGrid());
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IPart part) {
        return part == null || hasPermission(player, permission, part.getGridNode());
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission,
            ISecurityGrid securityGrid) {
        return player == null || permission == null
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
        final FluidStack t = FluidRegistry.getFluidStack(i.getString("FluidName"), 1);
        if (t == null) return null;
        final AEFluidStack fluid = AEFluidStack.create(t);
        fluid.setStackSize(i.getLong("Cnt"));
        fluid.setCountRequestable(i.getLong("Req"));
        fluid.setCraftable(i.getBoolean("Craft"));
        return fluid;
    }

    public static void mirrorFluidToPacket(IInventory packet, IAEFluidTank fluidTank) {
        for (int i = 0; i < fluidTank.getSlots(); i++) {
            IAEFluidStack fluid = fluidTank.getFluidInSlot(i);
            if (fluid == null) {
                packet.setInventorySlotContents(i, null);
            } else {
                packet.setInventorySlotContents(i, ItemFluidPacket.newDisplayStack(fluid.getFluidStack()));
            }
        }
    }

    public static FluidStack getFluidFromVirtual(ItemStack virtual) {
        if (virtual == null) {
            return null;
        }
        if (virtual.getItem() instanceof ItemFluidPacket) {
            return ItemFluidPacket.getFluidStack(virtual);
        } else if (virtual.getItem() instanceof ItemFluidDrop) {
            return ItemFluidDrop.getFluidStack(virtual);
        }
        return null;
    }

    public static IPart getPart(Object te, ForgeDirection face) {
        if (te instanceof TileCableBus) {
            return ((TileCableBus) te).getPart(face);
        }
        return null;
    }

    public static void writeFluidMapToBuf(Map<Integer, IAEFluidStack> list, ByteBuf buf) throws IOException {
        buf.writeInt(list.size());
        for (Map.Entry<Integer, IAEFluidStack> fs : list.entrySet()) {
            buf.writeInt(fs.getKey());
            if (fs.getValue() == null) buf.writeBoolean(false);
            else {
                buf.writeBoolean(true);
                fs.getValue().writeToPacket(buf);
            }
        }
    }

    public static void readFluidMapFromBuf(Map<Integer, IAEFluidStack> list, ByteBuf buf) throws IOException {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            int id = buf.readInt();
            boolean isNull = buf.readBoolean();
            if (!isNull) list.put(id, null);
            else {
                IAEFluidStack fluid = AEFluidStack.loadFluidStackFromPacket(buf);
                list.put(id, fluid);
            }
        }
    }

    public static class GuiHelper {

        public enum GuiType {
            TILE,
            ITEM
        }

        public enum InvType {
            PLAYER_INV,
            PLAYER_BAUBLES
        }

        private static final int value = 1 << 30;

        public static int encodeType(int y, GuiType type) {
            if (Math.abs(y) > (1 << 28)) {
                throw new IllegalArgumentException("out of range");
            }
            return value | (type.ordinal() << 29) | y;
        }

        public static int encodeType(int x, InvType type) {
            if (Math.abs(x) > (1 << 28)) {
                throw new IllegalArgumentException("out of range");
            }
            return value | (type.ordinal() << 29) | x;
        }

        public static ImmutablePair<GuiType, Integer> decodeType(int y) {
            if (Math.abs(y) > (1 << 28)) {
                return new ImmutablePair<>(GuiType.values()[y >> 29 & 1], y - (3 << 29 & y));
            } else {
                return new ImmutablePair<>(GuiType.TILE, y);
            }
        }

        public static ImmutablePair<InvType, Integer> decodeInvType(int x) {
            if (Math.abs(x) > (1 << 28)) {
                return new ImmutablePair<>(InvType.values()[x >> 29 & 1], x - (3 << 29 & x));
            } else {
                return new ImmutablePair<>(InvType.PLAYER_INV, x);
            }
        }
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : (Math.min(value, max));
    }

    public static class FluidUtil {

        public static void fluidTankInfoWriteToNBT(FluidTankInfo[] infos, NBTTagCompound data) {
            int i = 0;
            for (FluidTankInfo info : infos) {
                if (info.fluid != null && info.fluid.amount > 0) {
                    NBTTagCompound fs = new NBTTagCompound();
                    info.fluid.writeToNBT(fs);
                    fs.setInteger("capacity", info.capacity);
                    data.setTag("#" + i, fs);
                }
                i++;
            }
            data.setInteger("fluidInvSize", i);
        }

        public static FluidTankInfo[] fluidTankInfoReadFromNBT(NBTTagCompound data) {
            int i = 0;
            List<FluidTankInfo> infos = new ArrayList<>();
            while (data.hasKey("fluidInvSize") && i < data.getInteger("fluidInvSize")) {
                if (data.hasKey("#" + i)) {
                    NBTTagCompound tag = (NBTTagCompound) data.getTag("#" + i);
                    FluidStack fs = FluidStack.loadFluidStackFromNBT(tag);
                    infos.add(new FluidTankInfo(fs, tag.getInteger("capacity")));
                } else {
                    infos.add(new FluidTankInfo(null, 0));
                }
                i++;
            }
            return infos.toArray(new FluidTankInfo[0]);
        }

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
            return createAEFluidStack(new FluidStack(FluidRegistry.getFluid(fluidId), 1)).setStackSize(amount);
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
            if (item instanceof IFluidContainerItem fluidContainerItem) {
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

        public static ItemStack setFluidContainerAmount(ItemStack itemStack, int amount) {
            FluidStack fs = getFluidFromContainer(itemStack);
            if (fs == null) return null;
            fs.amount = amount;
            ItemStack is = itemStack.copy();
            is.stackSize = 1;
            ItemStack emptyContainer = clearFluid(is);
            if (emptyContainer == null) return null;
            MutablePair<Integer, ItemStack> result = fillStack(emptyContainer, fs);
            if (result != null) {
                result.right.stackSize = itemStack.stackSize;
                return result.right;
            }
            return null;
        }

        public static MutablePair<Integer, ItemStack> drainStack(ItemStack itemStack, FluidStack fluid) {
            if (itemStack == null) return null;
            Item item = itemStack.getItem();
            if (item instanceof IFluidContainerItem) {
                FluidStack drained = ((IFluidContainerItem) item).drain(itemStack, fluid.amount, true);
                int amountDrained = drained != null && drained.getFluid() == fluid.getFluid() ? drained.amount : 0;
                return new MutablePair<>(amountDrained, itemStack);
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                FluidStack content = FluidContainerRegistry.getFluidForFilledItem(itemStack);
                int amountDrained = content != null && content.getFluid() == fluid.getFluid() ? content.amount : 0;
                return new MutablePair<>(amountDrained, FluidContainerRegistry.drainFluidContainer(itemStack));
            }

            return null;
        }

        public static MutablePair<Integer, ItemStack> fillStack(ItemStack itemStack, FluidStack fluid) {
            if (itemStack == null || itemStack.stackSize != 1) return null;
            Item item = itemStack.getItem();
            // If it's a fluid container item instance
            if (item instanceof IFluidContainerItem) {
                // Call the fill method on it.
                int filled = ((IFluidContainerItem) item).fill(itemStack, fluid, true);

                // Return the filled itemstack.
                return new MutablePair<>(filled, itemStack);
            } else if (FluidContainerRegistry.isContainer(itemStack)) {
                // Fill it through the fluidcontainer registry.
                ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(fluid, itemStack);
                // get the filled fluidstack.
                FluidStack filled = FluidContainerRegistry.getFluidForFilledItem(filledContainer);
                // Return filled container and fill amount.
                return new MutablePair<>(filled != null ? filled.amount : 0, filledContainer);
            }
            return null;
        }
    }

    public static class DimensionalCoordSide extends DimensionalCoord {

        private ForgeDirection side = ForgeDirection.UNKNOWN;
        private final String name;

        public DimensionalCoordSide(final int _x, final int _y, final int _z, final int _dim, ForgeDirection side,
                String name) {
            super(_x, _y, _z, _dim);
            this.side = side;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public ForgeDirection getSide() {
            return this.side;
        }

        @Override
        public void writeToNBT(NBTTagCompound data) {
            data.setInteger("side", this.side.ordinal());
            data.setString("name", this.name);
            super.writeToNBT(data);
        }

        public static DimensionalCoordSide readFromNBT(final NBTTagCompound data) {
            return new DimensionalCoordSide(
                    data.getInteger("x"),
                    data.getInteger("y"),
                    data.getInteger("z"),
                    data.getInteger("dim"),
                    ForgeDirection.getOrientation(data.getInteger("side")),
                    data.getString("name"));
        }

    }
}
