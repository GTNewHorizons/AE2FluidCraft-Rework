package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.SPacketSuperStockReplenisherUpdate;
import com.glodblock.github.util.FluidPrimaryGui;

import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.interfaces.IVirtualSlotSource;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.sync.SyncManager;
import appeng.container.sync.handlers.AEStackInventorySyncHandler;
import appeng.container.sync.handlers.BooleanSyncHandler;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerSuperStockReplenisher extends AEBaseContainer implements IVirtualSlotSource {

    private final TileSuperStockReplenisher tile;

    private int lastUpdated = 1337;

    private final IAEStackInventory configFluids;
    public final AEStackInventorySyncHandler configFluidsSlots;

    private final IAEStackInventory configItems;
    public final AEStackInventorySyncHandler configItemsSlots;

    public final BooleanSyncHandler fullStockModeSync;

    @SideOnly(Side.CLIENT)
    private GuiFCImgButton modeButton;

    public ContainerSuperStockReplenisher(InventoryPlayer ipl, TileSuperStockReplenisher tile) {
        super(ipl, tile);
        this.tile = tile;
        this.configFluids = tile.getConfigFluids();
        this.configItems = tile.getConfigItems();
        final IInventory cell = tile.getCell();

        final SyncManager sm = this.getSyncManager();
        this.configFluidsSlots = sm.root().aeStackInventory("fluidConfig", this.configFluids);
        this.configItemsSlots = sm.root().aeStackInventory("itemConfig", this.configItems);
        this.fullStockModeSync = sm.root().booleanSync("fullstockMode").onClientChange((oldValue, newValue) -> {
            if (this.modeButton != null) {
                this.modeButton.set(newValue ? "fullstockMode" : "normalMode");
            }
        }).onServerChange((oldValue, newValue) -> this.tile.setFullStockMode(newValue));

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

    @SideOnly(Side.CLIENT)
    public void setModeButton(GuiFCImgButton button) {
        this.modeButton = button;
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

        if (this.lastUpdated > 20) {
            this.lastUpdated = 0;
            Map<Integer, IAEStack<?>> tmp = new HashMap<>();

            for (int i = 0; i < this.tile.getInternalFluid().getSlots(); i++) {
                tmp.put(i, this.tile.getInternalFluid().getFluidInSlot(i));
            }

            for (int i = 0; i < this.tile.getInternalInventory().getSizeInventory(); i++) {
                tmp.put(i + 100, AEItemStack.create(this.tile.getInternalInventory().getStackInSlot(i)));
            }

            for (final Object g : this.crafters) {
                if (g instanceof EntityPlayer) {
                    FluidCraft.proxy.netHandler.sendTo(new SPacketSuperStockReplenisherUpdate(tmp), (EntityPlayerMP) g);
                }
            }

            if (Platform.isServer()) this.setFullStockMode(this.tile.isFullStockMode());

        }

        this.lastUpdated++;
    }

    private boolean isConfigurated() {
        for (int i = 0; i < this.configItems.getSizeInventory(); i++)
            if (this.configItems.getAEStackInSlot(i) != null) return true;

        for (int i = 0; i < this.configFluids.getSizeInventory(); i++)
            if (this.configFluids.getAEStackInSlot(i) != null) return true;
        return false;
    }

    @Override
    public boolean isValidForSlot(Slot s, ItemStack is) {
        if (s.slotNumber == 0 && s.getHasStack() && isConfigurated()) {
            long currentBytes = 0;
            long newBytes = -1;

            if (s.getStack().getItem() instanceof IStorageCell isc) currentBytes = isc.getBytesLong(is);

            if (is.getItem() instanceof IStorageCell isc) newBytes = isc.getBytesLong(is);

            if (currentBytes > newBytes) {
                return false;
            }
        }
        return super.isValidForSlot(s, is);
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
    public void updateVirtualSlot(StorageName invName, int slotId, IAEStack<?> aes) {
        if (aes != null && aes.getStackSize() > Integer.MAX_VALUE) aes.setStackSize(Integer.MAX_VALUE);
        switch (invName) {
            case NONE -> {
                this.configFluids.putAEStackInSlot(slotId, aes);
                this.configFluidsSlots.markDirty();
            }
            case CONFIG -> {
                this.configItems.putAEStackInSlot(slotId, aes);
                this.configItemsSlots.markDirty();
            }
        }
    }

    public TileSuperStockReplenisher getTile() {
        return this.tile;
    }

    public boolean isFullStockMode() {
        return this.fullStockModeSync.get();
    }

    public void setFullStockMode(final boolean fullStockMode) {
        this.fullStockModeSync.set(fullStockMode);
    }

    public void markDirty() {
        this.fullStockModeSync.markDirty();
    }
}
