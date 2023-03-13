package com.glodblock.github.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;

import com.glodblock.github.client.gui.container.ContainerFluidIO;
import com.glodblock.github.common.parts.PartFluidExportBus;
import com.glodblock.github.common.parts.PartFluidImportBus;
import com.glodblock.github.common.parts.base.FCSharedFluidBus;
import com.glodblock.github.util.NameConst;

public class GuiFluidIO extends GuiUpgradeable {

    private final FCSharedFluidBus bus;

    public GuiFluidIO(InventoryPlayer inventoryPlayer, FCSharedFluidBus te) {
        super(new ContainerFluidIO(inventoryPlayer, te));
        this.bus = te;
    }

    @Override
    protected GuiText getName() {
        return this.bus instanceof PartFluidImportBus ? GuiText.ImportBus : GuiText.ExportBus;
    }

    public void update(int id, IAEFluidStack stack) {
        ((ContainerFluidIO) this.cvb).getBus().setFluidInSlot(id, stack);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                this.getGuiDisplayName(
                        I18n.format(
                                this.bus instanceof PartFluidImportBus ? NameConst.GUI_FLUID_IMPORT
                                        : NameConst.GUI_FLUID_EXPORT)),
                8,
                6,
                4210752);
        this.fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.craftMode != null) {
            this.craftMode.set(this.cvb.getCraftingMode());
        }

        if (this.schedulingMode != null) {
            this.schedulingMode.set(this.cvb.getSchedulingMode());
        }
    }

    @Override
    protected void handleButtonVisibility() {
        super.handleButtonVisibility();
        if (this.schedulingMode != null) {
            this.schedulingMode.setVisibility(
                    this.bc.getInstalledUpgrades(Upgrades.CAPACITY) > 0 && this.bc instanceof PartFluidExportBus);
        }
    }
}
