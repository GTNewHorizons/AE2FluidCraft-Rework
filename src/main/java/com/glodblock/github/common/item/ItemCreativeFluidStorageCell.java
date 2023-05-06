package com.glodblock.github.common.item;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCreativeFluidStorageCell extends AEBaseItem
        implements IStorageFluidCell, IRegister<ItemCreativeFluidStorageCell> {

    public ItemCreativeFluidStorageCell() {
        super();
        setUnlocalizedName(NameConst.ITEM_CREATIVE_FLUID_STORAGE);
        setTextureName(FluidCraft.resource(NameConst.ITEM_CREATIVE_FLUID_STORAGE).toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
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
        if (Config.blacklistEssentiaGas && ModAndClassUtil.ThE && requestedAddition != null) {
            return ModAndClassUtil.essentiaGas.isInstance(requestedAddition.getFluid());
        }
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
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
        return new CellUpgrades(is, 0);
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
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, StorageChannel.FLUIDS);

        if (inventory instanceof IFluidCellInventoryHandler) {
            final IFluidCellInventoryHandler handler = (IFluidCellInventoryHandler) inventory;
            final IFluidCellInventory cellInventory = handler.getCellInv();

            if (GuiScreen.isCtrlKeyDown()) {
                if (cellInventory.getContents().size() > 0) {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_CONTENTS));
                    for (IAEFluidStack fluid : cellInventory.getContents()) {
                        if (fluid != null) {
                            lines.add(String.format("  %s", fluid.getFluidStack().getLocalizedName()));
                        }
                    }
                } else {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_EMPTY));
                }
            } else {
                lines.add(StatCollector.translateToLocal(NameConst.TT_CTRL_FOR_MORE));
            }
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public ItemCreativeFluidStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_CREATIVE_FLUID_STORAGE, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
