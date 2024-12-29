package com.glodblock.github.util;

import java.util.Comparator;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.SortDir;
import appeng.api.storage.data.IAEItemStack;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IInvTweaks;
import appeng.util.Platform;

public class FluidSorters {

    private static SortDir Direction = SortDir.ASCENDING;
    public static final Comparator<IAEItemStack> CONFIG_BASED_SORT_BY_MOD = new Comparator<>() {

        @Override
        public int compare(final IAEItemStack o1, final IAEItemStack o2) {
            final FluidStack fs1 = ItemFluidDrop.getAeFluidStack(o1).getFluidStack();
            final FluidStack fs2 = ItemFluidDrop.getAeFluidStack(o2).getFluidStack();
            final Fluid f1 = fs1.getFluid();
            final Fluid f2 = fs2.getFluid();
            if (getDirection() == SortDir.ASCENDING) {
                return this.secondarySort(
                        Util.getFluidModName(f2).compareToIgnoreCase(Util.getFluidModName(f1)),
                        fs1,
                        fs2);
            }
            return this.secondarySort(Util.getFluidModName(f1).compareToIgnoreCase(Util.getFluidModName(f2)), fs2, fs1);
        }

        private int secondarySort(final int compareToIgnoreCase, final FluidStack f1, final FluidStack f2) {
            if (compareToIgnoreCase == 0) {
                return f2.getLocalizedName().compareToIgnoreCase(f1.getLocalizedName());
            }

            return compareToIgnoreCase;
        }
    };

    public static final Comparator<IAEItemStack> CONFIG_BASED_SORT_BY_NAME = (o1, o2) -> {
        if (getDirection() == SortDir.ASCENDING) {
            return Platform.getItemDisplayName(o1).compareToIgnoreCase(Platform.getItemDisplayName(o2));
        }
        return Platform.getItemDisplayName(o2).compareToIgnoreCase(Platform.getItemDisplayName(o1));
    };
    public static final Comparator<IAEItemStack> CONFIG_BASED_SORT_BY_SIZE = (o1, o2) -> {
        if (getDirection() == SortDir.ASCENDING) {
            return compareLong(o2.getStackSize(), o1.getStackSize());
        }
        return compareLong(o1.getStackSize(), o2.getStackSize());
    };
    private static IInvTweaks api;
    public static final Comparator<IAEItemStack> CONFIG_BASED_SORT_BY_INV_TWEAKS = new Comparator<>() {

        @Override
        public int compare(final IAEItemStack o1, final IAEItemStack o2) {
            if (api == null) {
                return CONFIG_BASED_SORT_BY_NAME.compare(o1, o2);
            }

            final int cmp = api.compareItems(o1.getItemStack(), o2.getItemStack());

            if (getDirection() == SortDir.ASCENDING) {
                return cmp;
            }
            return -cmp;
        }
    };

    public static void init() {
        if (api != null) {
            return;
        }

        if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.InvTweaks)) {
            api = (IInvTweaks) IntegrationRegistry.INSTANCE.getInstance(IntegrationType.InvTweaks);
        } else {
            api = null;
        }
    }

    public static int compareInt(final int a, final int b) {
        return Integer.compare(a, b);
    }

    public static int compareLong(final long a, final long b) {
        return Long.compare(a, b);
    }

    public static int compareDouble(final double a, final double b) {
        return Double.compare(a, b);
    }

    private static SortDir getDirection() {
        return Direction;
    }

    public static void setDirection(final SortDir direction) {
        Direction = direction;
    }
}
