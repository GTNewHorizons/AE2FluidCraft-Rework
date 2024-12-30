package com.glodblock.github.client.me;

import java.util.Iterator;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.util.FluidSorters;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;

public class EssentiaRepo extends FluidRepo {

    public EssentiaRepo(final IScrollSource src, final ISortSource sortSrc) {
        super(src, sortSrc);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void updateView() {
        this.view.clear();
        this.dsp.clear();

        this.view.ensureCapacity(this.list.size());
        this.dsp.ensureCapacity(this.list.size());

        final Enum<?> viewMode = this.sortSrc.getSortDisplay();
        final Enum<?> searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        if (searchMode == SearchBoxMode.NEI_AUTOSEARCH || searchMode == SearchBoxMode.NEI_MANUAL_SEARCH) {
            this.updateNEI(this.searchString);
        }

        String innerSearch = this.searchString;

        if (innerSearch.startsWith("@")) {
            innerSearch = innerSearch.substring(1);
        }

        Pattern m;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (final Throwable ignore) {
            try {
                m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
            } catch (final Throwable __) {
                return;
            }
        }

        for (IAEItemStack is : this.list) {
            if (this.myPartitionList != null) {
                if (!this.myPartitionList.isListed(is)) {
                    continue;
                }
            }

            if (viewMode == ViewItems.CRAFTABLE && !is.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.CRAFTABLE) {
                is = is.copy();
                is.setStackSize(0);
            }

            if (viewMode == ViewItems.STORED && is.getStackSize() == 0) {
                continue;
            }

            Fluid fluid = ItemFluidDrop.getAeFluidStack(is).getFluid();
            if (!AspectUtil.isEssentiaGas(fluid)) {
                continue;
            }

            if (m.matcher(fluid.getLocalizedName().toLowerCase()).find()) {
                this.view.add(is);
            }
        }

        final Enum<?> SortBy = this.sortSrc.getSortBy();
        final Enum<?> SortDir = this.sortSrc.getSortDir();

        FluidSorters.setDirection((appeng.api.config.SortDir) SortDir);
        FluidSorters.init();

        if (SortBy == SortOrder.MOD) {
            this.view.sort(FluidSorters.CONFIG_BASED_SORT_BY_MOD);
        } else if (SortBy == SortOrder.AMOUNT) {
            this.view.sort(FluidSorters.CONFIG_BASED_SORT_BY_SIZE);
        } else if (SortBy == SortOrder.INVTWEAKS) {
            this.view.sort(FluidSorters.CONFIG_BASED_SORT_BY_INV_TWEAKS);
        } else {
            this.view.sort(FluidSorters.CONFIG_BASED_SORT_BY_NAME);
        }

        for (final IAEItemStack is : this.view) {
            this.dsp.add(is.getItemStack());
        }
        Iterator<IAEItemStack> it1 = this.view.iterator();
        while (it1.hasNext()) {
            IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(it1.next());
            if (!AspectUtil.isEssentiaGas(fluid)) {
                it1.remove();
            }
        }
        Iterator<ItemStack> it2 = this.dsp.iterator();
        while (it2.hasNext()) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(it2.next());
            if (!AspectUtil.isEssentiaGas(fluid)) {
                it2.remove();
            }
        }
    }

}
