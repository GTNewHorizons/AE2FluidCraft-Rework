package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.helpers.Reflected;
import appeng.parts.automation.PartLevelEmitter;

public class PartFluidLevelEmitter extends PartLevelEmitter {

    @Reflected
    public PartFluidLevelEmitter(final ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        final IAEStack<?> aes = this.getAEInventoryByName(StorageName.CONFIG).getAEStackInSlot(0);
        if (aes instanceof IAEItemStack ais && ais.getItem() instanceof ItemFluidPacket) {
            this.getAEInventoryByName(StorageName.CONFIG).putAEStackInSlot(0, ItemFluidPacket.getFluidAEStack(ais));
        }
    }

    @Override
    protected IAEStackType<?> getUltraLegacyType() {
        return FLUID_STACK_TYPE;
    }
}
