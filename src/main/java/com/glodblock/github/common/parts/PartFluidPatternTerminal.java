package com.glodblock.github.common.parts;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.base.FCFluidEncodeTerminal;
import com.glodblock.github.inventory.gui.GuiType;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class PartFluidPatternTerminal extends FCFluidEncodeTerminal {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    public PartFluidPatternTerminal(ItemStack is) {
        super(is);
        this.crafting = new BiggerAppEngInventory(this, 9);
        this.output = new BiggerAppEngInventory(this, 3);
        this.prioritize = false;
        this.inverted = false;
        this.activePage = 0;
    }

    @Override
    public GuiType getGui() {
        return GuiType.FLUID_PATTERN_TERMINAL;
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (is != null && is.getItem() instanceof ICraftingPatternItem) {
                final ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
                final ICraftingPatternDetails details = pattern
                        .getPatternForItem(is, this.getHost().getTile().getWorldObj());
                if (details != null) {
                    final IAEItemStack[] inItems = details.getInputs();
                    final IAEItemStack[] outItems = details.getOutputs();

                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());
                    if (newStack != null) {
                        NBTTagCompound data = newStack.getTagCompound();
                        this.setCombineMode(data.getInteger("combine") == 1);
                        this.setBeSubstitute(details.canBeSubstitute());
                    }
                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.output.getSizeInventory(); i++) {
                        this.output.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.crafting.getSizeInventory() && i < inItems.length; i++) {
                        if (inItems[i] != null) {
                            final IAEItemStack item = inItems[i];
                            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.crafting.setInventorySlotContents(i, packet);
                            } else this.crafting.setInventorySlotContents(i, item == null ? null : item.getItemStack());
                        }
                    }

                    for (int i = 0; i < this.output.getSizeInventory() && i < outItems.length; i++) {
                        if (outItems[i] != null) {
                            final IAEItemStack item = outItems[i];
                            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.output.setInventorySlotContents(i, packet);
                            } else this.output.setInventorySlotContents(i, item == null ? null : item.getItemStack());
                        }
                    }
                }
            }
        }
        if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }
        this.getHost().markForSave();
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.getSizeInventory(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
    }

    public void setCraftingRecipe(final boolean craftingMode) {
        super.setCraftingRecipe(craftingMode);
        this.fixCraftingRecipes();
    }

    @Override
    public void setPrioritization(boolean canPrioritize) {
        this.prioritize = false;
    }

    @Override
    public boolean isPrioritize() {
        return false;
    }

    @Override
    public boolean isInverted() {
        return false;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = false;
    }

    @Override
    public int getActivePage() {
        return 0;
    }

    @Override
    public void setActivePage(int activePage) {
        this.activePage = 0;
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }
}
