package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseExportBus;
import appeng.util.InventoryAdaptor;

public class PartFluidExportBus extends PartBaseExportBus<IAEFluidStack> {

    public PartFluidExportBus(ItemStack is) {
        super(is);
    }

    @Override
    public int calculateAmountToSend() {
        double amount = 1000D;
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 4:
                amount = amount * 1.5;
            case 3:
                amount = amount * 2;
            case 2:
                amount = amount * 4;
            case 1:
                amount = amount * 8;
        }
        switch (this.getInstalledUpgrades(Upgrades.SUPERSPEED)) {
            case 4:
                amount = amount * 8;
            case 3:
                amount = amount * 12;
            case 2:
                amount = amount * 16;
            case 1:
                amount = amount * 32;
        }
        return (int) Math.floor(amount);
    }

    @Override
    protected IMEMonitor<IAEFluidStack> getMonitor() {
        try {
            return this.getProxy().getStorage().getFluidInventory();
        } catch (final GridAccessException e) {
            return null;
        }
    }

    @Override
    protected void doFuzzy(IAEFluidStack aes, FuzzyMode fzMode, InventoryAdaptor destination, IEnergyGrid energy,
            IMEMonitor<IAEFluidStack> gridInv) {}

    @Override
    protected void doOreDict(InventoryAdaptor destination, IEnergyGrid energy, IMEMonitor<IAEFluidStack> gridInv) {}
}
