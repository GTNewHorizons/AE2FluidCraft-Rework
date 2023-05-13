package com.glodblock.github.common.item;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidPacket extends FCBaseItem {

    public ItemFluidPacket() {
        setUnlocalizedName(NameConst.ITEM_FLUID_PACKET);
        setMaxStackSize(1);
    }

    @Nullable
    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return null;
        }
        FluidStack fluid = FluidStack
                .loadFluidStackFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("FluidStack"));
        return (fluid != null && fluid.amount > 0) ? fluid : null;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        return fluid != null ? fluid.getUnlocalizedName() : getUnlocalizedName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        boolean display = isDisplay(stack);
        if (display) {
            return fluid != null ? fluid.getLocalizedName() : super.getItemStackDisplayName(stack);
        }
        return fluid != null ? String.format("%s, %,d mB", fluid.getLocalizedName(), fluid.amount)
                : super.getItemStackDisplayName(stack);
    }

    public static boolean isDisplay(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound() || stack.getTagCompound() == null) {
            return false;
        }
        return stack.getTagCompound().getBoolean("DisplayOnly");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean flags) {
        FluidStack fluid = getFluidStack(stack);
        boolean display = isDisplay(stack);
        if (display) return;
        if (fluid != null) {
            for (String line : StatCollector.translateToLocalFormatted(NameConst.TT_FLUID_PACKET).split("\\\\n")) {
                tooltip.add(EnumChatFormatting.GRAY + line);
            }
        } else {
            tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted(NameConst.TT_INVALID_FLUID));
        }
    }

    @Nullable
    public static FluidStack getFluidStack(@Nullable IAEItemStack stack) {
        return stack != null ? getFluidStack(stack.getItemStack()) : null;
    }

    @Nullable
    public static ItemStack newStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount == 0) {
            return null;
        }
        ItemStack stack = new ItemStack(ItemAndBlockHolder.PACKET);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fluidTag = new NBTTagCompound();
        fluid.writeToNBT(fluidTag);
        tag.setTag("FluidStack", fluidTag);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public static ItemStack newStack(@Nullable IAEFluidStack fluid) {
        if (fluid == null || fluid.getStackSize() == 0) {
            return null;
        }
        return newStack(fluid.getFluidStack());
    }

    @Nullable
    public static ItemStack newDisplayStack(@Nullable FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        FluidStack copy = fluid.copy();
        copy.amount = 1000;
        ItemStack stack = new ItemStack(ItemAndBlockHolder.PACKET);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fluidTag = new NBTTagCompound();
        copy.writeToNBT(fluidTag);
        tag.setTag("FluidStack", fluidTag);
        tag.setBoolean("DisplayOnly", true);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        return AEItemStack.create(newStack(fluid));
    }

    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aIconRegister) {}

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return FluidRegistry.WATER.getStillIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack aStack, int aRenderPass) {
        Fluid tFluid = FluidRegistry.getFluid(aStack.getItemDamage());
        return tFluid == null ? 16777215 : tFluid.getColor();
    }

    @Override
    public ItemFluidPacket register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PACKET, FluidCraft.MODID);
        return this;
    }
}
