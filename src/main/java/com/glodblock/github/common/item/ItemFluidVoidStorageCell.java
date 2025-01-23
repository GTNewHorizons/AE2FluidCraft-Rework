package com.glodblock.github.common.item;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemFluidVoidStorageCell extends AEBaseItem
        implements IStorageFluidCell, IRegister<ItemFluidVoidStorageCell> {

    public ItemFluidVoidStorageCell() {
        super();
        setUnlocalizedName(NameConst.ITEM_FLUID_VOID_CELL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_FLUID_VOID_CELL).toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
    }

    @Override
    public ItemFluidVoidStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_VOID_CELL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
                                      final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
            .getCellInventory(stack, null, StorageChannel.FLUIDS);
        if (inventory instanceof final IFluidCellInventoryHandler handler) {
            lines.add(NameConst.i18n(NameConst.TT_ITEM_FLUID_VOID_CELL));
            lines.add(GuiText.VoidCellTooltip.getLocal());
            lines.add(0 + " " + GuiText.Of.getLocal() + " \u00A7k9999\u00A77 " + GuiText.BytesUsed.getLocal());
            final IFluidCellInventory inv = handler.getCellInv();
            if (GuiScreen.isShiftKeyDown()) {
                lines.add(GuiText.Filter.getLocal() + ": ");
                for (int i = 0; i < inv.getConfigInventory().getSizeInventory(); ++i) {
                    ItemStack s = inv.getConfigInventory().getStackInSlot(i);
                    if (s != null) lines.add(s.getDisplayName());
                }
            }
        }
    }

    @Override
    public long getBytes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 5);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }
}
