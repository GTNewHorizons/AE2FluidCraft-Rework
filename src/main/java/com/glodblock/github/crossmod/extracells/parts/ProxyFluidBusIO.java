package com.glodblock.github.crossmod.extracells.parts;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

import appeng.api.config.RedstoneMode;

public class ProxyFluidBusIO extends ProxyPart {

    /**
     * Map for converting slots from bus EC2 -> AE2FC
     */
    private static int[] FILTER_MAP = null;

    public static void init() {
        FILTER_MAP = new int[] { 5, 3, 6, 1, 0, 2, 7, 4, 8 };
    }

    public ProxyFluidBusIO(ProxyPartItem partItem) {
        super(partItem);
    }

    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Transform NBT in place
        // Fluid Filter
        NBTTagCompound fluidFilterNew = new NBTTagCompound();
        for (int slot = 0; slot < 9; ++slot) {
            String oldFilterName = "FilterFluid#" + slot;
            fluidFilterNew
                    .setTag("#" + FILTER_MAP[slot], ProxyPart.createFluidDisplayTag(extra.getString(oldFilterName)));
            extra.removeTag(oldFilterName);
        }
        extra.setTag("config", fluidFilterNew);
        // Upgrades
        NBTTagList upgrades = extra.getTagList("upgradeInventory", TAG_COMPOUND);
        NBTTagCompound upgradesNew = new NBTTagCompound();
        for (int i = 0; i < 4; ++i) {
            NBTTagCompound upgrade = upgrades.getCompoundTagAt(i);
            upgrade.removeTag("Slot");
            upgradesNew.setTag("#" + i, upgrade);
        }
        extra.removeTag("upgradeInventory");
        extra.setTag("upgrades", upgradesNew);
        // Redstone mode
        RedstoneMode redstoneMode = RedstoneMode.values()[extra.getInteger("redstoneMode")];
        switch (redstoneMode) {
            case LOW_SIGNAL:
                extra.setString("REDSTONE_CONTROLLED", "LOW_SIGNAL");
                break;
            case HIGH_SIGNAL:
                extra.setString("REDSTONE_CONTROLLED", "HIGH_SIGNAL");
                break;
            case SIGNAL_PULSE:
                extra.setString("REDSTONE_CONTROLLED", "SIGNAL_PULSE");
                break;
            default:
                extra.setString("REDSTONE_CONTROLLED", "IGNORE");
                break;
        }
        extra.removeTag("redstoneMode");
        // Part data
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");
        // Extra tags
        extra.setString("CRAFT_ONLY", "NO");
        extra.setString("FUZZY_MODE", "IGNORE_ALL");
        extra.setString("SCHEDULING_MODE", "DEFAULT");
        return extra;
    }
}
