package com.glodblock.github.inventory.item;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class WirelessCraftingTerminalInventory extends MEMonitorHandler<IAEItemStack>
        implements IWirelessCraftTerminal {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;
    private final AppEngInternalInventory viewCell;
    private final StorageChannel channel;
    private final IGridNode grid;

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;

    @SuppressWarnings("unchecked")
    public WirelessCraftingTerminalInventory(final ItemStack is, final int slot, IGridNode gridNode,
            EntityPlayer player) {
        super(
                (IMEInventoryHandler<IAEItemStack>) Objects
                        .requireNonNull(Util.getWirelessInv(is, player, StorageChannel.ITEMS)));
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.grid = gridNode;
        this.target = is;
        this.inventorySlot = slot;
        this.viewCell = new WirelessTerminalViewCells(is);
        this.channel = StorageChannel.ITEMS;
        this.crafting = new ItemBiggerAppEngInventory(is, "crafting_c", 9);
        this.output = new ItemBiggerAppEngInventory(is, "output_c", 3);
    }

    public StorageChannel getChannel() {
        return this.channel;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt));
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
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.target);
            manager.writeToNBT(data);
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        out.readFromNBT((NBTTagCompound) Platform.openNbtData(this.target).copy());
        return out;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public IInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.grid;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN).getMachine().securityBreak();
    }

    @Override
    public void saveChanges() {
        // NO-OP
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {
        // NO-OP
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        return null;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.grid;
    }
}
