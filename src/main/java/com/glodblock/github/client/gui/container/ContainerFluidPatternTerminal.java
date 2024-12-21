package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;

import appeng.api.storage.ITerminalHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.util.Platform;

public class ContainerFluidPatternTerminal extends FCContainerEncodeTerminal {

    public ContainerFluidPatternTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        final IInventory output = this.patternTerminal.getInventoryByName("output");
        final IInventory patternInv = this.patternTerminal.getInventoryByName("pattern");
        this.craftingSlots = new SlotFakeCraftingMatrix[9];
        this.outputSlots = new OptionalSlotFake[3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(
                        this.craftingSlots[x + y * 3] = new SlotFakeCraftingMatrix(
                                this.crafting,
                                x + y * 3,
                                18 + x * 18,
                                -76 + y * 18));
            }
        }

        this.addSlotToContainer(
                this.craftSlot = new SlotPatternTerm(
                        ip.player,
                        this.getActionSource(),
                        this.getPowerSource(),
                        monitorable,
                        this.crafting,
                        patternInv,
                        this.cOut,
                        110,
                        -76 + 18,
                        this,
                        2,
                        this));
        this.craftSlot.setIIcon(-1);

        for (int y = 0; y < 3; y++) {
            this.addSlotToContainer(
                    this.outputSlots[y] = new SlotPatternOutputs(output, this, y, 110, -76 + y * 18, 0, 0, 1));
            this.outputSlots[y].setRenderDisabled(false);
        }
        this.updateOrderOfOutputSlots();
    }

    private void updateOrderOfOutputSlots() {
        if (!this.isCraftingMode()) {
            this.craftSlot.xDisplayPosition = -9000;
            for (int y = 0; y < 3; y++) {
                this.outputSlots[y].xDisplayPosition = this.outputSlots[y].getX();
            }
        } else {
            this.craftSlot.xDisplayPosition = this.craftSlot.getX();
            for (int y = 0; y < 3; y++) {
                this.outputSlots[y].xDisplayPosition = -9000;
            }
        }
    }

    protected boolean validPatternSlot(Slot slot) {
        return slot instanceof SlotFakeCraftingMatrix || slot instanceof SlotPatternOutputs;
    }

    @Override
    public void putStackInSlot(final int par1, final ItemStack par2ItemStack) {
        super.putStackInSlot(par1, par2ItemStack);
        this.getAndUpdateOutput();
    }

    @Override
    public void putStacksInSlots(final ItemStack[] par1ArrayOfItemStack) {
        super.putStacksInSlots(par1ArrayOfItemStack);
        this.getAndUpdateOutput();
    }

    private ItemStack getAndUpdateOutput() {
        final InventoryCrafting ic = new InventoryCrafting(this, 3, 3);

        for (int x = 0; x < ic.getSizeInventory(); x++) {
            ic.setInventorySlotContents(x, this.crafting.getStackInSlot(x));
        }

        final ItemStack is = CraftingManager.getInstance().findMatchingRecipe(ic, this.getPlayerInv().player.worldObj);
        this.cOut.setInventorySlotContents(0, is);
        return is;
    }

    @Override
    protected ItemStack[] getOutputs() {
        if (this.isCraftingMode()) {
            final ItemStack out = this.getAndUpdateOutput();
            if (out != null && out.stackSize > 0) {
                return new ItemStack[] { out };
            }
        }
        return super.getOutputs();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        if (idx == 1) {
            return Platform.isServer() ? !this.patternTerminal.isCraftingRecipe() : !this.isCraftingMode();
        } else if (idx == 2) {
            return Platform.isServer() ? this.patternTerminal.isCraftingRecipe() : this.isCraftingMode();
        } else {
            return false;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            if (this.isCraftingMode() != this.patternTerminal.isCraftingRecipe()) {
                this.setCraftingMode(this.patternTerminal.isCraftingRecipe());
                this.updateOrderOfOutputSlots();
            }
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (field.equals("craftingMode")) {
            this.getAndUpdateOutput();
            this.updateOrderOfOutputSlots();
        }
    }

    public void clear() {
        super.clear();
        this.getAndUpdateOutput();
    }

    private void setCraftingMode(final boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    @Override
    public void doubleStacks(boolean isShift, boolean divide) {
        if (!isCraftingMode()) {
            super.doubleStacks(isShift, divide);
        }
    }
}
