package com.glodblock.github.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
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
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("FluidStack");
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
        if (fluid != null && tag.getLong("Amount") > Integer.MAX_VALUE) fluid.amount = Integer.MAX_VALUE;
        return (fluid != null && fluid.amount > 0) ? fluid : null;
    }

    @Nullable
    public static FluidStack getFluidStack(@Nullable IAEItemStack stack) {
        return stack != null ? getFluidStack(stack.getItemStack()) : null;
    }

    @Nullable
    public static IAEFluidStack getFluidAEStack(ItemStack stack) {
        if (stack != null && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound().getCompoundTag("FluidStack");
            IAEFluidStack ifs = AEFluidStack.create(FluidStack.loadFluidStackFromNBT(tag));
            if (ifs != null && tag.hasKey("Amount")) ifs.setStackSize(tag.getLong("Amount"));
            return ifs;

        }
        return null;
    }

    @Nullable
    public static IAEFluidStack getFluidAEStack(@Nullable IAEItemStack stack) {
        return stack != null ? getFluidAEStack(stack.getItemStack()) : null;
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
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean flags) {
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

    public static void setFluidAmount(ItemStack stack, long amount) {
        if (stack == null || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey("FluidStack", Constants.NBT.TAG_COMPOUND)) {
            return;
        }
        stack.getTagCompound().getCompoundTag("FluidStack").setLong("Amount", amount);
    }

    public static long getFluidAmount(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return 0;
        }
        // Default to 0 if no tag exists
        return stack.getTagCompound().getCompoundTag("FluidStack").getLong("Amount");
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
        if (fluid != null && fluid.getStackSize() > 0) {
            ItemStack stack = new ItemStack(ItemAndBlockHolder.PACKET);
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluid.getFluidStack().writeToNBT(fluidTag);
            fluidTag.setLong("Amount", fluid.getStackSize());
            tag.setTag("FluidStack", fluidTag);
            stack.setTagCompound(tag);
            return stack;
        }
        return null;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        return AEItemStack.create(newStack(fluid));
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
