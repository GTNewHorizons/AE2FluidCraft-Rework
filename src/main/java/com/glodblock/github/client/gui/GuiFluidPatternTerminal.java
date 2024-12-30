package com.glodblock.github.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.glodblock.github.client.gui.base.FCGuiEncodeTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.PatternBeSubstitution;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.core.localization.GuiText;

public class GuiFluidPatternTerminal extends FCGuiEncodeTerminal {

    public GuiFluidPatternTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te, new ContainerFluidPatternTerminal(inventoryPlayer, te));
    }

    public GuiFluidPatternTerminal(final InventoryPlayer inventoryPlayer, final IWirelessTerminal te) {
        super(inventoryPlayer, te, new ContainerFluidPatternWireless(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.tabCraftButton = new GuiTabButton(
                this.guiLeft + 173,
                this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.crafting_table),
                GuiText.CraftingPattern.getLocal(),
                itemRender);
        this.buttonList.add(this.tabCraftButton);

        this.tabProcessButton = new GuiTabButton(
                this.guiLeft + 173,
                this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.furnace),
                GuiText.ProcessingPattern.getLocal(),
                itemRender);
        this.buttonList.add(this.tabProcessButton);

        this.substitutionsEnabledBtn = new GuiImgButton(
                this.guiLeft + 84,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ItemSubstitution.ENABLED);
        this.substitutionsEnabledBtn.setHalfSize(true);
        this.buttonList.add(this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new GuiImgButton(
                this.guiLeft + 84,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ItemSubstitution.DISABLED);
        this.substitutionsDisabledBtn.setHalfSize(true);
        this.buttonList.add(this.substitutionsDisabledBtn);

        this.clearBtn = new GuiImgButton(
                this.guiLeft + 74,
                this.guiTop + this.ySize - 163,
                Settings.ACTIONS,
                ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.buttonList.add(this.clearBtn);

        this.encodeBtn = new GuiImgButton(
                this.guiLeft + 147,
                this.guiTop + this.ySize - 142,
                Settings.ACTIONS,
                ActionItems.ENCODE);
        this.buttonList.add(this.encodeBtn);

        int combineLeft = 74;
        int combineTop = 153;
        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton(
                    this.guiLeft + 74,
                    this.guiTop + this.ySize - 153,
                    Settings.ACTIONS,
                    ActionItems.DOUBLE);
            this.doubleBtn.setHalfSize(true);
            this.buttonList.add(this.doubleBtn);
            combineLeft = 84;
        }
        if (ModAndClassUtil.isBeSubstitutionsButton) {
            combineLeft = 74;
            combineTop -= 11;
            this.beSubstitutionsEnabledBtn = new GuiImgButton(
                    this.guiLeft + 84,
                    this.guiTop + this.ySize - 153,
                    Settings.ACTIONS,
                    PatternBeSubstitution.ENABLED);
            this.beSubstitutionsEnabledBtn.setHalfSize(true);
            this.buttonList.add(this.beSubstitutionsEnabledBtn);

            this.beSubstitutionsDisabledBtn = new GuiImgButton(
                    this.guiLeft + 84,
                    this.guiTop + this.ySize - 153,
                    Settings.ACTIONS,
                    PatternBeSubstitution.DISABLED);
            this.beSubstitutionsDisabledBtn.setHalfSize(true);
            this.buttonList.add(this.beSubstitutionsDisabledBtn);
        }
        this.combineEnableBtn = new GuiFCImgButton(
                this.guiLeft + combineLeft,
                this.guiTop + this.ySize - combineTop,
                "FORCE_COMBINE",
                "DO_COMBINE");
        this.combineEnableBtn.setHalfSize(true);
        this.buttonList.add(this.combineEnableBtn);

        this.combineDisableBtn = new GuiFCImgButton(
                this.guiLeft + combineLeft,
                this.guiTop + this.ySize - combineTop,
                "NOT_COMBINE",
                "DONT_COMBINE");
        this.combineDisableBtn.setHalfSize(true);
        this.buttonList.add(this.combineDisableBtn);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal(NameConst.GUI_FLUID_PATTERN_TERMINAL),
                8,
                this.ySize - 96 + 2 - getReservedSpace(),
                4210752);
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (mouseButton == 3) {
            if (this.container.isCraftingMode()
                    && (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix
                            || slot instanceof SlotPatternTerm)) {
                return;
            }
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    @Override
    protected String getBackground() {
        if (this.container.isCraftingMode()) {
            return "gui/pattern.png";
        }
        return "gui/pattern2.png";
    }
}
