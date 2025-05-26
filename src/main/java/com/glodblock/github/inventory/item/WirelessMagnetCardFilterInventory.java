package com.glodblock.github.inventory.item;

import static com.glodblock.github.inventory.item.WirelessMagnet.filterConfigKey;
import static com.glodblock.github.inventory.item.WirelessMagnet.filterKey;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.inventory.ItemBiggerAppEngInventory;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.OreHelper;
import appeng.util.prioitylist.OreFilteredList;

public class WirelessMagnetCardFilterInventory extends BaseWirelessInventory implements IWirelessMagnetFilter {

    private boolean ignoreNbt;
    private boolean ignoreMeta;
    private boolean useOre;
    private boolean useOreDict;
    private String oreDictFilter = "";
    protected Predicate<IAEItemStack> filterPredicate = null;
    private WirelessMagnet.ListMode listMode = WirelessMagnet.ListMode.BlackList;
    private final AppEngInternalInventory filterInventory;
    private final NBTTagCompound settingCache;
    private final NBTTagCompound filterCache;

    @SuppressWarnings("unchecked")
    public WirelessMagnetCardFilterInventory(ItemStack is, int slot, IGridNode gridNode, EntityPlayer player) {
        super(is, slot, gridNode, player, StorageChannel.ITEMS, true);
        filterInventory = new ItemBiggerAppEngInventory(is, filterKey, 27) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack newItemStack) {
                if (newItemStack != null) newItemStack.stackSize = 1;
                super.setInventorySlotContents(slot, newItemStack);
            }
        };
        settingCache = getSettingTag(is);
        filterCache = getFilterTag(is);
        readFromNBT();
    }

    public void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        if (!data.hasKey(filterConfigKey)) this.writeToNBT();
        NBTTagCompound tag = (NBTTagCompound) data.getTag(filterConfigKey);
        ignoreNbt = tag.getBoolean("nbt");
        ignoreMeta = tag.getBoolean("meta");
        useOre = tag.getBoolean("ore");
        useOreDict = tag.getBoolean("oreDict");
        oreDictFilter = tag.getString("oreDictFilter");
        this.listMode = WirelessMagnet.ListMode.values()[tag.getInteger("list")];
    }

    public void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        NBTTagCompound tmp = new NBTTagCompound();
        tmp.setBoolean("nbt", ignoreNbt);
        tmp.setBoolean("meta", ignoreMeta);
        tmp.setBoolean("ore", useOre);
        tmp.setBoolean("oreDict", useOreDict);
        tmp.setString("oreDictFilter", oreDictFilter);
        tmp.setInteger("list", this.listMode.ordinal());
        data.setTag(filterConfigKey, tmp);
    }

    private NBTTagCompound getSettingTag(ItemStack is) {
        return (NBTTagCompound) is.getTagCompound().getCompoundTag(filterConfigKey).copy();
    }

    private NBTTagCompound getFilterTag(ItemStack is) {
        return (NBTTagCompound) is.getTagCompound().getCompoundTag(filterKey).copy();
    }

    @Override
    public WirelessMagnet.ListMode getListMode() {
        return this.listMode;
    }

    @Override
    public boolean getNBTMode() {
        return ignoreNbt;
    }

    @Override
    public boolean getMetaMode() {
        return ignoreMeta;
    }

    @Override
    public boolean getOreMode() {
        return useOre;
    }

    @Override
    public boolean getOreDictMode() {
        return useOreDict;
    }

    @Override
    public String getOreDictFilter() {
        return oreDictFilter;
    }

    @Override
    public void setListMode(WirelessMagnet.ListMode mode) {
        this.listMode = mode;
    }

    @Override
    public void setNBTMode(boolean ignoreNBT) {
        ignoreNbt = ignoreNBT;
    }

    @Override
    public void setMetaMode(boolean ignoreMeta) {
        this.ignoreMeta = ignoreMeta;
    }

    @Override
    public void setOreMode(boolean useOre) {
        this.useOre = useOre;
    }

    @Override
    public void setOreDictMode(boolean useOreDict) {
        this.useOreDict = useOreDict;
    }

    @Override
    public void setOreDictFilter(String str) {
        oreDictFilter = str;
        filterPredicate = null;
    }

    @Override
    public void clearConfig() {
        IInventory inv = this.getInventoryByName("config");
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, null);
        }
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public IInventory getViewCellStorage() {
        return null;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        return 0;
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {}

    @Override
    public void saveSettings() {
        super.saveSettings();
        writeToNBT();
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("config")) {
            return filterInventory;
        }
        return null;
    }

    public boolean doInject(IAEItemStack is, EntityItem itemToGet, World world) {
        IAEItemStack ais = (IAEItemStack) injectItems(is);
        if (ais != null) {
            player.onItemPickup(itemToGet, ais.getItemStack().stackSize);
            player.inventory.addItemStackToInventory(ais.getItemStack());
            world.playSoundAtEntity(
                    player,
                    "random.pop",
                    0.15F,
                    ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return false;
        }
        return true;
    }

    public boolean isItemFiltered(ItemStack inputItemStack) {
        if (useOreDict && !oreDictFilter.isEmpty()) {
            if (filterPredicate == null) filterPredicate = OreFilteredList.makeFilter(oreDictFilter);
            if (filterPredicate.test(AEItemStack.create(inputItemStack))) return true;
        }

        for (int i = 0; i < filterInventory.getSizeInventory(); i++) {
            ItemStack is = filterInventory.getStackInSlot(i);
            if (is != null) {
                if (useOre) {
                    // use oredict
                    if (OreHelper.INSTANCE
                            .sameOre(OreHelper.INSTANCE.isOre(is), OreHelper.INSTANCE.isOre(inputItemStack)))
                        return true;
                }
                if (ignoreMeta && ignoreNbt) {
                    // ignore meta & nbt
                    return is.getItem().equals(inputItemStack.getItem());
                } else if (ignoreMeta) {
                    // ignore meta only
                    return ItemStack.areItemStackTagsEqual(is, inputItemStack)
                            && is.getItem() == inputItemStack.getItem();
                } else if (ignoreNbt) {
                    // ignore nbt only
                    return is.getItem() == inputItemStack.getItem()
                            && is.getItemDamage() == inputItemStack.getItemDamage();
                } else {
                    // ignore nothing/don't use oredict--must be exact match
                    return is.isItemEqual(inputItemStack) && ItemStack.areItemStackTagsEqual(is, inputItemStack);
                }
            }
        }
        return false;
    }

    public boolean isPassFilter(ItemStack is) {
        return is != null && (listMode == WirelessMagnet.ListMode.WhiteList) == isItemFiltered(is);
    }

    public boolean checkCache(ItemStack is) {
        return settingCache.equals(getSettingTag(is)) && filterCache.equals(getFilterTag(is));
    }

    public static class FilterCache {

        private static final Map<UUID, WirelessMagnetCardFilterInventory> cache = new WeakHashMap<>();

        public static WirelessMagnetCardFilterInventory getFilter(ItemStack is, int slot, IGridNode gridNode,
                EntityPlayer player) {
            WirelessMagnetCardFilterInventory cachedInv = cache.get(player.getUniqueID());
            if (cachedInv != null && cachedInv.checkCache(is)) {
                return cachedInv;
            } else {
                WirelessMagnetCardFilterInventory newInv = new WirelessMagnetCardFilterInventory(
                        is,
                        slot,
                        gridNode,
                        player);
                cache.put(player.getUniqueID(), newInv);
                return newInv;
            }
        }
    }
}
