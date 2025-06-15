package com.glodblock.github.inventory.item;

import static com.glodblock.github.common.item.ItemBaseWirelessTerminal.restockItems;
import static com.glodblock.github.inventory.item.WirelessMagnet.modeKey;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.me.storage.NullInventory;
import appeng.util.Platform;

public abstract class BaseWirelessInventory extends MEMonitorHandler implements IWirelessTerminal, IWirelessExtendCard {

    protected final ItemStack target;
    protected final IAEItemPowerStorage ips;
    protected final int inventorySlot;

    protected final IGridNode grid;

    protected final PlayerSource source;
    protected final StorageChannel channel;
    protected WirelessMagnet.Mode magnetMode = WirelessMagnet.Mode.Off;
    protected EntityPlayer player;
    protected boolean restock;

    @SuppressWarnings("unchecked")
    public BaseWirelessInventory(final ItemStack is, final int slot, IGridNode gridNode, EntityPlayer player,
            StorageChannel channel) {
        super(Objects.requireNonNull(Util.getWirelessInv(is, player, channel)));
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.grid = gridNode;
        this.target = is;
        this.inventorySlot = slot;
        this.channel = channel;
        this.source = new PlayerSource(player, this);
        this.readFromNBT();
    }

    @SuppressWarnings("unchecked")
    public BaseWirelessInventory(final ItemStack is, final int slot, IGridNode gridNode, EntityPlayer player,
            StorageChannel channel, boolean nullInventory) {
        super(new NullInventory<>());
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.grid = gridNode;
        this.target = is;
        this.inventorySlot = slot;
        this.channel = channel;
        this.source = new PlayerSource(player, this);
        this.readFromNBT();
    }

    private void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        this.setMagnetCardMode(WirelessMagnet.Mode.getModes()[data.getInteger(modeKey)]);
        this.setRestock(data.getBoolean(ItemBaseWirelessTerminal.restockItems));
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
    public int getInventorySlot() {
        return this.inventorySlot;
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
    public void saveChanges() {}

    @Override
    public IGridNode getActionableNode() {
        return this.grid;
    }

    @Override
    public PlayerSource getActionSource() {
        return this.source;
    }

    @Override
    public void setMagnetCardMode(WirelessMagnet.Mode mode) {
        this.magnetMode = mode;
    }

    @Override
    public WirelessMagnet.Mode getMagnetCardMode() {
        return this.magnetMode;
    }

    public void setMagnetCardNextMode() {
        final WirelessMagnet.Mode[] MODES = WirelessMagnet.Mode.getModes();
        setMagnetCardMode(MODES[(getMagnetCardMode().ordinal() + 1) % MODES.length]);
    }

    public void setRestock(boolean val) {
        this.restock = val;
    }

    public boolean isRestock() {
        return this.restock;
    }

    public IAEStack injectItems(IAEStack aeStack) {
        try {
            if (aeStack.isItem()) {
                return ((IStorageGrid) getActionableNode().getGrid().getCache(IStorageGrid.class)).getItemInventory()
                        .injectItems((IAEItemStack) aeStack, Actionable.MODULATE, getActionSource());
            } else {
                return ((IStorageGrid) getActionableNode().getGrid().getCache(IStorageGrid.class)).getFluidInventory()
                        .injectItems((IAEFluidStack) aeStack, Actionable.MODULATE, getActionSource());
            }
        } catch (Exception e) {
            return aeStack;
        }
    }

    public IAEStack extractItems(IAEStack aeStack) {
        try {
            if (aeStack.isItem()) {
                return ((IStorageGrid) getActionableNode().getGrid().getCache(IStorageGrid.class)).getItemInventory()
                        .extractItems((IAEItemStack) aeStack, Actionable.MODULATE, getActionSource());
            } else {
                return ((IStorageGrid) getActionableNode().getGrid().getCache(IStorageGrid.class)).getFluidInventory()
                        .extractItems((IAEFluidStack) aeStack, Actionable.MODULATE, getActionSource());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void saveSettings() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        data.setBoolean(restockItems, this.restock);
        data.setInteger(modeKey, this.getMagnetCardMode().ordinal());
    }
}
