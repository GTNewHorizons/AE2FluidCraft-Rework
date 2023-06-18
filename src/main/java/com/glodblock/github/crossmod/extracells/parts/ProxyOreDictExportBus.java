package com.glodblock.github.crossmod.extracells.parts;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

import appeng.api.AEApi;

public class ProxyOreDictExportBus extends ProxyPart {

    public ProxyOreDictExportBus(ProxyPartItem item) {
        super(item);
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // TODO: Node tag... should use OOP here.
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
