package com.glodblock.github.crossmod.thaumcraft;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.common.Optional;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.storage.AspectStack;

public class AspectUtil {

    public static final EssentiaItemContainerHelper HELPER = EssentiaItemContainerHelper.INSTANCE;
    public static int R = 128;

    @Optional.Method(modid = "thaumicenergistics")
    public static void init() {
        R = ThEApi.instance().config().conversionMultiplier();
    }

    public static boolean isPlayerDiscoveredAspect(final EntityPlayer player, final Aspect aspect) {
        if (player != null && aspect != null) {
            return Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect(player.getCommandSenderName(), aspect);
        }
        return false;
    }

    public static boolean isEssentiaGas(Fluid fluid) {
        return fluid instanceof GaseousEssentia;
    }

    public static boolean isEssentiaGas(FluidStack fluid) {
        return fluid != null && fluid.getFluid() instanceof GaseousEssentia;
    }

    public static boolean isEssentiaGas(IAEFluidStack fluid) {
        return fluid != null && fluid.getFluid() instanceof GaseousEssentia;
    }

    public static Aspect getAspectFromGas(FluidStack fluid) {
        if (!isEssentiaGas(fluid)) {
            return null;
        }
        GaseousEssentia gas = (GaseousEssentia) fluid.getFluid();
        return gas.getAspect();
    }

    public static Aspect getAspectFromJar(ItemStack stack) {
        if (isEssentiaContainer(stack)) {
            return HELPER.getAspectInContainer(stack);
        }
        return null;
    }

    public static Aspect getAspectFromGas(IAEFluidStack fluid) {
        if (isEssentiaGas(fluid)) {
            return null;
        }
        GaseousEssentia gas = (GaseousEssentia) fluid.getFluid();
        return gas.getAspect();
    }

    public static FluidStack getGasFromAspect(IAspectStack aspectStack) {
        if (aspectStack == null || aspectStack.isEmpty()) return null;
        return new FluidStack(
                GaseousEssentia.getGasFromAspect(aspectStack.getAspect()),
                (int) aspectStack.getStackSize() * R);
    }

    public static boolean isEssentiaContainer(ItemStack stack) {
        return HELPER.getItemType(stack) != EssentiaItemContainerHelper.AspectItemType.Invalid
                && HELPER.getItemType(stack) != EssentiaItemContainerHelper.AspectItemType.ItemAspect;
    }

    public static AEFluidStack getAEGasFromContainer(ItemStack stack) {
        if (isEssentiaContainer(stack) && !isEmptyEssentiaContainer(stack)) {
            IAspectStack aspectStack = HELPER.getAspectStackFromContainer(stack);
            aspectStack.setStackSize(stack.stackSize * aspectStack.getStackSize());
            FluidStack gas = getGasFromAspect(aspectStack);
            return AEFluidStack.create(gas);
        }
        return null;
    }

    public static boolean isEmptyEssentiaContainer(ItemStack stack) {
        return HELPER.isContainerEmpty(stack) && isEssentiaContainer(stack);
    }

    public static ItemStack setAspectAmount(ItemStack itemStack, int amount, Aspect aspect) {
        if (isEssentiaContainer(itemStack)) {
            IEssentiaContainerItem container = (IEssentiaContainerItem) itemStack.getItem();
            assert container != null;
            container.getAspects(itemStack).aspects.put(aspect, amount);
            return itemStack;
        }
        return null;
    }

    public static MutablePair<Integer, ItemStack> fillEssentiaFromGas(ItemStack itemStack, FluidStack fluid) {
        if (isEssentiaContainer(itemStack) && isEssentiaGas(fluid)) {
            if (itemStack.stackSize != 1) {
                return null;
            }
            Aspect aspect = getAspectFromGas(fluid);
            int amount = fluid.amount / R;
            EssentiaItemContainerHelper.AspectItemType iType = HELPER.getItemType(itemStack);
            if (iType == EssentiaItemContainerHelper.AspectItemType.JarLabel) {
                ItemStack label = itemStack.copy();
                HELPER.setLabelAspect(label, aspect);
            }
            int cap = HELPER.getContainerCapacity(itemStack);
            int tryFill = Math.min(cap, amount);
            ImmutablePair<Integer, ItemStack> result = HELPER
                    .injectIntoContainer(itemStack, new AspectStack(aspect, tryFill));
            return result != null ? new MutablePair<>(result.left * R, result.right) : null;
        }
        return null;
    }

    public static MutablePair<Integer, ItemStack> drainEssentiaFromGas(ItemStack itemStack, FluidStack fluid) {
        if (isEssentiaContainer(itemStack) && isEssentiaGas(fluid)) {
            Aspect aspect = getAspectFromGas(fluid);
            int amount = fluid.amount / R;
            IAspectStack contents = HELPER.getAspectStackFromContainer(itemStack);
            int drained;
            ItemStack stack = null;
            if (contents == null || !Objects.equals(contents.getAspect(), aspect)) {
                drained = 0;
            } else {
                ImmutablePair<Integer, ItemStack> result = HELPER
                        .extractFromContainer(itemStack, new AspectStack(aspect, amount));
                drained = result == null ? 0 : result.left;
                stack = result == null ? null : result.right;
            }
            return stack == null ? null : new MutablePair<>(drained * R, stack);
        }
        return null;
    }

}
