package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import com.glodblock.github.client.gui.container.base.FCContainerEncodeTerminal;

import appeng.api.storage.ITerminalHost;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

public class ContainerFluidPatternTerminalEx extends FCContainerEncodeTerminal {

    private static final int CRAFTING_GRID_PAGES = 2;
    private static final int CRAFTING_GRID_WIDTH = 4;
    private static final int CRAFTING_GRID_HEIGHT = 4;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;

    public ContainerFluidPatternTerminalEx(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        this.craftingSlots = new ProcessingSlotFake[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
        this.outputSlots = new ProcessingSlotFake[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
        final IInventory output = this.getPatternTerminal().getInventoryByName("output");
        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    this.addSlotToContainer(
                            this.craftingSlots[x + y * CRAFTING_GRID_WIDTH
                                    + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotFake(
                                            crafting,
                                            this,
                                            x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS,
                                            15,
                                            -83,
                                            x,
                                            y,
                                            x + 4));
                }
            }
            for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                    this.addSlotToContainer(
                            this.outputSlots[x * CRAFTING_GRID_HEIGHT + y
                                    + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotFake(
                                            output,
                                            this,
                                            x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS,
                                            112,
                                            -83,
                                            -x,
                                            y,
                                            x));
                }
            }
        }
        this.craftingMode = false;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        if (idx < 4) // outputs
        {
            return inverted || idx == 0;
        } else {
            return !inverted || idx == 4;
        }
    }

    protected boolean validPatternSlot(Slot slot) {
        return slot instanceof ProcessingSlotFake;
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (inverted != patternTerminal.isInverted() || activePage != patternTerminal.getActivePage()) {
                inverted = patternTerminal.isInverted();
                activePage = patternTerminal.getActivePage();
                offsetSlots();
            }
        }
        super.detectAndSendChanges();
    }

    private void offsetSlots() {
        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    ((ProcessingSlotFake) this.craftingSlots[x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS])
                            .setHidden(page != activePage || x > 0 && inverted);
                    ((ProcessingSlotFake) this.outputSlots[x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS])
                            .setHidden(page != activePage || x > 0 && !inverted);
                }
            }
        }
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (field.equals("inverted") || field.equals("activePage")) {
            offsetSlots();
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.patternSlotOUT && Platform.isServer()) {
            inverted = patternTerminal.isInverted();
        }
        super.onSlotChange(s);
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        this.craftingMode = false;
        super.doAction(player, action, slotId, id);
    }

    @Override
    public void encode() {
        this.craftingMode = false;
        super.encode();
    }

    @Override
    public boolean isCraftingMode() {
        return false;
    }

    private static class ProcessingSlotFake extends OptionalSlotFake {

        private static final int POSITION_SHIFT = 9000;
        private boolean hidden = false;

        public ProcessingSlotFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX,
                int offY, int groupNum) {
            super(inv, containerBus, idx, x, y, offX, offY, groupNum);
            this.setRenderDisabled(false);
        }

        public void setHidden(boolean hide) {
            if (this.hidden != hide) {
                this.hidden = hide;
                this.xDisplayPosition += (hide ? -1 : 1) * POSITION_SHIFT;
            }
        }
    }
}
