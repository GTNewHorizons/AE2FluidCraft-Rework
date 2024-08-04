package com.glodblock.github.client.gui.container;

import java.util.Iterator;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.client.gui.container.base.FCContainerFluidConfigurable;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFakeTypeOnly;
import com.glodblock.github.util.Ae2Reflect;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.IterationCounter;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;

public class ContainerFluidStorageBus extends FCContainerFluidConfigurable {

    public final PartFluidStorageBus bus;

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public ContainerFluidStorageBus(InventoryPlayer ip, PartFluidStorageBus te) {
        super(ip, te);
        this.bus = te;
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    protected void setupConfig() {
        final int xo = 8;
        final int yo = 23 + 6;
        final IInventory upgrades = Ae2Reflect.getUpgradeList(this).getInventoryByName("upgrades");
        final IInventory config = Ae2Reflect.getUpgradeList(this).getInventoryByName("config");
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(
                        new OptionalFluidSlotFakeTypeOnly(config, null, this, y * 9 + x, xo, yo, x, y, y));
            }
        }
        this.addSlotToContainer(
                (new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.UPGRADES,
                        upgrades,
                        0,
                        187,
                        8,
                        this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.UPGRADES,
                        upgrades,
                        1,
                        187,
                        8 + 18,
                        this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.UPGRADES,
                        upgrades,
                        2,
                        187,
                        8 + 18 * 2,
                        this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.UPGRADES,
                        upgrades,
                        3,
                        187,
                        8 + 18 * 3,
                        this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.UPGRADES,
                        upgrades,
                        4,
                        187,
                        8 + 18 * 4,
                        this.getInventoryPlayer())).setNotDraggable());
    }

    @Override
    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            final int upgrades = Ae2Reflect.getUpgradeList(this).getInstalledUpgrades(Upgrades.CAPACITY);
            final int y = slot / 9;
            return y < upgrades + 2;
        }
        return true;
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.setReadWriteMode(
                    (AccessRestriction) Ae2Reflect.getUpgradeList(this).getConfigManager().getSetting(Settings.ACCESS));
            this.setStorageFilter(
                    (StorageFilter) Ae2Reflect.getUpgradeList(this).getConfigManager()
                            .getSetting(Settings.STORAGE_FILTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        if (Ae2Reflect.getUpgradeList(this).getInstalledUpgrades(Upgrades.ORE_FILTER) > 0) return false;
        final int upgrades = Ae2Reflect.getUpgradeList(this).getInstalledUpgrades(Upgrades.CAPACITY);
        return upgrades > (idx - 2);
    }

    public void clear() {
        AppEngInternalAEInventory h = this.bus.getConfig();
        for (int i = 0; i < h.getSizeInventory(); ++i) {
            h.setInventorySlotContents(i, null);
        }
        this.detectAndSendChanges();
    }

    public void partition() {
        AppEngInternalAEInventory h = this.bus.getConfig();
        final IMEInventory<IAEFluidStack> cellInv = this.bus.getInternalHandler();
        Iterator<IAEFluidStack> i = new NullIterator<>();
        if (cellInv != null) {
            final IItemList<IAEFluidStack> list = cellInv
                    .getAvailableItems(AEApi.instance().storage().createFluidList(), IterationCounter.fetchNewId());
            i = list.iterator();
        }

        for (int x = 0; x < h.getSizeInventory(); x++) {
            if (i.hasNext() && this.isSlotEnabled((x / 9) - 2)) {
                h.setInventorySlotContents(x, ItemFluidPacket.newStack(i.next()));
            } else {
                h.setInventorySlotContents(x, null);
            }
        }
        this.detectAndSendChanges();
    }

    public AccessRestriction getReadWriteMode() {
        return this.rwMode;
    }

    public StorageFilter getStorageFilter() {
        return this.storageFilter;
    }

    @Override
    public AppEngInternalAEInventory getFakeFluidInv() {
        return (AppEngInternalAEInventory) this.bus.getInventoryByName("config");
    }

    private void setReadWriteMode(final AccessRestriction rwMode) {
        this.rwMode = rwMode;
    }

    private void setStorageFilter(final StorageFilter storageFilter) {
        this.storageFilter = storageFilter;
    }
}
