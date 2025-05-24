package com.glodblock.github.client.gui.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.api.registries.LevelItemInfo;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.coremod.registries.LevelTerminalRegistry;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.SPacketLevelTerminalUpdate;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.DimensionalCoord;
import appeng.util.Platform;

public class ContainerLevelTerminal extends FCBaseContainer {

    /**
     * this stuff is all server side
     */
    private int nextId = 0;

    private final Map<IGridHost, ContainerLevelTerminal.InvTracker> tracked = new HashMap<>();
    private final Map<Long, ContainerLevelTerminal.InvTracker> trackedById = new HashMap<>();

    private IGrid grid;
    private SPacketLevelTerminalUpdate updatePacket;
    private boolean isPacketDirty;
    private boolean wasOff;
    private int ticks = 0;

    public ContainerLevelTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        if (Platform.isServer()) {
            if (monitorable instanceof PartLevelTerminal terminal) {
                grid = terminal.getActionableNode().getGrid();
            } else if (monitorable instanceof IWirelessTerminal wirelessTerminal) {
                grid = wirelessTerminal.getActionableNode().getGrid();
            }

            updatePacket = this.updateList();
            if (updatePacket != null) {
                this.isPacketDirty = true;
            } else {
                updatePacket = new SPacketLevelTerminalUpdate();
                this.isPacketDirty = false;
            }
        }

        this.bindPlayerInventory(ip, 14, 0);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();

        if (this.grid == null) {
            return;
        }

        final IActionHost host = this.getActionHost();

        if (!host.getActionableNode().isActive()) {
            if (!wasOff) {
                SPacketLevelTerminalUpdate update = new SPacketLevelTerminalUpdate();

                update.addDisconnectEntry();
                wasOff = true;
                FluidCraft.proxy.netHandler.sendTo(update, (EntityPlayerMP) this.getPlayerInv().player);
            }
            return;
        }
        wasOff = false;

        if (isPacketDirty) {
            FluidCraft.proxy.netHandler.sendTo(updatePacket, (EntityPlayerMP) this.getPlayerInv().player);
            updatePacket = new SPacketLevelTerminalUpdate();
            isPacketDirty = false;
        } else if (++ticks % 20 == 0) {
            ticks = 0;
            SPacketLevelTerminalUpdate updateNew = this.updateList();
            if (updateNew != null) {
                FluidCraft.proxy.netHandler.sendTo(updateNew, (EntityPlayerMP) this.getPlayerInv().player);
            }
        }
    }

    @Nullable
    private SPacketLevelTerminalUpdate updateList() {
        SPacketLevelTerminalUpdate update = null;
        var supported = LevelTerminalRegistry.instance().getSupportedClasses();
        Set<IGridHost> visited = new HashSet<>();

        for (Class<? extends ILevelViewable> clz : supported) {
            boolean isAdopted = LevelTerminalRegistry.instance().isAdopted(clz);
            Class<? extends IGridHost> machineClass = isAdopted ? LevelTerminalRegistry.instance().getAdopted(clz)
                    : clz;
            for (IGridNode gridNode : grid.getMachines(machineClass)) {
                final IGridHost gridHost = gridNode.getMachine();
                final ILevelViewable machine = isAdopted
                        ? LevelTerminalRegistry.instance().getAdapter(clz).adapt(gridHost)
                        : (ILevelViewable) gridHost;
                visited.add(gridHost);

                /* First check if we are already tracking this node */
                if (tracked.containsKey(gridHost)) {
                    ContainerLevelTerminal.InvTracker knownTracker = tracked.get(gridHost);

                    /* Name changed? */
                    String name = machine.getCustomName();
                    if (!Objects.equals(knownTracker.name, name)) {
                        knownTracker.name = name;
                        if (update == null) update = new SPacketLevelTerminalUpdate();
                        update.addRenameEntry(knownTracker.id, name);
                    }

                    LevelItemInfo[] prev = knownTracker.infoList;
                    LevelItemInfo[] curr = machine.getLevelItemInfoList();
                    if (prev.length == curr.length) {
                        for (int i = 0; i < prev.length; i++) {
                            if (prev[i] == null && curr[i] == null) continue;

                            if (prev[i] == null) {
                                knownTracker.infoList[i] = curr[i];
                                if (update == null) update = new SPacketLevelTerminalUpdate();
                                update.addOverwriteSlotEntry(knownTracker.id, i, curr[i]);
                            } else if (curr[i] == null) {
                                knownTracker.infoList[i] = null;
                                if (update == null) update = new SPacketLevelTerminalUpdate();
                                update.addOverwriteSlotEntry(knownTracker.id, i, null);
                            } else if (!ItemStack.areItemStacksEqual(prev[i].stack, curr[i].stack)
                                    || prev[i].quantity == curr[i].quantity
                                    || prev[i].batchSize == curr[i].batchSize
                                    || prev[i].state == curr[i].state) {
                                        knownTracker.infoList[i] = curr[i];
                                        if (update == null) update = new SPacketLevelTerminalUpdate();
                                        update.addOverwriteSlotEntry(knownTracker.id, i, curr[i]);
                                    }
                        }
                    } else {
                        knownTracker.infoList = curr;
                        if (update == null) update = new SPacketLevelTerminalUpdate();
                        update.addOverwriteAllSlotEntry(knownTracker.id, curr);
                    }
                } else {
                    /* Add a new entry */
                    if (update == null) update = new SPacketLevelTerminalUpdate();
                    ContainerLevelTerminal.InvTracker entry = new ContainerLevelTerminal.InvTracker(nextId++, machine);

                    update.addNewEntry(
                            entry.id,
                            entry.x,
                            entry.y,
                            entry.z,
                            entry.dim,
                            entry.side.ordinal(),
                            entry.rows,
                            entry.rowSize,
                            entry.name,
                            entry.infoList);
                    tracked.put(gridHost, entry);
                    trackedById.put(entry.id, entry);
                }
            }
        }

        /* Now find any entries that we need to remove */
        Iterator<Map.Entry<IGridHost, ContainerLevelTerminal.InvTracker>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (visited.contains(entry.getKey())) {
                continue;
            }

            if (update == null) update = new SPacketLevelTerminalUpdate();
            update.addRemoveEntry(entry.getValue().id);
            trackedById.remove(entry.getValue().id);
            it.remove();
        }
        return update;
    }

    @Override
    protected boolean isWirelessTerminal() {
        return false;
    }

    private static class InvTracker {

        private final long id;
        private String name;
        private final int rowSize;
        private final int rows;
        private final int x;
        private final int y;
        private final int z;
        private final int dim;
        private final ForgeDirection side;

        private LevelItemInfo[] infoList;

        public InvTracker(long id, ILevelViewable machine) {

            DimensionalCoord location = machine.getLocation();

            this.id = id;
            this.name = machine.getCustomName();
            this.rowSize = machine.rowSize();
            this.rows = machine.rows();
            this.x = location.x;
            this.y = location.y;
            this.z = location.z;
            this.dim = location.getDimension();
            this.side = machine.getSide();

            this.infoList = machine.getLevelItemInfoList();
        }
    }
}
