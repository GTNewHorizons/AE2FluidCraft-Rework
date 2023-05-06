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

public class PartFluidPatternTerminalEx extends FCFluidEncodeTerminal {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    public PartFluidPatternTerminalEx(ItemStack is) {
        super(is);
        this.crafting = new BiggerAppEngInventory(this, 32);
        this.output = new BiggerAppEngInventory(this, 32);
        this.craftingMode = false;
    }

    @Override
    public GuiType getGui() {
        return GuiType.FLUID_PATTERN_TERMINAL_EX;
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
                    int inputsCount = 0;
                    int outputCount = 0;
                    for (IAEItemStack inItem : inItems) {
                        if (inItem != null) {
                            inputsCount++;
                        }
                    }
                    for (IAEItemStack outItem : outItems) {
                        if (outItem != null) {
                            outputCount++;
                        }
                    }

                    this.setSubstitution(details.canSubstitute());
                    if (newStack != null) {
                        NBTTagCompound data = newStack.getTagCompound();
                        this.setCombineMode(data.getInteger("combine") == 1);
                        this.setBeSubstitute(details.canBeSubstitute());
                    }
                    this.setInverted(inputsCount <= 8 && outputCount > 8);
                    this.setActivePage(0);

                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.output.getSizeInventory(); i++) {
                        this.output.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.crafting.getSizeInventory() && i < inItems.length; i++) {
                        final IAEItemStack item = inItems[i];
                        if (item != null) {
                            if (item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.crafting.setInventorySlotContents(i, packet);
                            } else this.crafting.setInventorySlotContents(i, item.getItemStack());
                        }
                    }

                    if (inverted) {
                        for (int i = 0; i < this.output.getSizeInventory() && i < outItems.length; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket
                                            .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.output.setInventorySlotContents(i, packet);
                                } else this.output.setInventorySlotContents(i, item.getItemStack());
                            }
                        }
                    } else {
                        for (int i = 0; i < outItems.length && i < 8; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket
                                            .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.output.setInventorySlotContents(i >= 4 ? 12 + i : i, packet);
                                } else this.output.setInventorySlotContents(i >= 4 ? 12 + i : i, item.getItemStack());
                            }
                        }
                    }
                }
            }
        }
        this.getHost().markForSave();
    }

    @Override
    public void setCraftingRecipe(final boolean craftingMode) {
        this.craftingMode = false;
    }

    @Override
    public boolean isCraftingRecipe() {
        return false;
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
