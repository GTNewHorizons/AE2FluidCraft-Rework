package com.glodblock.github.crossmod.extracells.parts;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

public class ProxyFluidStorage extends ProxyPart {

    private static int[] FILTER_MAP = null;

    protected static void init() {
        // spotless:off
        FILTER_MAP = new int[] {
            0,  9, 18, 27, 36, 45,
            1, 10, 19, 28, 37, 46,
            2, 11, 20, 29, 38, 47,
            3, 12, 21, 30, 39, 48,
            4, 13, 22, 31, 40, 49,
            5, 14, 23, 32, 41, 50,
            6, 15, 24, 33, 42, 51,
            7, 16, 25, 34, 43, 52,
            8, 17, 26, 35, 44, 53 };
        //spotless:on
    }

    public ProxyFluidStorage(ProxyPartItem item) {
        super(item);
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Transform in place, less messy IMO
        // Fluid filter slots
        NBTTagCompound fluidFilterNew = new NBTTagCompound();
        for (int slot = 0; slot < 54; ++slot) {
            String oldFilterName = "FilterFluid#" + slot;
            fluidFilterNew
                    .setTag("#" + FILTER_MAP[slot], ProxyPart.createFluidDisplayTag(extra.getString(oldFilterName)));
            extra.removeTag(oldFilterName);
        }
        extra.setTag("config", fluidFilterNew);

        // Part data
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");

        // Access
        extra.setString("ACCESS", extra.getString("access"));
        extra.removeTag("access");

        // Upgrades
        NBTTagList upgrades = extra.getTagList("upgradeInventory", TAG_COMPOUND);
        NBTTagCompound upgradeNew = new NBTTagCompound();
        upgradeNew.setTag("#0", upgrades.getCompoundTagAt(0));
        upgradeNew.setTag("#1", new NBTTagCompound());
        upgradeNew.setTag("#2", new NBTTagCompound());
        upgradeNew.setTag("#3", new NBTTagCompound());
        upgradeNew.setTag("#4", new NBTTagCompound());
        extra.setTag("upgrades", upgradeNew);
        extra.removeTag("upgradeInventory");
        return extra;
    }
}
