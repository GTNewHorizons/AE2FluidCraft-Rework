package com.glodblock.github.client.gui.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.IPatternConsumer;
import com.glodblock.github.inventory.slot.SlotFluidConvertingFake;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;

public class ContainerFluidPatternEncoder extends AEBaseContainer implements IPatternConsumer {

    private final TileFluidPatternEncoder tile;

    public ContainerFluidPatternEncoder(InventoryPlayer ipl, TileFluidPatternEncoder tile) {
        super(ipl, tile);
        this.tile = tile;
        AeItemStackHandler crafting = new AeItemStackHandler(tile.getCraftingSlots());
        AeItemStackHandler output = new AeItemStackHandler(tile.getOutputSlots());
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotFluidConvertingFake(crafting, y * 3 + x, 23 + x * 18, 17 + y * 18));
            }
            addSlotToContainer(new SlotFluidConvertingFake(output, y, 113, 17 + y * 18));
        }
        addSlotToContainer(
                new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.BLANK_PATTERN,
                        tile.getInventory(),
                        0,
                        138,
                        20,
                        ipl));
        addSlotToContainer(
                new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                        tile.getInventory(),
                        1,
                        138,
                        50,
                        ipl));
        bindPlayerInventory(ipl, 0, 84);
    }

    public TileFluidPatternEncoder getTile() {
        return tile;
    }

    private static boolean isNotPattern(ItemStack stack) {
        return stack == null || !(AEApi.instance().definitions().materials().blankPattern().isSameAs(stack)
                || (stack.getItem() instanceof ItemFluidEncodedPattern));
    }

    public boolean canEncodePattern() {
        if (isNotPattern(tile.getInventory().getStackInSlot(0))
                && isNotPattern(tile.getInventory().getStackInSlot(1))) {
            return false;
        }
        find_input: {
            for (IAEItemStack stack : tile.getCraftingSlots()) {
                if (stack != null && stack.getStackSize() > 0) {
                    break find_input;
                }
            }
            return false;
        }
        for (IAEItemStack stack : tile.getOutputSlots()) {
            if (stack != null && stack.getStackSize() > 0) {
                return true;
            }
        }
        return false;
    }

    public void encodePattern() {
        if (canEncodePattern()) {
            // if there is an encoded pattern, overwrite it; otherwise, consume a blank
            if (tile.getInventory().getStackInSlot(1) == null) {
                tile.getInventory().decrStackSize(0, 1); // this better work
            }
            ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
            FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
            pattern.setInputs(collectAeInventory(tile.getCraftingSlots()));
            pattern.setOutputs(collectAeInventory(tile.getOutputSlots()));
            tile.getInventory().setInventorySlotContents(1, pattern.writeToStack());
        }
    }

    private static IAEItemStack[] collectAeInventory(AeStackInventory<IAEItemStack> inv) {
        /*
         * AE2 API documentation says the input/output arrays can contain nulls, but their use of the API directly
         * contradicts that, so we just go by behaviour and filter out nulls.
         */
        List<IAEItemStack> acc = new ArrayList<>();
        for (IAEItemStack stack : inv) {
            if (stack != null) {
                if (stack.getItem() instanceof ItemFluidPacket) {
                    IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
                    if (dropStack != null) {
                        acc.add(dropStack);
                        continue;
                    }
                }
                acc.add(stack);
            }
        }
        return acc.toArray(new IAEItemStack[0]);
    }

    // adapted from ae2's AEBaseContainer#doAction
    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        Slot slot = getSlot(slotId);
        if (slot instanceof SlotFluidConvertingFake) {
            final ItemStack stack = player.inventory.getItemStack();
            switch (action) {
                case PICKUP_OR_SET_DOWN -> {
                    if (stack == null) {
                        slot.putStack(null);
                    } else {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(stack.copy());
                    }
                }
                case PLACE_SINGLE -> {
                    if (stack != null) {
                        ((SlotFluidConvertingFake) slot)
                                .putConvertedStack(Objects.requireNonNull(Util.copyStackWithSize(stack, 1)));
                    }
                }
                case SPLIT_OR_PLACE_SINGLE -> {
                    ItemStack inSlot = slot.getStack();
                    if (inSlot != null) {
                        if (stack == null) {
                            slot.putStack(Util.copyStackWithSize(inSlot, Math.max(1, inSlot.stackSize - 1)));
                        } else if (stack.isItemEqual(inSlot)) {
                            slot.putStack(
                                    Util.copyStackWithSize(
                                            inSlot,
                                            Math.min(inSlot.getMaxStackSize(), inSlot.stackSize + 1)));
                        } else {
                            ((SlotFluidConvertingFake) slot)
                                    .putConvertedStack(Objects.requireNonNull(Util.copyStackWithSize(stack, 1)));
                        }
                    } else if (stack != null) {
                        ((SlotFluidConvertingFake) slot)
                                .putConvertedStack(Objects.requireNonNull(Util.copyStackWithSize(stack, 1)));
                    }
                }
                default -> {}
            }
        } else {
            super.doAction(player, action, slotId, id);
        }
    }

    @Override
    public void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        copyStacks(inputs, tile.getCraftingSlots());
        copyStacks(outputs, tile.getOutputSlots());
    }

    private static void copyStacks(IAEItemStack[] src, AeStackInventory<IAEItemStack> dest) {
        int bound = Math.min(src.length, dest.getSlotCount());
        for (int i = 0; i < bound; i++) {
            dest.setStack(i, src[i]);
        }
    }
}
