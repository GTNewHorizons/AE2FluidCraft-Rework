package com.glodblock.github.client.gui.container.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;

import com.glodblock.github.inventory.item.BaseWirelessInventory;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.SPacketMEUpdateBuffer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import appeng.api.config.Actionable;
import appeng.api.config.CraftingAllow;
import appeng.api.config.PinsState;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.ITerminalPins;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPinsHandler;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.PinsHandler;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public abstract class FCContainerMonitor<T extends IAEStack<T>> extends FCBaseContainer
        implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver, IPinsHandler {

    protected final SlotRestrictedInput[] cellView = new SlotRestrictedInput[5];
    protected final IConfigManager clientCM;
    protected final ITerminalHost host;

    protected PinsHandler pinsHandler = null;

    @GuiSync(99)
    public boolean canAccessViewCells = false;

    @GuiSync(98)
    public boolean hasPower = false;

    protected IConfigManagerHost gui;
    protected IConfigManager serverCM;
    protected IGridNode networkNode;
    protected IMEMonitor<T> monitor;

    public FCContainerMonitor(final InventoryPlayer ip, final ITerminalHost monitorable) {
        this(ip, monitorable, true);
    }

    protected FCContainerMonitor(final InventoryPlayer ip, final ITerminalHost monitorable,
            final boolean bindInventory) {
        super(ip, monitorable);
        this.host = monitorable;
        this.clientCM = new ConfigManager(this);
        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.clientCM.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        this.clientCM.registerSetting(Settings.PINS_STATE, PinsState.DISABLED); // use for GUI

        if (Platform.isServer()) {
            if (monitorable instanceof ITerminalPins t && !(monitorable instanceof BaseWirelessInventory bwi
                    && bwi.getChannel() == StorageChannel.FLUIDS)) {
                pinsHandler = t.getPinsHandler(ip.player);
            }
        }
    }

    public IMEMonitor<T> getMonitor() {
        return this.monitor;
    }

    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (isInvalid()) {
                this.setValidContainer(false);
            }
            if (this.serverCM != null) {
                for (final Settings set : this.serverCM.getSettings()) {
                    final Enum<?> sideLocal = this.serverCM.getSetting(set);
                    final Enum<?> sideRemote = this.clientCM.getSetting(set);

                    if (sideLocal != sideRemote) {
                        this.clientCM.putSetting(set, sideLocal);
                        for (final Object crafter : this.crafters) {
                            try {
                                NetworkHandler.instance.sendTo(
                                        new PacketValueConfig(set.name(), sideLocal.name()),
                                        (EntityPlayerMP) crafter);
                            } catch (final IOException e) {
                                AELog.debug(e);
                            }
                        }
                    }
                }
            }

            if (pinsHandler != null) {
                if (clientCM.getSetting(Settings.PINS_STATE) != pinsHandler.getPinsState()) {
                    this.clientCM.putSetting(Settings.PINS_STATE, pinsHandler.getPinsState());
                    updatePins(true);
                } else {
                    updatePins(false);
                }
            }

            processItemList();
            this.updatePowerStatus();
            final boolean oldAccessible = this.canAccessViewCells;
            this.canAccessViewCells = this.host instanceof WirelessTerminalGuiObject
                    || this.host instanceof IWirelessTerminal
                    || this.hasAccess(SecurityPermissions.BUILD, false);
            if (this.canAccessViewCells != oldAccessible) {
                for (int y = 0; y < 5; y++) {
                    if (this.cellView[y] != null) {
                        this.cellView[y].setAllowEdit(this.canAccessViewCells);
                    }
                }
            }
            super.detectAndSendChanges();
        }
    }

    private int lastUpdate = 0;

    public void updatePins(boolean forceUpdate) {
        if (pinsHandler == null || !(host instanceof ITerminalPins itp)) return;

        boolean isActive = pinsHandler.getPinsState() != PinsState.DISABLED;
        ++lastUpdate;
        if (!forceUpdate && lastUpdate <= 20) return;
        lastUpdate = 0;
        if (isActive) {
            final ICraftingGrid cc = itp.getGrid().getCache(ICraftingGrid.class);
            final ImmutableList<ICraftingCPU> cpuList = cc.getCpus().asList();

            List<IAEStack<?>> craftedItems = new ArrayList<>();

            // fetch the first available crafting output
            for (int i = 0; i < cpuList.size(); i++) {
                ICraftingCPU cpu = cpuList.get(i);
                if (cpu.getCraftingAllowMode() != CraftingAllow.ONLY_NONPLAYER && cpu.getFinalOutput() != null
                        && cpu.getCurrentJobSource() instanceof PlayerSource src
                        && src.player == pinsHandler.getPlayer()) {
                    if (craftedItems.contains(cpu.getFinalOutput())) {
                        continue; // skip if already added
                    }
                    if (cpu.isBusy()) craftedItems.add(0, cpu.getFinalOutput().copy());
                    else craftedItems.add(cpu.getFinalOutput().copy());
                }
            }

            pinsHandler.addItemsToPins(craftedItems);
        }
        pinsHandler.update(forceUpdate);
        onListUpdate(); // notify the repo that the pins have changed
    }

    @Override
    public void setPin(IAEStack<?> is, int idx) {
        if (pinsHandler == null || !(host instanceof ITerminalPins itp)) return;

        if (is == null) {
            final ICraftingGrid cc = itp.getGrid().getCache(ICraftingGrid.class);
            final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();
            for (ICraftingCPU cpu : cpuSet) {
                if (cpu.getCraftingAllowMode() != CraftingAllow.ONLY_NONPLAYER && cpu.getFinalOutput() != null
                        && cpu.getFinalOutput().isSameType(getPin(idx))) {
                    if (!cpu.isBusy()) {
                        cpu.resetFinalOutput();
                    } else {
                        return;
                    }
                }
            }
        }
        pinsHandler.setPin(idx, is);
        updatePins(true);
    }

    @Override
    public IAEStack<?> getPin(int idx) {
        if (pinsHandler == null) return null;
        return pinsHandler.getPin(idx);
    }

    public void setPinsState(PinsState pinsState) {
        if (pinsHandler == null) return;
        clientCM.putSetting(Settings.PINS_STATE, pinsState);
        pinsHandler.setPinsState(pinsState);
        updatePins(true);
    }

    public PinsState getPinsState() {
        return pinsHandler != null ? pinsHandler.getPinsState() : PinsState.DISABLED;
    }

    public PinsHandler getPinsHandler() {
        return pinsHandler;
    }

    protected abstract void processItemList();

    protected boolean isInvalid() {
        return this.monitor == null;
    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(
                        this.getPowerSource().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable ignore) {}
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("canAccessViewCells")) {
            for (int y = 0; y < 5; y++) {
                if (this.cellView[y] != null) {
                    this.cellView[y].setAllowEdit(this.canAccessViewCells);
                }
            }
        }
        super.onUpdate(field, oldValue, newValue);
    }

    @Override
    public void addCraftingToCrafters(final ICrafting c) {
        super.addCraftingToCrafters(c);
        this.queueInventory(c);
    }

    protected abstract void queueInventory(final ICrafting c);

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
            if (player instanceof EntityPlayerMP && Platform.isServer()) {
                SPacketMEUpdateBuffer.clear((EntityPlayerMP) player);
            }

        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public ItemStack[] getViewCells() {
        final ItemStack[] list = new ItemStack[this.cellView.length];
        for (int x = 0; x < this.cellView.length; x++) {
            list[x] = this.cellView[x].getStack();
        }
        return list;
    }

    public SlotRestrictedInput getCellViewSlot(final int index) {
        return this.cellView[index];
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    protected void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    protected IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void onListUpdate() {
        for (final Object c : this.crafters) {
            if (c instanceof final ICrafting cr) {
                this.queueInventory(cr);
            }
        }
    }

    @Override
    public void updateSetting(final IConfigManager manager, @SuppressWarnings("rawtypes") final Enum settingName,
            @SuppressWarnings("rawtypes") final Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }
}
