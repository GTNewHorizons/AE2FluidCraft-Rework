package com.glodblock.github.client.gui.container;

import static appeng.util.Platform.isServer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.SPacketSuperStockReplenisherUpdate;
import com.glodblock.github.util.FluidPrimaryGui;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.interfaces.IVirtualSlotHolder;
import appeng.container.interfaces.IVirtualSlotSource;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class ContainerSuperStockReplenisher extends AEBaseContainer implements IVirtualSlotHolder, IVirtualSlotSource {

    private final TileSuperStockReplenisher tile;

    private boolean isFirstUpdate = true;
    private int lastUpdated = 0;

    private final IAEStackInventory configFluids;
    private final IAEStack<?>[] configFluidsClientSlots = new IAEStack[9];

    private final IAEStackInventory configItems;
    private final IAEStack<?>[] configItemsClientSlots = new IAEStack[63];

    public ContainerSuperStockReplenisher(InventoryPlayer ipl, TileSuperStockReplenisher tile) {
        super(ipl, tile);
        this.tile = tile;
        this.configFluids = tile.getConfigFluids();
        this.configItems = tile.getConfigItems();
        final IInventory cell = tile.getCell();

        this.addSlotToContainer(
                new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL,
                        cell,
                        0,
                        173,
                        8,
                        this.getInventoryPlayer()));

        bindPlayerInventory(ipl, 0, 251 - 82);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (slotId == 0 && player.inventory.getItemStack() == null && isConfigurated()) return null;
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isClient()) return;

        if (this.isFirstUpdate || this.lastUpdated > 20) {
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

        if (this.isFirstUpdate) {
            this.updateVirtualSlots(StorageName.NONE, this.configFluids, this.configFluidsClientSlots);
            this.updateVirtualSlots(StorageName.CONFIG, this.configItems, this.configItemsClientSlots);
            this.isFirstUpdate = false;
        }
    }

    private boolean isConfigurated() {
        for (int i = 0; i < configItems.getSizeInventory(); i++)
            if (configItems.getAEStackInSlot(i) != null) return true;

        for (int i = 0; i < configFluids.getSizeInventory(); i++)
            if (configFluids.getAEStackInSlot(i) != null) return true;
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
        switch (invName) {
            case NONE -> {
                for (var entry : slotStacks.int2ObjectEntrySet())
                    this.configFluids.putAEStackInSlot(entry.getIntKey(), entry.getValue());
                if (isServer())
                    this.updateVirtualSlots(StorageName.NONE, this.configFluids, this.configFluidsClientSlots);
            }
            case CONFIG -> {
                for (var entry : slotStacks.int2ObjectEntrySet())
                    this.configItems.putAEStackInSlot(entry.getIntKey(), entry.getValue());
                if (isServer())
                    this.updateVirtualSlots(StorageName.CONFIG, this.configItems, this.configItemsClientSlots);
            }
        }
    }

    @Override
    public void updateVirtualSlot(StorageName invName, int slotId, IAEStack<?> aes) {
        if (aes != null && aes.getStackSize() > Integer.MAX_VALUE) aes.setStackSize(Integer.MAX_VALUE);
        switch (invName) {
            case NONE -> {
                this.configFluids.putAEStackInSlot(slotId, aes);
                this.updateVirtualSlots(StorageName.NONE, this.configFluids, this.configFluidsClientSlots);
            }
            case CONFIG -> {
                this.configItems.putAEStackInSlot(slotId, aes);
                this.updateVirtualSlots(StorageName.CONFIG, this.configItems, this.configItemsClientSlots);
            }
        }
    }
}
