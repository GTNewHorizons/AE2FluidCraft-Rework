package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.glodblock.github.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.inventory.item.IWirelessCraftTerminal;
import com.glodblock.github.inventory.item.IWirelessTerminal;

import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerCraftingWireless extends ContainerItemMonitor
        implements IAEAppEngInventory, IContainerCraftingPacket {

    private final IWirelessCraftTerminal it;
    private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
    private final SlotCraftingTerm outputSlot;

    public ContainerCraftingWireless(final InventoryPlayer ip, final IWirelessTerminal monitorable) {
        super(ip, monitorable, false);
        this.it = (IWirelessCraftTerminal) monitorable;
        final IInventory crafting = this.it.getInventoryByName("crafting");
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(
                        this.craftingSlots[x + y * 3] = new SlotCraftingMatrix(
                                this,
                                crafting,
                                x + y * 3,
                                37 + x * 18,
                                -72 + y * 18) {

                            @Override
                            public void onSlotChanged() {
                                if (this.inventory instanceof ItemBiggerAppEngInventory i) {
                                    i.markDirty();
                                }
                                super.onSlotChanged();
                            }
                        });
            }
        }
        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        this.addSlotToContainer(
                this.outputSlot = new SlotCraftingTerm(
                        this.getPlayerInv().player,
                        this.getActionSource(),
                        this.getPowerSource(),
                        monitorable,
                        crafting,
                        crafting,
                        output,
                        131,
                        -72 + 18,
                        this));
        this.bindPlayerInventory(ip, 0, 0);
        this.onCraftMatrixChanged(crafting);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }

    @Override
    public void onCraftMatrixChanged(final IInventory par1IInventory) {
        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting ic = new InventoryCrafting(cn, 3, 3);
        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }
        this.outputSlot
                .putStack(CraftingManager.getInstance().findMatchingRecipe(ic, this.getPlayerInv().player.worldObj));
    }

    @Override
    public void saveChanges() {
        // NO-OP
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        // NO-OP
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("player")) {
            return this.getInventoryPlayer();
        }
        return this.it.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

}
