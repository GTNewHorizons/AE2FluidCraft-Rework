package com.glodblock.github.common.item;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ItemFluidDrop extends Item {

    @SideOnly(Side.CLIENT)
    public IIcon shape;

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        if (CreativeTabs.tabMisc.equals(tab)) {
            list.add(newStack(new FluidStack(FluidRegistry.WATER, 1)));
            list.add(newStack(new FluidStack(FluidRegistry.LAVA, 1)));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        if (ItemFluidPacket.isDisplay(stack)) {
            return fluid != null ? fluid.getLocalizedName() : StatCollector.translateToLocalFormatted("error.unknown");
        }
        return StatCollector.translateToLocalFormatted(
                "item.fluid_drop.name", fluid == null ? "???" : fluid.getLocalizedName());
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean flag) {
        FluidStack fluid = getFluidStack(stack);
        if (ItemFluidPacket.isDisplay(stack)) return;
        if (fluid != null) {
            tooltip.add(String.format(EnumChatFormatting.GRAY + "%s, 1 mB", fluid.getLocalizedName()));
        } else {
            tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted(NameConst.TT_INVALID_FLUID));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aIconRegister) {
        super.registerIcons(aIconRegister);
        shape = aIconRegister.registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_DROP);
    }

    public static ItemStack newStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        ItemStack stack = new ItemStack(ItemAndBlockHolder.DROP, fluid.amount);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Fluid", fluid.getFluid().getName());
        stack.setTagCompound(tag);
        return stack;
    }

    public static ItemStack newDisplayStack(FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        ItemStack stack = new ItemStack(ItemAndBlockHolder.DROP, fluid.amount);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Fluid", fluid.getFluid().getName());
        tag.setBoolean("DisplayOnly", true);
        stack.setTagCompound(tag);
        return stack;
    }

    public static boolean isFluidStack(ItemStack stack) {
        if (getFluidStack(stack) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFluidStack(@Nullable IAEItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (getFluidStack(stack.getItemStack()) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack == null || stack.getItem() != ItemAndBlockHolder.DROP || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
        if (!tag.hasKey("Fluid", Constants.NBT.TAG_STRING)) {
            return null;
        }
        Fluid fluid = FluidRegistry.getFluid(tag.getString("Fluid").toLowerCase());
        if (fluid == null) {
            return null;
        }
        FluidStack fluidStack = new FluidStack(fluid, stack.stackSize);
        if (tag.hasKey("FluidTag", Constants.NBT.TAG_COMPOUND)) {
            fluidStack.tag = tag.getCompoundTag("FluidTag");
        }
        return fluidStack;
    }

    public static IAEFluidStack getAeFluidStack(@Nullable IAEItemStack stack) {
        if (stack == null) {
            return null;
        }
        IAEFluidStack fluidStack = AEFluidStack.create(getFluidStack(stack.getItemStack()));
        if (fluidStack == null) {
            return null;
        }
        fluidStack.setStackSize(stack.getStackSize());
        return fluidStack;
    }

    public static IAEItemStack newAeStack(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.create(newStack(fluid));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.amount);
        return stack;
    }

    public static IAEItemStack newAeStack(@Nullable IAEFluidStack fluid) {
        if (fluid == null || fluid.getStackSize() <= 0) {
            return null;
        }
        IAEItemStack stack = AEItemStack.create(newStack(fluid.getFluidStack()));
        if (stack == null) {
            return null;
        }
        stack.setStackSize(fluid.getStackSize());
        return stack;
    }

    public ItemFluidDrop register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_DROP, FluidCraft.MODID);
        return this;
    }
}
