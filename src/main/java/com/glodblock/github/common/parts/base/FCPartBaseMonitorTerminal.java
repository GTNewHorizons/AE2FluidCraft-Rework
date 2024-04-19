package com.glodblock.github.common.parts.base;

import static com.glodblock.github.common.item.ItemBaseWirelessTerminal.syncData;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.inventory.item.IItemTerminal;

public abstract class FCPartBaseMonitorTerminal extends FCPart implements IItemTerminal {

    protected boolean sync = true;

    public FCPartBaseMonitorTerminal(ItemStack is) {
        super(is);
    }

    public FCPartBaseMonitorTerminal(final ItemStack is, final boolean requireChannel) {
        super(is, requireChannel);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setSyncData(data.getBoolean(syncData));
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(syncData, this.sync);
    }

    public void setSyncData(boolean sync) {
        this.sync = sync;
    }

    public boolean getSyncData() {
        return this.sync;
    }

}
