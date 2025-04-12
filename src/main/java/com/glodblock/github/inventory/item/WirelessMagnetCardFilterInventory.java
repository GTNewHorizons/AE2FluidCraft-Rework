/* Author: asdflj */
package com.glodblock.github.inventory.item;

import static com.glodblock.github.inventory.item.WirelessMagnet.filterConfigKey;
import static com.glodblock.github.inventory.item.WirelessMagnet.filterKey;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.inventory.ItemBiggerAppEngInventory;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class WirelessMagnetCardFilterInventory extends BaseWirelessInventory implements IWirelessMagnetFilter {

    private boolean ignoreNbt;
    private boolean ignoreMeta;
    private boolean useOreDict;
    private WirelessMagnet.ListMode listMode = WirelessMagnet.ListMode.WhiteList;
    protected AppEngInternalInventory filterList;

    @SuppressWarnings("unchecked")
    public WirelessMagnetCardFilterInventory(ItemStack is, int slot, IGridNode gridNode, EntityPlayer player) {
        super(is, slot, gridNode, player, StorageChannel.ITEMS);
        this.filterList = new ItemBiggerAppEngInventory(is, filterKey, 27);
        readFromNBT();
    }

    public void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        if (!data.hasKey(filterConfigKey)) this.writeToNBT();
        NBTTagCompound tag = (NBTTagCompound) data.getTag(filterConfigKey);
        ignoreNbt = tag.getBoolean("nbt");
        ignoreMeta = tag.getBoolean("meta");
        useOreDict = tag.getBoolean("ore");
        this.listMode = WirelessMagnet.ListMode.values()[tag.getInteger("list")];
    }

    public void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        NBTTagCompound tmp = new NBTTagCompound();
        tmp.setBoolean("nbt", ignoreNbt);
        tmp.setBoolean("meta", ignoreMeta);
        tmp.setBoolean("ore", useOreDict);
        tmp.setInteger("list", this.listMode.ordinal());
        data.setTag(filterConfigKey, tmp);
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
        return useOreDict;
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
        useOreDict = useOre;
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
        return this;
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
            ItemStack newStack) {

    }

    @Override
    public void saveSettings() {
        super.saveSettings();
        writeToNBT();
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("config")) {
            return this.filterList;
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

    public List<ItemStack> getFilteredItems() {
        return WirelessMagnet.getFilteredItems(this.target);
    }

    public IItemList<IAEItemStack> getAEFilteredItems() {
        IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
        for (ItemStack is : this.getFilteredItems()) {
            list.add(AEApi.instance().storage().createItemStack(is));
        }
        return list;
    }

    public boolean isItemFiltered(ItemStack is, IItemList<IAEItemStack> list) {
        if (is == null && list.isEmpty()) return false;
        IAEItemStack ais = AEApi.instance().storage().createItemStack(is);
        for (IAEItemStack i : list) {
            if (useOreDict) {
                // use oredict
                return i.sameOre(ais);
            } else if (ignoreMeta && ignoreNbt) {
                // ignore meta & nbt
                return ais.getItem() == i.getItem();
            } else if (ignoreMeta) {
                // ignore meta only
                return ais.getItemStack().getTagCompound().equals(i.getItemStack().getTagCompound());
            } else if (ignoreNbt) {
                // ignore nbt only
                return i.getItemDamage() == ais.getItemDamage();
            } else {
                // ignore nothing/don't use oredict--must be exact match
                return ais.equals(i);
            }
        }
        return false;
    }
}
