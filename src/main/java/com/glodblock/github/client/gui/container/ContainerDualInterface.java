package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPrimaryGui;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.api.parts.IPart;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.IInterfaceHost;

public class ContainerDualInterface extends ContainerInterface {

    @GuiSync(11)
    public SidelessMode sidelessMode;

    private final boolean isTile;

    public ContainerDualInterface(InventoryPlayer ip, IInterfaceHost te) {
        super(ip, te);
        this.sidelessMode = SidelessMode.SIDELESS;
        this.isTile = te instanceof TileFluidInterface;
    }

    public SidelessMode getSidelessMode() {
        return this.sidelessMode;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        super.loadSettingsFromHost(cm);
        this.sidelessMode = this.isTile ? (SidelessMode) cm.getSetting(Settings.SIDELESS_MODE) : SidelessMode.SIDELESS;
    }

    @Override
    public PrimaryGui getPrimaryGui() {

        ContainerOpenContext context = getOpenContext();
        return new FluidPrimaryGui(
                GuiType.DUAL_INTERFACE_FLUID,
                getTarget() instanceof IPart ? ItemAndBlockHolder.FLUID_INTERFACE.stack()
                        : ItemAndBlockHolder.INTERFACE.stack(),
                context.getTile(),
                context.getSide());
    }
}
