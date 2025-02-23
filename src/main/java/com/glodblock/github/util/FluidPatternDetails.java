package com.glodblock.github.util;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.PatternHelper;
import appeng.util.item.AEItemStack;

public class FluidPatternDetails implements ICraftingPatternDetails, Comparable<FluidPatternDetails> {

    private final ItemStack patternStack;
    private IAEItemStack patternStackAe;
    private IAEItemStack[] inputs = null, inputsCond = null, outputs = null, outputsCond = null;
    private int priority = 0;
    private int combine = 0;
    private int beSubstitute = 0;

    public FluidPatternDetails(ItemStack stack) {
        this.patternStack = stack;
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("author")) {
            final ItemStack forComparison = this.patternStack.copy();
            forComparison.stackTagCompound.removeTag("author");
            this.patternStackAe = Objects.requireNonNull(AEItemStack.create(forComparison)); // s2g
        } else {
            this.patternStackAe = Objects.requireNonNull(AEItemStack.create(stack)); // s2g
        }
    }

    public boolean canBeSubstitute() {
        return beSubstitute != 0;
    }

    public void setCanBeSubstitute(int beSubstitute) {
        this.beSubstitute = beSubstitute;
    }

    @Override
    public ItemStack getPattern() {
        return patternStack;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCombine(int combine) {
        this.combine = combine;
    }

    public int getCombine() {
        return combine;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return checkInitialized(inputs);
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return checkInitialized(inputsCond);
    }

    public boolean setInputs(IAEItemStack[] inputs) {
        IAEItemStack[] condensed = condenseStacks(inputs);
        if (condensed.length == 0) {
            return false;
        }
        this.inputs = inputs;
        this.inputsCond = condensed;
        return true;
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return checkInitialized(outputs);
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return checkInitialized(outputsCond);
    }

    public boolean setOutputs(IAEItemStack[] outputs) {
        IAEItemStack[] condensed = condenseStacks(outputs);
        if (condensed.length == 0) {
            return false;
        }
        this.outputs = Arrays.stream(outputs).filter(Objects::nonNull).toArray(IAEItemStack[]::new);
        this.outputsCond = condensed;
        return true;
    }

    private static IAEItemStack[] condenseStacks(IAEItemStack[] stacks) {
        return PatternHelper.convertToCondensedList(stacks);
    }

    @Override
    public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    @Override
    public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    private static <T> T checkInitialized(@Nullable T value) {
        if (value == null) {
            throw new IllegalStateException("Pattern is not initialized!");
        }
        return value;
    }

    @Override
    public int hashCode() {
        return patternStackAe.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // ae2 null-checks the pattern stack here for some reason, but doesn't null-check in hashCode()
        // this is inconsistent, so I've just decided to assert non-null in the constructor, which is to say that
        // the pattern stack can never be null here
        return obj instanceof FluidPatternDetails && patternStackAe.equals(((FluidPatternDetails) obj).patternStackAe);
    }

    @Override
    public int compareTo(FluidPatternDetails o) {
        return Integer.compare(o.priority, this.priority);
    }

    public ItemStack writeToStack() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Inputs", writeStackArray(checkInitialized(inputs)));
        tag.setTag("Outputs", writeStackArray(checkInitialized(outputs)));
        // Shits
        tag.setTag("in", writeStackArray(checkInitialized(inputs)));
        tag.setTag("out", writeStackArray(checkInitialized(outputs)));
        tag.setInteger("combine", this.getCombine());
        tag.setBoolean("beSubstitute", this.canBeSubstitute());
        patternStack.setTagCompound(tag);
        patternStackAe = Objects.requireNonNull(AEItemStack.create(patternStack));
        return patternStack;
    }

    public static NBTTagList writeStackArray(IAEItemStack[] stacks) {
        NBTTagList listTag = new NBTTagList();
        for (IAEItemStack stack : stacks) {
            // see note at top of class
            NBTTagCompound stackTag = new NBTTagCompound();
            if (stack != null) stack.writeToNBT(stackTag);
            listTag.appendTag(stackTag);
        }
        return listTag;
    }

    public boolean readFromStack() {
        if (!patternStack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = Objects.requireNonNull(patternStack.getTagCompound());
        setCanBeSubstitute(tag.getBoolean("beSubstitute") ? 1 : 0);
        setCombine(tag.getInteger("combine"));

        // may be possible to enter a partially-correct state if setInputs succeeds but setOutputs failed
        // but outside code should treat it as completely incorrect and not attempt to make calls
        return setInputs(readStackArray(tag.getTagList("in", Constants.NBT.TAG_COMPOUND)))
                && setOutputs(readStackArray(tag.getTagList("out", Constants.NBT.TAG_COMPOUND)));
    }

    public static IAEItemStack[] readStackArray(NBTTagList listTag) {
        // see note at top of class
        IAEItemStack[] stacks = new IAEItemStack[listTag.tagCount()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = AEItemStack.loadItemStackFromNBT(listTag.getCompoundTagAt(i));
        }
        return stacks;
    }
}
