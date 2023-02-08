package com.glodblock.github.client.gui.container;

import java.io.IOException;
import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.*;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.AEBasePart;
import appeng.parts.misc.PartInterface;
import appeng.parts.p2p.PartP2PInterface;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.WrapperInvSlot;

import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ContainerInterfaceWireless extends AEBaseContainer {

    /**
     * this stuff is all server side..
     */
    private static long autoBase = Long.MIN_VALUE;

    private final Multimap<IInterfaceHost, ContainerInterfaceWireless.InvTracker> diList = HashMultimap.create();
    private final Map<Long, ContainerInterfaceWireless.InvTracker> byId = new HashMap<Long, ContainerInterfaceWireless.InvTracker>();
    // private final Map<Long, InvTracker> byId = new HashMap<>();
    private IGrid grid;
    private NBTTagCompound data = new NBTTagCompound();

    public ContainerInterfaceWireless(final InventoryPlayer ip, final IWirelessTerminal anchor) {
        super(ip, anchor);

        if (Platform.isServer()) {
            this.grid = anchor.getActionableNode().getGrid();
        }

        this.bindPlayerInventory(ip, 14, 0);
    }

    private Set<IGridNode> getMachineNodes() {
        Set<IGridNode> union = new HashSet<>();
        this.grid.getMachines(TileFluidInterface.class).forEach(union::add);
        this.grid.getMachines(TileInterface.class).forEach(union::add);
        this.grid.getMachines(PartFluidInterface.class).forEach(union::add);
        this.grid.getMachines(PartInterface.class).forEach(union::add);
        this.grid.getMachines(PartP2PInterface.class).forEach(union::add);
        return union;
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();

        if (this.grid == null) {
            return;
        }

        int total = 0;
        boolean missing = false;

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.getMachineNodes()) {
                    ContainerInterfaceWireless.InterfaceCheck interfaceCheck = new ContainerInterfaceWireless.InterfaceCheck()
                            .invoke(gn);
                    total += interfaceCheck.getTotal();
                    missing |= interfaceCheck.isMissing();
                }
            }
        }

        if (total != this.diList.size() || missing) {
            this.regenList(this.data);
        } else {
            for (final ContainerInterfaceWireless.InvTracker inv : diList.values()) {
                for (int x = 0; x < inv.client.getSizeInventory(); x++) {
                    if (this.isDifferent(inv.server.getStackInSlot(inv.offset + x), inv.client.getStackInSlot(x))) {
                        this.addItems(this.data, inv, x, 1);
                    }
                }
            }
        }

        if (!this.data.hasNoTags()) {
            try {
                NetworkHandler.instance
                        .sendTo(new PacketCompressedNBT(this.data), (EntityPlayerMP) this.getPlayerInv().player);
            } catch (final IOException e) {
                // :P
            }

            this.data = new NBTTagCompound();
        }
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        final ContainerInterfaceWireless.InvTracker inv = this.byId.get(id);
        if (inv != null) {
            final ItemStack is = inv.server.getStackInSlot(slot + inv.offset);
            final boolean hasItemInHand = player.inventory.getItemStack() != null;

            final InventoryAdaptor playerHand = new AdaptorPlayerHand(player);

            final WrapperInvSlot slotInv = new ContainerInterfaceWireless.PatternInvSlot(inv.server);

            final IInventory theSlot = slotInv.getWrapper(slot + inv.offset);
            final InventoryAdaptor interfaceSlot = new AdaptorIInventory(theSlot);

            IInventory interfaceHandler = inv.server;
            boolean canInsert = true;

            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    if (hasItemInHand) {
                        for (int s = 0; s < interfaceHandler.getSizeInventory(); s++) {
                            if (Platform.isSameItemPrecise(
                                    interfaceHandler.getStackInSlot(s),
                                    player.inventory.getItemStack())) {
                                canInsert = false;
                                break;
                            }
                        }
                        if (canInsert) {
                            ItemStack inSlot = theSlot.getStackInSlot(0);
                            if (inSlot == null) {
                                player.inventory.setItemStack(interfaceSlot.addItems(player.inventory.getItemStack()));
                            } else {
                                inSlot = inSlot.copy();
                                final ItemStack inHand = player.inventory.getItemStack().copy();

                                theSlot.setInventorySlotContents(0, null);
                                player.inventory.setItemStack(null);

                                player.inventory.setItemStack(interfaceSlot.addItems(inHand.copy()));

                                if (player.inventory.getItemStack() == null) {
                                    player.inventory.setItemStack(inSlot);
                                } else {
                                    player.inventory.setItemStack(inHand);
                                    theSlot.setInventorySlotContents(0, inSlot);
                                }
                            }
                        }
                    } else {
                        final IInventory mySlot = slotInv.getWrapper(slot + inv.offset);
                        mySlot.setInventorySlotContents(0, playerHand.addItems(mySlot.getStackInSlot(0)));
                    }

                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    if (hasItemInHand) {
                        for (int s = 0; s < interfaceHandler.getSizeInventory(); s++) {
                            if (Platform.isSameItemPrecise(
                                    interfaceHandler.getStackInSlot(s),
                                    player.inventory.getItemStack())) {
                                canInsert = false;
                                break;
                            }
                        }
                        if (canInsert) {
                            ItemStack extra = playerHand.removeItems(1, null, null);
                            if (extra != null && !interfaceSlot.containsItems()) {
                                extra = interfaceSlot.addItems(extra);
                            }
                            if (extra != null) {
                                playerHand.addItems(extra);
                            }
                        }
                    } else if (is != null) {
                        ItemStack extra = interfaceSlot.removeItems((is.stackSize + 1) / 2, null, null);
                        if (extra != null) {
                            extra = playerHand.addItems(extra);
                        }
                        if (extra != null) {
                            interfaceSlot.addItems(extra);
                        }
                    }

                    break;
                case SHIFT_CLICK:
                    final IInventory mySlot = slotInv.getWrapper(slot + inv.offset);
                    final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                    mySlot.setInventorySlotContents(0, mergeToPlayerInventory(playerInv, mySlot.getStackInSlot(0)));

                    break;
                case MOVE_REGION:
                    final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                    for (int x = 0; x < inv.client.getSizeInventory(); x++) {
                        inv.server.setInventorySlotContents(
                                x + inv.offset,
                                mergeToPlayerInventory(playerInvAd, inv.server.getStackInSlot(x + inv.offset)));
                    }

                    break;
                case CREATIVE_DUPLICATE:
                    if (player.capabilities.isCreativeMode && !hasItemInHand) {
                        player.inventory.setItemStack(is == null ? null : is.copy());
                    }

                    break;
                default:
                    return;
            }

            this.updateHeld(player);
        }
    }

    private ItemStack mergeToPlayerInventory(InventoryAdaptor playerInv, ItemStack stack) {
        if (stack == null) return null;
        for (ItemSlot slot : playerInv) {
            if (Platform.isSameItemPrecise(slot.getItemStack(), stack)) {
                if (slot.getItemStack().stackSize < slot.getItemStack().getMaxStackSize()) {
                    ++slot.getItemStack().stackSize;
                    return null;
                }
            }
        }
        return playerInv.addItems(stack);
    }

    private void regenList(final NBTTagCompound data) {
        this.byId.clear();
        this.diList.clear();

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.getMachineNodes()) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        for (int i = 0; i <= dual.getInstalledUpgrades(Upgrades.PATTERN_CAPACITY); ++i) {
                            this.diList.put(
                                    ih,
                                    new ContainerInterfaceWireless.InvTracker(
                                            dual,
                                            dual.getPatterns(),
                                            dual.getTermName(),
                                            i * 9,
                                            9));
                        }
                    }
                }
            }
        }

        data.setBoolean("clear", true);

        for (final ContainerInterfaceWireless.InvTracker inv : this.diList.values()) {
            this.byId.put(inv.which, inv);
            this.addItems(data, inv, 0, inv.client.getSizeInventory());
        }
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if (a == null && b == null) {
            return false;
        }

        if (a == null || b == null) {
            return true;
        }

        return !ItemStack.areItemStacksEqual(a, b);
    }

    private void addItems(final NBTTagCompound data, final ContainerInterfaceWireless.InvTracker inv, final int offset,
            final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final NBTTagCompound tag = data.getCompoundTag(name);

        if (tag.hasNoTags()) {
            tag.setLong("sortBy", inv.sortBy);
            tag.setString("un", inv.unlocalizedName);
            tag.setInteger("x", inv.X);
            tag.setInteger("y", inv.Y);
            tag.setInteger("z", inv.Z);
            tag.setInteger("dim", inv.dim);
            tag.setInteger("side", inv.side.ordinal());
        }

        for (int x = 0; x < length; x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();

            final ItemStack is = inv.server.getStackInSlot(x + offset + inv.offset);

            // "update" client side.
            inv.client.setInventorySlotContents(offset + x, is == null ? null : is.copy());

            if (is != null) {
                is.writeToNBT(itemNBT);
            }

            tag.setTag(Integer.toString(x + offset), itemNBT);
        }

        data.setTag(name, tag);
    }

    private static class InvTracker {

        private final long sortBy;
        private final long which = autoBase++;
        private final String unlocalizedName;
        private final IInventory client;
        private final IInventory server;
        private final int offset;
        private final int X;
        private final int Y;
        private final int Z;
        private final int dim;
        private final ForgeDirection side;

        public InvTracker(final DualityInterface dual, final IInventory patterns, final String unlocalizedName,
                int offset, int size) {
            this.server = patterns;
            this.client = new AppEngInternalInventory(null, size);
            this.unlocalizedName = unlocalizedName;
            this.sortBy = dual.getSortValue() + offset << 16;
            this.offset = offset;
            X = dual.getLocation().x;
            Y = dual.getLocation().y;
            Z = dual.getLocation().z;
            dim = dual.getLocation().getDimension();
            if (dual.getHost() instanceof AEBasePart) {
                side = ((AEBasePart) dual.getHost()).getSide();
            } else {
                side = ForgeDirection.UNKNOWN;
            }
        }
    }

    private static class PatternInvSlot extends WrapperInvSlot {

        public PatternInvSlot(final IInventory inv) {
            super(inv);
        }

        @Override
        public boolean isItemValid(final ItemStack itemstack) {
            return itemstack != null && itemstack.getItem() instanceof ItemEncodedPattern;
        }
    }

    private class InterfaceCheck {

        int total = 0;
        boolean missing = false;

        public InterfaceCheck() {}

        public int getTotal() {
            return total;
        }

        public boolean isMissing() {
            return missing;
        }

        public ContainerInterfaceWireless.InterfaceCheck invoke(IGridNode gn) {
            if (gn.isActive()) {
                final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                if (ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO
                        || ih instanceof PartP2PInterface && ((PartP2PInterface) ih).isOutput()) {
                    return this;
                }

                final Collection<ContainerInterfaceWireless.InvTracker> t = ContainerInterfaceWireless.this.diList
                        .get(ih);

                if (t.isEmpty()) {
                    missing = true;
                } else {
                    final DualityInterface dual = ih.getInterfaceDuality();
                    for (ContainerInterfaceWireless.InvTracker it : t) {
                        if (!it.unlocalizedName.equals(dual.getTermName())) {
                            missing = true;
                        }
                    }
                }

                total += (ih.getInterfaceDuality().getInstalledUpgrades(Upgrades.PATTERN_CAPACITY) + 1);
            }
            return this;
        }
    }
}
