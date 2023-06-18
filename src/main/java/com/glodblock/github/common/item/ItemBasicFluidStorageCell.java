package com.glodblock.github.common.item;

import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Optional;

import appeng.api.exceptions.MissingDefinition;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBasicFluidStorageCell extends FCBaseItemCell
        implements IStorageFluidCell, IRegister<ItemBasicFluidStorageCell> {

    private static final HashMap<Integer, IIcon> icon = new HashMap<>();
    private final int housingValue;

    @SuppressWarnings("Guava")
    public ItemBasicFluidStorageCell(final CellType whichCell, final int housingValue, final long kilobytes) {
        super(Optional.of(kilobytes + "k"));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
        this.totalBytes = kilobytes * 1024;
        this.component = whichCell;
        this.housingValue = housingValue;
        setUnlocalizedName(NameConst.ITEM_FLUID_STORAGE + kilobytes);

        switch (this.component) {
            case Cell1kPart -> {
                this.idleDrain = 0.5;
                this.perType = 8;
            }
            case Cell4kPart -> {
                this.idleDrain = 1.0;
                this.perType = 8;
            }
            case Cell16kPart -> {
                this.idleDrain = 1.5;
                this.perType = 8;
            }
            case Cell64kPart -> {
                this.idleDrain = 2.0;
                this.perType = 8;
            }
            case Cell256kPart -> {
                this.idleDrain = 2.5;
                this.perType = 8;
            }
            case Cell1024kPart -> {
                this.idleDrain = 3.0;
                this.perType = 8;
            }
            case Cell4096kPart -> {
                this.idleDrain = 3.5;
                this.perType = 8;
            }
            case Cell16384kPart -> {
                this.idleDrain = 4.0;
                this.perType = 8;
            }
            default -> {
                this.idleDrain = 0.0;
                this.perType = 8;
            }
        }
    }

    @Override
    public ItemStack getHousing() {
        return ItemAndBlockHolder.CELL_HOUSING.stack(1, this.housingValue);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted(
                "item.fluid_storage." + this.totalBytes / 1024 + ".name",
                CellType.getTypeColor(this.component),
                EnumChatFormatting.RESET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icon.put(
                (int) (this.totalBytes / 1024),
                iconRegister
                        .registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_STORAGE + "." + this.totalBytes / 1024));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int id = (int) (this.totalBytes / 1024);
        return icon.get(id);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        this.disassembleDrive(stack, world, player);
        return stack;
    }

    @Override
    public boolean onItemUseFirst(final ItemStack stack, final EntityPlayer player, final World world, final int x,
            final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        if (ForgeEventFactory.onItemUseStart(player, stack, 1) <= 0) return true;
        return this.disassembleDrive(stack, world, player);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        if (this.getHousing() != null) {
            return this.getHousing();
        }
        throw new MissingDefinition("Tried to use empty storage cells while basic storage cells are defined.");
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return AEConfig.instance.isFeatureEnabled(AEFeature.EnableDisassemblyCrafting);
    }

    @Override
    public ItemBasicFluidStorageCell register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_STORAGE + this.totalBytes / 1024, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
