package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.client.gui.container.base.FCContainerFluidConfigurable;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFakeTypeOnly;
import com.glodblock.github.util.Ae2Reflect;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.guisync.GuiSync;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerFluidLevelEmitter extends FCContainerFluidConfigurable {

    private final PartFluidLevelEmitter lvlEmitter;

    @SideOnly(Side.CLIENT)
    private MEGuiTextField textField;

    @GuiSync(2)
    public long EmitterValue = -1;

    public ContainerFluidLevelEmitter(final InventoryPlayer ip, final PartFluidLevelEmitter te) {
        super(ip, te);
        this.lvlEmitter = te;
    }

    public PartFluidLevelEmitter getBus() {
        return this.lvlEmitter;
    }

    protected IUpgradeableHost getUpgradeable() {
        return Ae2Reflect.getUpgradeableHost(this);
    }

    public AppEngInternalAEInventory getFakeFluidInv() {
        return (AppEngInternalAEInventory) this.lvlEmitter.getInventoryByName("config");
    }

    @Override
    protected void setupConfig() {
        final IInventory inv = this.getUpgradeable().getInventoryByName("config");
        this.addSlotToContainer(new OptionalFluidSlotFakeTypeOnly(inv, null, this, 0, 17, 42, 0, 0, 0));
    }

    @SideOnly(Side.CLIENT)
    public void setTextField(final MEGuiTextField level) {
        this.textField = level;
    }

    public void setLevel(final long l, final EntityPlayer player) {
        this.lvlEmitter.setReportingValue(l);
        this.EmitterValue = l;
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 0;
    }

    public void setRedStoneMode(final RedstoneMode rsMode) {
        this.rsMode = rsMode;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.EmitterValue = this.lvlEmitter.getReportingValue();
            this.setRedStoneMode(
                    (RedstoneMode) Ae2Reflect.getUpgradeableHost(this).getConfigManager()
                            .getSetting(Settings.REDSTONE_EMITTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("EmitterValue")) {
            if (this.textField != null) {
                this.textField.setText(String.valueOf(this.EmitterValue));

                if (String.valueOf(oldValue).equals("-1")) {
                    this.textField.setCursorPositionEnd();
                }
            }
        }
    }
}
