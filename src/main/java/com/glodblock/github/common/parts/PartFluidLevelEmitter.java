package com.glodblock.github.common.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.helpers.Reflected;
import appeng.parts.automation.PartLevelEmitter;
import appeng.util.LevelEmitterTypeFilter;
import appeng.util.item.AEFluidStackType;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;

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

        // Legacy fluid level emitter (placed before unified toggle): fluid only, item and essentia off
        if (!data.hasKey(LevelEmitterTypeFilter.NBT_FILTERS) && !data.hasKey("TYPE_FILTER")) {
            final Reference2BooleanMap<IAEStackType<?>> filters = this.getTypeFilters().getFilters();
            for (IAEStackType<?> type : filters.keySet()) {
                filters.put(type, type == AEFluidStackType.FLUID_STACK_TYPE);
            }
            this.saveChanges();
        }
    }
}
