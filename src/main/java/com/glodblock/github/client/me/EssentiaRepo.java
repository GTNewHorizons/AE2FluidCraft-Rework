package com.glodblock.github.client.me;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraftforge.fluids.Fluid;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.util.FluidSorters;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
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
        if (this.paused) {
            // Update existing view with new data
            for (int i = 0; i < this.view.size(); i++) {
                IAEItemStack entry = this.view.get(i);
                IAEItemStack serverEntry = this.list.findPrecise(entry);
                if (serverEntry == null) {
                    entry.setStackSize(0);
                } else {
                    this.view.set(i, serverEntry);
                }
            }

            // Append newly added item stacks to the end of the view
            Set<IAEItemStack> viewSet = new HashSet<>(this.view);
            ArrayList<IAEItemStack> entriesToAdd = new ArrayList<>();
            for (IAEItemStack serverEntry : this.list) {
                if (!viewSet.contains(serverEntry)) {
                    entriesToAdd.add(serverEntry);
                }
            }
            addEntriesToView(entriesToAdd);
        } else {
            this.view.clear();
            this.view.ensureCapacity(this.list.size());
            addEntriesToView(this.list);
        }

        // Do not sort if paused
        if (!this.paused) {
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
        }

        // Update the display
        this.dsp.clear();
        this.dsp.ensureCapacity(this.list.size());
        for (final IAEItemStack is : this.view) {
            this.dsp.add(is.getItemStack());
        }
    }

    private void addEntriesToView(Iterable<IAEItemStack> entries) {
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

        for (IAEItemStack is : entries) {
            if (!AspectUtil.isEssentiaGas(ItemFluidDrop.getAeFluidStack(is))) {
                continue;
            }

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
    }

}
