package com.glodblock.github.common.item;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStackType;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseCell;
import appeng.me.storage.FluidCellInventoryHandler;
import appeng.util.IterationCounter;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class FCBaseItemCell extends AEBaseCell {

    protected CellType component;

    public FCBaseItemCell(long bytes, int perType, int totalType, double drain) {
        super(Optional.of(bytes / 1024 + "k"));
        this.totalBytes = bytes;
        this.perType = perType;
        this.idleDrain = drain;
        this.totalTypes = totalType;
        this.component = null;
    }

    @Override
    public @NotNull IAEStackType<?> getStackType() {
        return FLUID_STACK_TYPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);

        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, getStackType());
        if (!(inventory instanceof FluidCellInventoryHandler handler) || handler.getCellInv().getTotalItemTypes() != 1
                || handler.getCellInv().getStoredItemTypes() == 0)
            return;

        final IAEFluidStack content = handler
                .getAvailableItems(FLUID_STACK_TYPE.createPrimitiveList(), IterationCounter.fetchNewId())
                .getFirstItem();
        final String oldLine = GuiText.Contains.getLocal() + ": " + content.getDisplayName();
        final int oldLineIndex = lines.lastIndexOf(oldLine);
        if (oldLineIndex >= 0) {
            if (!GuiScreen.isCtrlKeyDown()) {
                lines.set(oldLineIndex, EnumChatFormatting.GRAY + GuiText.HoldCtrlForContents.getLocal());
                return;
            }
            final String unit = FLUID_STACK_TYPE.getDisplayUnit();
            lines.set(
                    oldLineIndex,
                    "  " + content.getDisplayName()
                            + " x"
                            + ReadableNumberConverter.INSTANCE.toWideReadableForm(content.getStackSize())
                            + (unit.isEmpty() ? "" : " " + unit));
        }
    }

    public FCBaseItemCell(Optional subName) {
        super(subName);
    }

    public ItemStack getHousing() {
        return ItemAndBlockHolder.CELL_HOUSING.stack();
    }

    public ItemStack getComponent() {
        return component.stack(1);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }
}
