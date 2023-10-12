package com.glodblock.github.client.gui.container.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.client.gui.container.ContainerItemMonitor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.IPatternConsumer;
import com.glodblock.github.inventory.item.IItemPatternTerminal;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public abstract class FCContainerEncodeTerminal extends ContainerItemMonitor
        implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket, IPatternConsumer {

    protected final IItemPatternTerminal patternTerminal;
    protected final AppEngInternalInventory cOut = new AppEngInternalInventory(null, 1);
    protected final IInventory crafting;
    protected final SlotRestrictedInput patternSlotIN;
    protected final SlotRestrictedInput patternSlotOUT;
    protected SlotFake[] craftingSlots;
    protected OptionalSlotFake[] outputSlots;
    protected SlotPatternTerm craftSlot;

    @GuiSync(97)
    public boolean craftingMode = true;

    @GuiSync(96)
    public boolean substitute = false;

    @GuiSync(95)
    public boolean combine = false;

    @GuiSync(94)
    public boolean beSubstitute = false;

    @GuiSync(93)
    public boolean inverted;

    @GuiSync(92)
    public int activePage = 0;

    @GuiSync(91)
    public boolean prioritize = false;

    @GuiSync(90)
    public boolean autoFillPattern = false;

    public FCContainerEncodeTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        this.patternTerminal = (IItemPatternTerminal) monitorable;
        this.inverted = patternTerminal.isInverted();
        final IInventory patternInv = this.patternTerminal.getInventoryByName("pattern");
        this.crafting = this.patternTerminal.getInventoryByName("crafting");
        this.addSlotToContainer(
                this.patternSlotIN = new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.BLANK_PATTERN,
                        patternInv,
                        0,
                        147,
                        -72 - 9,
                        this.getInventoryPlayer()));
        this.addSlotToContainer(
                this.patternSlotOUT = new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                        patternInv,
                        1,
                        147,
                        -72 + 34,
                        this.getInventoryPlayer()));
        this.patternSlotOUT.setStackLimit(1);
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        if (this.isCraftingMode()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (action == InventoryAction.MOVE_REGION) {
            super.doAction(player, InventoryAction.MOVE_REGION, slotId, id);
            return;
        }
        if (action == InventoryAction.PICKUP_SINGLE) {
            super.doAction(player, InventoryAction.PICKUP_OR_SET_DOWN, slotId, id);
            return;
        }
        Slot slot = getSlot(slotId);
        ItemStack stack = player.inventory.getItemStack();
        if (Util.getFluidFromItem(stack) == null || Util.getFluidFromItem(stack).amount <= 0) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (validPatternSlot(slot)
                && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
            FluidStack fluid = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN -> {
                    fluid = Util.getFluidFromItem(stack);
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                }
                case SPLIT_OR_PLACE_SINGLE -> {
                    fluid = Util.getFluidFromItem(Util.copyStackWithSize(stack, 1));
                    FluidStack origin = ItemFluidPacket.getFluidStack(slot.getStack());
                    if (fluid != null && fluid.equals(origin)) {
                        fluid.amount += origin.amount;
                        if (fluid.amount <= 0) fluid = null;
                    }
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                }
            }
            if (fluid == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        super.doAction(player, action, slotId, id);
    }

    protected abstract boolean validPatternSlot(Slot slot);

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int idx) {
        Slot clickSlot = (Slot) this.inventorySlots.get(idx);
        ItemStack is = clickSlot.getStack();
        if (is != null && !patternSlotOUT.getHasStack()
                && is.stackSize == 1
                && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemEncodedPattern)) {
            ItemStack output = is.copy();
            patternSlotOUT.putStack(output);
            p.inventory.setInventorySlotContents(clickSlot.getSlotIndex(), null);
            this.detectAndSendChanges();
            return null;
        } else {
            return super.transferStackInSlot(p, idx);
        }
    }

    public IItemPatternTerminal getPatternTerminal() {
        return this.patternTerminal;
    }

    protected static boolean canDoubleStacks(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long emptySlots = enabledSlots.stream().filter(s -> s.getStack() == null).count();
        long fullSlots = enabledSlots.stream().filter(s -> s.getStack() != null && s.getStack().stackSize * 2 > 127)
                .count();
        return fullSlots <= emptySlots;
    }

    protected static void doubleStacksInternal(SlotFake[] slots) {
        List<ItemStack> overFlowStacks = new ArrayList<>();
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        for (final Slot s : enabledSlots) {
            ItemStack st = s.getStack();
            if (st == null) continue;
            if (Util.isFluidPacket(st)) {
                FluidStack fluidStack = ItemFluidPacket.getFluidStack(st);
                if (fluidStack != null) {
                    fluidStack = ItemFluidPacket.getFluidStack(st).copy();
                    if (fluidStack.amount < Integer.MAX_VALUE / 2) fluidStack.amount *= 2;
                }
                s.putStack(ItemFluidPacket.newStack(fluidStack));
            } else if (st.stackSize * 2 > 127) {
                overFlowStacks.add(st.copy());
            } else {
                st.stackSize *= 2;
                s.putStack(st);
            }
        }
        Iterator<ItemStack> ow = overFlowStacks.iterator();
        for (final Slot s : enabledSlots) {
            if (!ow.hasNext()) break;
            if (s.getStack() != null) continue;
            s.putStack(ow.next());
        }
        assert !ow.hasNext();
    }

    protected static boolean containsItem(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long item = enabledSlots.stream().filter(s -> s.getStack() != null && !Util.isFluidPacket(s.getStack()))
                .count();
        return item > 0;
    }

    protected static boolean containsFluid(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long fluid = enabledSlots.stream().filter(s -> Util.isFluidPacket(s.getStack())).count();
        return fluid > 0;
    }

    protected static boolean nonNullSlot(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long object = enabledSlots.stream().filter(s -> s.getStack() != null).count();
        return object > 0;
    }

    protected boolean checkHasFluidPattern() {
        if (this.craftingMode) {
            return false;
        }
        boolean hasFluid = containsFluid(this.craftingSlots);
        boolean search = nonNullSlot(this.craftingSlots);
        if (!search) { // search=false -> inputs were empty
            return false;
        }
        hasFluid |= containsFluid(this.outputSlots);
        search = nonNullSlot(this.outputSlots);
        return hasFluid && search; // search=false -> outputs were empty
    }

    protected ItemStack stampAuthor(ItemStack patternStack) {
        if (patternStack.stackTagCompound == null) {
            patternStack.stackTagCompound = new NBTTagCompound();
        }
        patternStack.stackTagCompound.setString("author", getPlayerInv().player.getCommandSenderName());
        return patternStack;
    }

    protected void encodeFluidPattern() {
        ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
        FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(this.craftingSlots));
        pattern.setOutputs(collectInventory(this.outputSlots));
        pattern.setCanBeSubstitute(this.beSubstitute ? 1 : 0);
        patternSlotOUT.putStack(stampAuthor(pattern.writeToStack()));
    }

    protected static IAEItemStack[] collectInventory(Slot[] slots) {
        IAEItemStack[] stacks = new IAEItemStack[slots.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = slots[i].getStack();
            if (stack != null) {
                if (stack.getItem() instanceof ItemFluidPacket) {
                    IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
                    if (dropStack != null) {
                        stacks[i] = dropStack;
                        continue;
                    }
                }
            }
            IAEItemStack aeStack = AEItemStack.create(stack);
            stacks[i] = aeStack;
        }
        return stacks;
    }

    @Override
    public void saveChanges() {
        // NO-OP
    }

    public void clear() {
        for (final Slot s : this.craftingSlots) {
            s.putStack(null);
        }
        for (final Slot s : this.outputSlots) {
            s.putStack(null);
        }
        this.detectAndSendChanges();
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        // NO-OP
    }

    public void encodeAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (!getPlayerInv().addItemStackToInventory(output)) {
                getPlayerInv().player.entityDropItem(output, 0);
            }
            this.patternSlotOUT.putStack(null);
        }
        fillPattern();
    }

    public void encodeAllItemAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (this.patternSlotIN.getStack() != null) output.stackSize += this.patternSlotIN.getStack().stackSize;
            if (!getPlayerInv().addItemStackToInventory(output)) {
                getPlayerInv().player.entityDropItem(output, 0);
            }
            this.patternSlotOUT.putStack(null);
            this.patternSlotIN.putStack(null);
        }
        fillPattern();
    }

    private void fillPattern() {
        if (this.autoFillPattern && this.getHost().getItemInventory() != null) {
            // try to use me network item to fill pattern input slot
            final IDefinitions definitions = AEApi.instance().definitions();
            int fillStackSize = this.patternSlotIN.getHasStack() ? 64 - this.patternSlotIN.getStack().stackSize : 64;
            if (fillStackSize == 0) return;
            for (ItemStack blankPattern : definitions.materials().blankPattern().maybeStack(fillStackSize).asSet()) {
                IAEItemStack iBlankPattern = AEApi.instance().storage().createItemStack(blankPattern);
                if (this.patternSlotIN.getHasStack() && !iBlankPattern.isSameType(this.patternSlotIN.getStack()))
                    continue;
                IAEItemStack out = this.getHost().getItemInventory()
                        .extractItems(iBlankPattern, Actionable.MODULATE, this.getActionSource());
                if (out != null) {
                    ItemStack outPattern;
                    if (this.patternSlotIN.getHasStack()) {
                        outPattern = this.patternSlotIN.getStack().copy();
                        outPattern.stackSize += out.getItemStack().stackSize;
                    } else {
                        outPattern = out.getItemStack();
                    }
                    this.patternSlotIN.putStack(outPattern);
                    return;
                }
            }
        }
    }

    public void encode() {
        fillPattern();
        if (!checkHasFluidPattern()) {
            encodeItemPattern();
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack == null) {
            stack = this.patternSlotIN.getStack();
            if (notPattern(stack)) {
                return;
            }
            if (stack.stackSize == 1) {
                this.patternSlotIN.putStack(null);
            } else {
                stack.stackSize--;
            }
            encodeFluidPattern();
        } else if (!notPattern(stack)) {
            encodeFluidPattern();
        }
    }

    public void encodeItemPattern() {
        ItemStack output = this.patternSlotOUT.getStack();
        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if (in == null || out == null) {
            return;
        }
        // first check the output slots, should either be null, or a pattern
        if (output != null && this.notPattern(output)) {
            return;
        } // if nothing is there we should snag a new pattern.
        else if (output == null) {
            output = this.patternSlotIN.getStack();
            if (this.notPattern(output)) {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.stackSize--;
            if (output.stackSize == 0) {
                this.patternSlotIN.putStack(null);
            }

            // add a new encoded pattern.
            for (final ItemStack encodedPatternStack : AEApi.instance().definitions().items().encodedPattern()
                    .maybeStack(1).asSet()) {
                output = encodedPatternStack;
            }
        } else if (output.getItem() instanceof ItemFluidEncodedPattern) {
            for (final ItemStack encodedPatternStack : AEApi.instance().definitions().items().encodedPattern()
                    .maybeStack(1).asSet()) {
                output = encodedPatternStack;
            }
        }

        // encode the slot.
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for (final ItemStack i : in) {
            tagIn.appendTag(this.createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.appendTag(this.createItemTag(i));
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setBoolean("crafting", this.craftingMode);
        encodedValue.setBoolean("substitute", this.substitute);
        encodedValue.setBoolean("beSubstitute", this.beSubstitute);
        encodedValue.setBoolean("prioritize", this.prioritize);
        output.setTagCompound(encodedValue);
        stampAuthor(output);
        this.patternSlotOUT.putStack(output);
    }

    protected ItemStack[] getInputs() {
        final ArrayList<ItemStack> input = new ArrayList<>();
        for (SlotFake craftingSlot : this.craftingSlots) {
            input.add(craftingSlot.getStack());
        }
        if (input.stream().anyMatch(Objects::nonNull)) {
            return input.toArray(new ItemStack[0]);
        }
        return null;
    }

    protected ItemStack[] getOutputs() {
        final ArrayList<ItemStack> output = new ArrayList<>();
        for (final SlotFake outputSlot : this.outputSlots) {
            output.add(outputSlot.getStack());
        }
        if (output.stream().anyMatch(Objects::nonNull)) {
            return output.toArray(new ItemStack[0]);
        }
        return null;
    }

    protected boolean notPattern(final ItemStack output) {
        if (output == null) {
            return true;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return false;
        }
        final IDefinitions definitions = AEApi.instance().definitions();

        boolean isPattern = definitions.items().encodedPattern().isSameAs(output);
        isPattern |= definitions.materials().blankPattern().isSameAs(output);

        return !isPattern;
    }

    protected NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (i != null) {
            i.writeToNBT(c);
            c.setInteger("Count", i.stackSize);
        }
        return c;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            this.substitute = this.patternTerminal.isSubstitution();
            this.combine = this.patternTerminal.shouldCombine();
            this.beSubstitute = this.patternTerminal.canBeSubstitute();
            this.prioritize = this.patternTerminal.isPrioritize();
            this.autoFillPattern = this.patternTerminal.isAutoFillPattern();
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.patternSlotOUT && Platform.isServer()) {
            for (final Object crafter : this.crafters) {
                final ICrafting icrafting = (ICrafting) crafter;

                for (final Object g : this.inventorySlots) {
                    if (g instanceof OptionalSlotFake || g instanceof SlotFakeCraftingMatrix) {
                        final Slot sri = (Slot) g;
                        icrafting.sendSlotContents(this, sri.slotNumber, sri.getStack());
                    }
                }
                ((EntityPlayerMP) icrafting).isChangingQuantityOnly = false;
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("player")) {
            return this.getInventoryPlayer();
        }
        return this.patternTerminal.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    static boolean canDouble(SlotFake[] slots, int mult) {
        for (Slot s : slots) {
            ItemStack st = s.getStack();
            if (st != null) {
                if (st.getItem() instanceof ItemFluidPacket) {
                    long result = (long) ItemFluidPacket.getFluidAmount(st) * mult;
                    if (result > Integer.MAX_VALUE) {
                        return false;
                    }
                } else {
                    long result = (long) s.getStack().stackSize * mult;
                    if (result > Integer.MAX_VALUE) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static void doubleStacksInternal(SlotFake[] slots, int mult) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        for (final Slot s : enabledSlots) {
            ItemStack st = s.getStack();
            if (st != null) {
                if (st.getItem() instanceof ItemFluidPacket) {
                    ItemFluidPacket.setFluidAmount(st, ItemFluidPacket.getFluidAmount(st) * mult);
                } else {
                    st.stackSize *= mult;
                }
            }
        }
    }

    public void doubleStacks(boolean isShift) {
        if (!isCraftingMode()) {
            if (isShift) {
                if (canDouble(this.craftingSlots, 8) && canDouble(this.outputSlots, 8)) {
                    doubleStacksInternal(this.craftingSlots, 8);
                    doubleStacksInternal(this.outputSlots, 8);
                }
            } else {
                if (canDouble(this.craftingSlots, 2) && canDouble(this.outputSlots, 2)) {
                    doubleStacksInternal(this.craftingSlots, 2);
                    doubleStacksInternal(this.outputSlots, 2);
                }
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        if (this.patternTerminal != null) {
            this.patternTerminal.onChangeCrafting(inputs, outputs);
        }
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }
}
