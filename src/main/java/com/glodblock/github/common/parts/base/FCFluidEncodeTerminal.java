package com.glodblock.github.common.parts.base;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.item.IItemPatternTerminal;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalInventory;

public abstract class FCFluidEncodeTerminal extends FCPart implements IItemPatternTerminal {

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;
    protected final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 2);

    protected boolean craftingMode = true;
    protected boolean substitute = false;
    protected boolean combine = false;
    protected boolean prioritize = false;
    protected boolean inverted = false;
    protected boolean beSubstitute = false;
    protected boolean autoFillPattern = false;
    protected int activePage = 0;

    public FCFluidEncodeTerminal(ItemStack is) {
        super(is, true);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        super.getDrops(drops, wrenched);
        for (final ItemStack is : this.pattern) {
            if (is != null) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        this.setCombineMode(data.getBoolean("combine"));
        this.setBeSubstitute(data.getBoolean("beSubstitute"));
        this.setPrioritization(data.getBoolean("priorization"));
        this.setInverted(data.getBoolean("inverted"));
        this.setActivePage(data.getInteger("activePage"));
        this.setAutoFillPattern(data.getBoolean("autoFillPattern"));
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("craftingMode", this.craftingMode);
        data.setBoolean("combine", this.combine);
        data.setBoolean("beSubstitute", this.beSubstitute);
        data.setBoolean("priorization", this.prioritize);
        data.setBoolean("substitute", this.substitute);
        data.setBoolean("inverted", this.inverted);
        data.setInteger("activePage", this.activePage);
        data.setBoolean("autoFillPattern", this.autoFillPattern);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "craftingGrid");
    }

    @Override
    public boolean shouldCombine() {
        return this.combine;
    }

    @Override
    public void setCombineMode(boolean shouldCombine) {
        this.combine = shouldCombine;
    }

    @Override
    public boolean isSubstitution() {
        return this.substitute;
    }

    @Override
    public boolean isPrioritize() {
        return this.prioritize;
    }

    @Override
    public void setBeSubstitute(boolean canBeSubstitute) {
        this.beSubstitute = canBeSubstitute;
    }

    @Override
    public boolean canBeSubstitute() {
        return this.beSubstitute;
    }

    @Override
    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Override
    public void setPrioritization(boolean canPrioritize) {
        this.prioritize = canPrioritize;
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public int getActivePage() {
        return this.activePage;
    }

    @Override
    public void setActivePage(int activePage) {
        this.activePage = activePage;
    }

    @Override
    public void setCraftingRecipe(final boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    @Override
    public boolean isCraftingRecipe() {
        return this.craftingMode;
    }

    @Override
    public void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput) {
        IInventory crafting = this.getInventoryByName("crafting");
        IInventory output = this.getInventoryByName("output");
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            for (int x = 0; x < crafting.getSizeInventory() && x < newCrafting.length; x++) {
                final IAEItemStack item = newCrafting[x];
                crafting.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
            for (int x = 0; x < output.getSizeInventory() && x < newOutput.length; x++) {
                final IAEItemStack item = newOutput[x];
                output.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
        }
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        if (name.equals("pattern")) {
            return this.pattern;
        }

        return super.getInventoryByName(name);
    }

    @Override
    public void sortCraftingItems() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        List<ItemStack> fluids = new ArrayList<ItemStack>();
        for (ItemStack is : this.crafting) {
            if (is == null) continue;
            if (is.getItem() instanceof ItemFluidPacket) {
                fluids.add(is);
            } else {
                items.add(is);
            }
        }
        if (this.prioritize) {
            fluids.addAll(items);
            items.clear();
        } else {
            items.addAll(fluids);
            fluids.clear();
        }

        for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
            if (this.crafting.getStackInSlot(i) == null) break;
            if (items.isEmpty()) {
                this.crafting.setInventorySlotContents(i, fluids.get(i));
            } else {
                this.crafting.setInventorySlotContents(i, items.get(i));
            }
        }
        saveChanges();
    }

    @Override
    public boolean isAutoFillPattern() {
        return this.autoFillPattern;
    }

    @Override
    public void setAutoFillPattern(boolean canFill) {
        this.autoFillPattern = canFill;
    }
}
