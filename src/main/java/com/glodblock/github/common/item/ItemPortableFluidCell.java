package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.items.AEBasePortableCell;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellViewer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPortableFluidCell extends AEBasePortableCell implements IRegister<ItemPortableFluidCell> {

    public ItemPortableFluidCell() {
        super();
        setUnlocalizedName(NameConst.ITEM_FLUID_PORTABLE_CELL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_FLUID_PORTABLE_CELL).toString());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        if (GuiScreen.isShiftKeyDown()) {
            lines.addAll(
                    RenderUtil.listFormattedStringToWidth(
                            StatCollector.translateToLocalFormatted(NameConst.TT_CELL_PORTABLE)));
        } else {
            lines.add(StatCollector.translateToLocal(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @Override
    public long getBytesLong(final ItemStack cellItem) {
        return 256;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 5;
    }

    @Override
    public boolean isBlackListed(IAEStack<?> requestedAddition) {

        return requestedAddition instanceof IAEFluidStack ifs && (ifs.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(ifs.getFluid().getClass()));
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0.5;
    }

    @Override
    public IInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World w, final EntityPlayer p, final int x,
            final int y, final int z) {
        return new PortableCellViewer<IAEFluidStack>(is, x, StorageChannel.FLUIDS);
    }

    @Override
    public StorageChannel getStorageChannel() {
        return StorageChannel.FLUIDS;
    }

    @Override
    public ItemPortableFluidCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PORTABLE_CELL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
