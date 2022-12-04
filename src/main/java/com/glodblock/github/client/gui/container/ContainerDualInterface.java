package com.glodblock.github.client.gui.container;

import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDualInterface extends ContainerInterface {

    @GuiSync(10)
    public SidelessMode sidelessMode;

    public ContainerDualInterface(InventoryPlayer ip, IInterfaceHost te) {
        super(ip, te);
        this.sidelessMode = SidelessMode.SIDELESS;
    }

    public SidelessMode getSidelessMode() {
        return this.sidelessMode;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        super.loadSettingsFromHost(cm);
        this.sidelessMode = (SidelessMode) cm.getSetting(Settings.SIDELESS_MODE);
    }
}
