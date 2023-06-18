package com.glodblock.github.crossmod.extracells.parts;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

import appeng.api.config.RedstoneMode;

public class ProxyLevelEmitter extends ProxyPart {

    public ProxyLevelEmitter(ProxyPartItem item) {
        super(item);
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Fluid
        NBTTagCompound fluidSlots = new NBTTagCompound();
        fluidSlots.setTag("#0", ProxyPart.createFluidDisplayTag(extra.getString("fluid")));
        extra.setTag("config", fluidSlots);
        extra.removeTag("fluid");
        // Redstone
        RedstoneMode redstoneMode = RedstoneMode.values()[extra.getInteger("mode")];
        if (redstoneMode == RedstoneMode.LOW_SIGNAL) {
            extra.setString("REDSTONE_EMITTER", "LOW_SIGNAL");
        } else {
            extra.setString("REDSTONE_EMITTER", "HIGH_SIGNAL");
        }
        extra.removeTag("mode");
        // Level
        long wanted = extra.getLong("wantedAmount");
        extra.setLong("reportingValue", wanted);
        extra.setLong("lastReportingValue", wanted);
        extra.removeTag("wantedAmount");
        // Part
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");
        return extra;
    }
}
