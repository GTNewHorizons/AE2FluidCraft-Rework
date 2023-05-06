package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.storage.FluidCellInventory;

import appeng.api.AEApi;
import appeng.api.exceptions.AppEngException;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;

public class ProxyFluidCellInventory extends FluidCellInventory {

    private static final String EC_CONVERTED = "ecc";

    public ProxyFluidCellInventory(ItemStack o, ISaveProvider container) throws AppEngException {
        super(o, container);
    }

    @Override
    protected void loadCellFluids() {
        // If we've already converted the tag to AE2FC, just go to super.
        // Else, proceed with conversion
        if (!this.tagCompound.hasKey(EC_CONVERTED)) {
            if (this.cellFluids == null) {
                this.cellFluids = AEApi.instance().storage().createFluidList();
            }
            this.cellFluids.resetStatus(); // clears totals and stuff.
            // Load using Extra Cell's NBT format
            for (int i = 0; i < this.cellType.getTotalTypes(this.cellItem); i++) {
                FluidStack fs = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid#" + i));
                tagCompound.removeTag("Fluid#" + i);
                final AEFluidStack aet = AEFluidStack.create(fs);
                if (aet != null) {
                    if (aet.getStackSize() > 0) {
                        this.cellFluids.add(aet);
                    }
                }
            }
            this.tagCompound.setBoolean(EC_CONVERTED, true);
            // Convert to the new format
            long fluidCount = 0;
            int x = 0;

            for (final IAEFluidStack v : this.cellFluids) {
                fluidCount += v.getStackSize();
                final NBTBase c = this.tagCompound.getTag(fluidSlots[x]);
                if (c instanceof NBTTagCompound) {
                    v.writeToNBT((NBTTagCompound) c);
                } else {
                    final NBTTagCompound g = new NBTTagCompound();
                    v.writeToNBT(g);
                    this.tagCompound.setTag(fluidSlots[x], g);
                }
                this.tagCompound.setLong(fluidSlotCount[x], v.getStackSize());
                x++;
            }
            this.storedFluids = (short) this.cellFluids.size();

            if (!this.cellFluids.isEmpty()) {
                this.tagCompound.setShort(FLUID_TYPE_TAG, this.storedFluids);
            }

            this.storedFluidCount = fluidCount;
            if (fluidCount > 0) {
                this.tagCompound.setLong(FLUID_COUNT_TAG, fluidCount);
            }
        } else {
            super.loadCellFluids();
        }

    }
}
