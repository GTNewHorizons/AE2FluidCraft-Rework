package com.glodblock.github.common.item;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.inventory.item.PortableFluidCellInventory;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;
import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPortableFluidCell extends AEBasePoweredItem
        implements IStorageFluidCell, IItemInventory, IRegister<ItemPortableFluidCell> {

    public ItemPortableFluidCell() {
        super(Config.portableCellBattery, Optional.absent());
        setUnlocalizedName(NameConst.ITEM_FLUID_PORTABLE_CELL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_FLUID_PORTABLE_CELL).toString());
        this.setFeature(EnumSet.of(AEFeature.PortableCell, AEFeature.StorageCells, AEFeature.PoweredTools));
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        InventoryHandler.openGui(
                player,
                w,
                new BlockPos(player.inventory.currentItem, 0, 0),
                ForgeDirection.UNKNOWN,
                GuiType.PORTABLE_FLUID_CELL);
        return item;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, StorageChannel.FLUIDS);
        if (inventory instanceof IFluidCellInventoryHandler) {
            final IFluidCellInventoryHandler handler = (IFluidCellInventoryHandler) inventory;
            final IFluidCellInventory cellInventory = handler.getCellInv();
            if (cellInventory != null) {
                lines.add(
                        EnumChatFormatting.WHITE + String.valueOf(cellInventory.getUsedBytes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + cellInventory.getTotalBytes()
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.BytesUsed.getLocal());
                lines.add(
                        EnumChatFormatting.WHITE + String.valueOf(cellInventory.getStoredFluidTypes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + cellInventory.getTotalFluidTypes()
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.Types.getLocal());
            }
        }
        if (GuiScreen.isShiftKeyDown()) {
            lines.addAll(
                    RenderUtil.listFormattedStringToWidth(
                            StatCollector.translateToLocalFormatted(NameConst.TT_CELL_PORTABLE)));
        } else {
            lines.add(StatCollector.translateToLocal(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @Override
    public long getBytes(final ItemStack cellItem) {
        return 256;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 5;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        // What even is a null fluid
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0.5;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IInventory getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public ItemPortableFluidCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PORTABLE_CELL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return new PortableFluidCellInventory(stack, x);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
