package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.tile.TileSuperStoker;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFake;
import com.glodblock.github.network.SPacketSuperStokerUpdate;

import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.storage.ItemBasicStorageCell;

public class ContainerSuperStoker extends AEBaseContainer implements IOptionalSlotHost {

    private final TileSuperStoker tile;
    private final IInventory configFluids;
    private final IInventory configItems;
    private int lastUpdated = 0;

    public ContainerSuperStoker(InventoryPlayer ipl, TileSuperStoker tile) {
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
                tmp.put(i + 100, tile.getInternalAEInventory().getAEStackInSlot(i));
            }

            for (final Object g : this.crafters) {
                if (g instanceof EntityPlayer) {
                    FluidCraft.proxy.netHandler.sendTo(new SPacketSuperStokerUpdate(tmp), (EntityPlayerMP) g);
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

    public TileSuperStoker getTile() {
        return tile;
    }
}
