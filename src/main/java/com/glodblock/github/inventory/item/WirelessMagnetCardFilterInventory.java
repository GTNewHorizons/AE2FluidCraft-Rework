package com.glodblock.github.inventory.item;

import static com.glodblock.github.common.item.ItemMagnetCard.filterConfigKey;
import static com.glodblock.github.common.item.ItemMagnetCard.filterKey;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.glodblock.github.common.item.ItemMagnetCard;
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

public class WirelessMagnetCardFilterInventory extends BaseWirelessInventory implements IWirelessMagnetCardFilter {

    private boolean nbt;
    private boolean mata;
    private boolean ore;
    private ItemMagnetCard.ListMode listMode = ItemMagnetCard.ListMode.WhiteList;
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
        this.nbt = tag.getBoolean("nbt");
        this.mata = tag.getBoolean("meta");
        this.ore = tag.getBoolean("ore");
        this.listMode = ItemMagnetCard.ListMode.values()[tag.getInteger("list")];
    }

    public void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        NBTTagCompound tmp = new NBTTagCompound();
        tmp.setBoolean("nbt", this.nbt);
        tmp.setBoolean("meta", this.mata);
        tmp.setBoolean("ore", this.ore);
        tmp.setInteger("list", this.listMode.ordinal());
        data.setTag(filterConfigKey, tmp);
    }

    @Override
    public ItemMagnetCard.ListMode getListMode() {
        return this.listMode;
    }

    @Override
    public boolean getNBTMode() {
        return this.nbt;
    }

    @Override
    public boolean getMetaMode() {
        return this.mata;
    }

    @Override
    public boolean getOreMode() {
        return this.ore;
    }

    @Override
    public void setListMode(ItemMagnetCard.ListMode mode) {
        this.listMode = mode;
    }

    @Override
    public void setNBTMode(boolean ignoreNBT) {
        this.nbt = ignoreNBT;
    }

    @Override
    public void setMetaMode(boolean ignoreMeta) {
        this.mata = ignoreMeta;
    }

    @Override
    public void setOreMode(boolean useOre) {
        this.ore = useOre;
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
        return ItemMagnetCard.getFilteredItems(this.target);
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
            if (this.ore) {
                // use oredict
                return i.sameOre(ais);
            } else if (!this.mata && !this.nbt) {
                // ignore meta & nbt
                return Platform.isSameItem(i.getItemStack(), ais.getItemStack());
            } else if (!this.mata && this.nbt) {
                // ignore meta only
                return Platform.isSameItemPrecise(i.getItemStack(), ais.getItemStack());
            } else if (this.mata) {
                // ignore nbt only
                return i.getItemDamage() == ais.getItemDamage();
            } else {
                // ignore nothing/don't use oredict--must be exact match
                return list.findPrecise(ais) != null;
            }
        }
        return false;
    }
}
