package com.glodblock.github.common.item;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseCell;
import appeng.me.storage.FluidCellInventoryHandler;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemFluidVoidStorageCell extends AEBaseCell<IAEFluidStack> implements IRegister<ItemFluidVoidStorageCell> {

    public ItemFluidVoidStorageCell() {
        super(com.google.common.base.Optional.absent());
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
        if (inventory instanceof final FluidCellInventoryHandler handler) {
            lines.add(NameConst.i18n(NameConst.TT_ITEM_FLUID_VOID_CELL));
            lines.add(GuiText.VoidCellTooltip.getLocal());
            lines.add(0 + " " + GuiText.Of.getLocal() + " \u00A7k9999\u00A77 " + GuiText.BytesUsed.getLocal());
            final ICellInventory<IAEFluidStack> inv = handler.getCellInv();
            if (GuiScreen.isShiftKeyDown()) {
                lines.add(GuiText.Filter.getLocal() + ": ");
                for (int i = 0; i < inv.getConfigInventory().getSizeInventory(); ++i) {
                    IAEStack<?> s = inv.getConfigInventory().getAEStackInSlot(i);
                    if (s != null) lines.add(s.getDisplayName());
                }
            }
        }
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public boolean isBlackListed(IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public boolean storableInStorageCell() {
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
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }
}
