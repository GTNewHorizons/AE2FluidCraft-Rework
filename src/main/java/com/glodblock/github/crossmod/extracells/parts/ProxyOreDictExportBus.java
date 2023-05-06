package com.glodblock.github.crossmod.extracells.parts;

import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

import appeng.api.AEApi;

public class ProxyOreDictExportBus extends ProxyPart {

    public ProxyOreDictExportBus(ProxyPartItem item) {
        super(item);
    }

    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Node tag... should've used OOP here but im too lazy to fix it now
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");
        // Ore dict card
        NBTTagCompound upgrades = new NBTTagCompound();
        NBTTagCompound oreDictCard = new NBTTagCompound();
        AEApi.instance().definitions().materials().cardOreFilter().maybeStack(1).get().writeToNBT(oreDictCard);
        upgrades.setTag("#0", oreDictCard);
        extra.setTag("upgrades", upgrades);
        return super.transformNBT(extra);
    }
}
