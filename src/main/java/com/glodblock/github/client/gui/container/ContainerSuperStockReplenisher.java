package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFake;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.SPacketSuperStockReplenisherUpdate;
import com.glodblock.github.util.FluidPrimaryGui;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiPatternItemRenamer;
import appeng.client.gui.implementations.GuiPatternValueAmount;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.interfaces.IVirtualSlotHolder;
import appeng.container.interfaces.IVirtualSlotSource;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketVirtualSlot;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class ContainerSuperStockReplenisher extends AEBaseContainer
        implements IOptionalSlotHost, IVirtualSlotHolder, IVirtualSlotSource {

    private final TileSuperStockReplenisher tile;
    private final IInventory configFluids;
    private final IInventory configItems;
    private int lastUpdated = 0;

    public ContainerSuperStockReplenisher(InventoryPlayer ipl, TileSuperStockReplenisher tile) {
        super(ipl, tile);
        this.tile = tile;
        configFluids = tile.getConfigFluid();
        configItems = tile.getConfigItems();
        final IInventory cell = tile.getCell();

        final int x = 8;
        final int fy = 8;

        this.addSlotToContainer(
                new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL,
                        cell,
                        0,
                        173,
                        fy,
                        this.getInventoryPlayer()));

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new OptionalFluidSlotFake(configFluids, this, i, x, fy, i, 0, 0));
        }

        final int io = 29;
        for (int y = 0; y < 7; y++) {
            for (int ix = 0; ix < 9; ix++) {
                this.addSlotToContainer(new SlotPatternOutputs(configItems, this, y * 9 + ix, x, io, ix, y, y));
            }
        }

        bindPlayerInventory(ipl, 0, 251 - 82);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (slotId == 0 && player.inventory.getItemStack() == null && isConfigurated()) {
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        super.addCraftingToCrafters(p_75132_1_);
        lastUpdated = 24;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (lastUpdated > 20) {
            lastUpdated = 0;
            Map<Integer, IAEStack<?>> tmp = new HashMap<>();

            for (int i = 0; i < tile.getInternalFluid().getSlots(); i++) {
                tmp.put(i, tile.getInternalFluid().getFluidInSlot(i));
            }

            for (int i = 0; i < tile.getInternalInventory().getSizeInventory(); i++) {
                tmp.put(i + 100, AEItemStack.create(tile.getInternalInventory().getStackInSlot(i)));
            }

            for (final Object g : this.crafters) {
                if (g instanceof EntityPlayer) {
                    FluidCraft.proxy.netHandler.sendTo(new SPacketSuperStockReplenisherUpdate(tmp), (EntityPlayerMP) g);
                }
            }
        }
        lastUpdated++;
    }

    private boolean isConfigurated() {
        for (int i = 0; i < configItems.getSizeInventory(); i++) {
            if (configItems.getStackInSlot(i) != null) {
                return true;
            }
        }
        for (int i = 0; i < configFluids.getSizeInventory(); i++) {
            if (configFluids.getStackInSlot(i) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidForSlot(Slot s, ItemStack is) {
        if (s.slotNumber == 0 && s.getHasStack() && isConfigurated()) {
            long currentBytes = 0;
            long newBytes = -1;

            if (s.getStack().getItem() instanceof ItemBasicStorageCell ibsc) {
                currentBytes = ibsc.getBytesLong(is);
            } else if (s.getStack().getItem() instanceof FCBaseItemCell fcbic) {
                currentBytes = fcbic.getBytes(is);
            }

            if (is.getItem() instanceof ItemBasicStorageCell ibsc) {
                newBytes = ibsc.getBytesLong(is);
            } else if (is.getItem() instanceof FCBaseItemCell fcbic) {
                newBytes = fcbic.getBytes(is);
            }

            if (currentBytes > newBytes) {
                return false;
            }
        }
        return super.isValidForSlot(s, is);
    }

    public TileSuperStockReplenisher getTile() {
        return tile;
    }

    @Override
    public PrimaryGui createPrimaryGui() {
        ContainerOpenContext context = getOpenContext();
        return new FluidPrimaryGui(
                GuiType.GUI_SUPER_STOCK_REPLENISHER,
                ItemAndBlockHolder.SUPER_STOCK_RERPLENISHER.stack(),
                context.getTile(),
                context.getSide());
    }

    @Override
    public void receiveSlotStacks(StorageName invName, Int2ObjectMap<IAEStack<?>> slotStacks) {
        var entry = slotStacks.int2ObjectEntrySet().iterator().next();
        IAEStack<?> aes = entry.getValue();
        int slotIndex = entry.getIntKey();

        if (Platform.isServer()) {
            for (ICrafting crafter : this.crafters) {
                final EntityPlayerMP emp = (EntityPlayerMP) crafter;
                NetworkHandler.instance.sendTo(new PacketVirtualSlot(invName, slotIndex, aes), emp);
            }
        } else {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiPatternValueAmount gpva) {
                gpva.update();
            } else if (gs instanceof GuiPatternItemRenamer gpir) {
                gpir.update();
            }
        }
    }

    @Override
    public void updateVirtualSlot(StorageName invName, int slotId, IAEStack<?> aes) {
        if (aes instanceof IAEItemStack ais) {
            ItemStack newIs;
            if (ais.getItem() instanceof ItemFluidPacket) {
                newIs = ais.getItemStack();
                newIs.stackSize = 1;
                ItemFluidPacket.setFluidAmount(newIs, aes.getStackSize());
            } else {
                newIs = ais.getItemStack();
            }
            this.inventorySlots.get(slotId).putStack(newIs);
        }
    }

}
