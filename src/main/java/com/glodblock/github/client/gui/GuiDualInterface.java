package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.glodblock.github.client.gui.container.ContainerDualInterface;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;

import appeng.api.config.AdvancedBlockingMode;
import appeng.api.config.InsertionMode;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.SidelessMode;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiSimpleImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IInterfaceHost;

public class GuiDualInterface extends GuiUpgradeable {

    private GuiTabButton priority;
    private GuiTabButton switcher;
    private GuiImgButton BlockMode;
    private GuiImgButton SmartBlockMode;
    private GuiToggleButton interfaceMode;
    private GuiImgButton insertionMode;
    private GuiImgButton sidelessMode;
    private GuiSimpleImgButton doublePatterns;
    private GuiToggleButton patternOptimization;
    private GuiImgButton advancedBlockingMode;
    private GuiImgButton lockCraftingMode;
    private final IInterfaceHost host;

    public GuiDualInterface(InventoryPlayer inventoryPlayer, IInterfaceHost te) {
        super(new ContainerDualInterface(inventoryPlayer, te));
        this.host = te;
        this.ySize = 211;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addButtons() {
        this.priority = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                2 + 4 * 16,
                GuiText.Priority.getLocal(),
                itemRender);
        this.buttonList.add(this.priority);

        this.switcher = new GuiTabButton(
                this.guiLeft + 132,
                this.guiTop,
                host instanceof PartFluidInterface ? ItemAndBlockHolder.FLUID_INTERFACE.stack()
                        : ItemAndBlockHolder.INTERFACE.stack(),
                StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"),
                itemRender);
        this.buttonList.add(this.switcher);

        int offset = 8;

        this.BlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + offset, Settings.BLOCK, YesNo.NO);
        this.buttonList.add(this.BlockMode);

        this.SmartBlockMode = new GuiImgButton(this.guiLeft - 36, this.guiTop + offset, Settings.SMART_BLOCK, YesNo.NO);
        this.buttonList.add(this.SmartBlockMode);

        offset += 18;

        this.interfaceMode = new GuiToggleButton(
                this.guiLeft - 18,
                this.guiTop + offset,
                84,
                85,
                GuiText.InterfaceTerminal.getLocal(),
                GuiText.InterfaceTerminalHint.getLocal());
        this.buttonList.add(this.interfaceMode);

        offset += 18;

        this.insertionMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + offset,
                Settings.INSERTION_MODE,
                InsertionMode.DEFAULT);
        this.buttonList.add(this.insertionMode);

        offset += 18;

        this.doublePatterns = new GuiSimpleImgButton(this.guiLeft - 18, this.guiTop + offset, 71, "");
        this.doublePatterns.enabled = false;
        this.buttonList.add(this.doublePatterns);

        offset += 18;

        this.patternOptimization = new GuiToggleButton(
                this.guiLeft - 18,
                this.guiTop + offset,
                178,
                194,
                GuiText.PatternOptimization.getLocal(),
                GuiText.PatternOptimizationHint.getLocal());
        this.buttonList.add(this.patternOptimization);

        offset += 18;

        this.advancedBlockingMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + offset,
                Settings.ADVANCED_BLOCKING_MODE,
                AdvancedBlockingMode.DEFAULT);
        this.buttonList.add(this.advancedBlockingMode);
        this.advancedBlockingMode.visible = this.bc.getInstalledUpgrades(Upgrades.ADVANCED_BLOCKING) > 0;

        offset += 18;

        if (isTile()) {
            this.sidelessMode = new GuiImgButton(
                    this.guiLeft - 18,
                    this.guiTop + offset,
                    Settings.SIDELESS_MODE,
                    SidelessMode.SIDELESS);
            this.buttonList.add(this.sidelessMode);

            offset += 18;
        }

        this.lockCraftingMode = new GuiImgButton(
                this.guiLeft - 18,
                this.guiTop + offset,
                Settings.LOCK_CRAFTING_MODE,
                LockCraftingMode.NONE);
        this.lockCraftingMode.visible = this.bc.getInstalledUpgrades(Upgrades.LOCK_CRAFTING) > 0;
        this.buttonList.add(lockCraftingMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        if (this.BlockMode != null) {
            this.BlockMode.set(((ContainerInterface) this.cvb).getBlockingMode());
        }
        if (this.SmartBlockMode != null) {
            this.SmartBlockMode.set(((ContainerInterface) this.cvb).getSmartBlockingMode());
        }
        if (this.interfaceMode != null) {
            this.interfaceMode.setState(((ContainerInterface) this.cvb).getInterfaceTerminalMode() == YesNo.YES);
        }
        if (this.insertionMode != null) {
            this.insertionMode.set(((ContainerInterface) this.cvb).getInsertionMode());
        }
        if (this.doublePatterns != null) {
            this.doublePatterns.enabled = ((ContainerInterface) this.cvb).isAllowedToMultiplyPatterns;
            if (this.doublePatterns.enabled) this.doublePatterns.setTooltip(
                    ButtonToolTips.DoublePatterns.getLocal() + "\n" + ButtonToolTips.DoublePatternsHint.getLocal());
            else this.doublePatterns.setTooltip(
                    ButtonToolTips.DoublePatterns.getLocal() + "\n" + ButtonToolTips.OptimizePatternsNoReq.getLocal());
        }
        if (this.patternOptimization != null) {
            this.patternOptimization.setState(((ContainerInterface) this.cvb).getPatternOptimization() == YesNo.YES);
        }
        if (this.sidelessMode != null) {
            this.sidelessMode.set(((ContainerDualInterface) this.cvb).getSidelessMode());
        }
        if (this.advancedBlockingMode != null) {
            this.advancedBlockingMode.set(((ContainerDualInterface) this.cvb).getAdvancedBlockingMode());
        }
        if (this.lockCraftingMode != null) {
            this.lockCraftingMode.set(((ContainerInterface) this.cvb).getLockCraftingMode());
        }
        this.fontRendererObj.drawString(
                getGuiDisplayName(StatCollector.translateToLocal(NameConst.GUI_FLUID_INTERFACE)),
                8,
                6,
                4210752);
    }

    @Override
    protected String getBackground() {
        if (!ModAndClassUtil.isBigInterface) return "guis/interface.png";
        return switch (((ContainerInterface) this.cvb).getPatternCapacityCardsInstalled()) {
            case -1 -> "guis/interfacenone.png";
            case 1 -> "guis/interface2.png";
            case 2 -> "guis/interface3.png";
            case 3 -> "guis/interface4.png";
            default -> "guis/interface.png";
        };
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        final boolean backwards = Mouse.isButtonDown(1);
        if (btn == this.priority) {
            InventoryHandler.switchGui(GuiType.PRIORITY);
        } else if (btn == this.switcher) {
            InventoryHandler.switchGui(GuiType.DUAL_INTERFACE_FLUID);
        } else if (btn == this.interfaceMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(Settings.INTERFACE_TERMINAL, backwards));
        } else if (btn == this.BlockMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.BlockMode.getSetting(), backwards));
        } else if (btn == this.SmartBlockMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.SmartBlockMode.getSetting(), backwards));
        } else if (btn == this.insertionMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.insertionMode.getSetting(), backwards));
        } else if (btn == this.doublePatterns) {
            try {
                int val = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : 0;
                if (backwards) val |= 0b10;
                NetworkHandler.instance
                        .sendToServer(new PacketValueConfig("Interface.DoublePatterns", String.valueOf(val)));
            } catch (final Throwable e) {
                AELog.debug(e);
            }
        } else if (btn == this.patternOptimization) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(Settings.PATTERN_OPTIMIZATION, backwards));
        } else if (btn == this.sidelessMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.sidelessMode.getSetting(), backwards));
        } else if (btn == this.advancedBlockingMode) {
            NetworkHandler.instance
                    .sendToServer(new PacketConfigButton(this.advancedBlockingMode.getSetting(), backwards));
        } else if (btn == this.lockCraftingMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.lockCraftingMode.getSetting(), backwards));
        }
    }

    private boolean isPart() {
        return this.host instanceof PartFluidInterface;
    }

    private boolean isTile() {
        return this.host instanceof TileFluidInterface;
    }

    @Override
    protected void handleButtonVisibility() {
        super.handleButtonVisibility();
        if (this.advancedBlockingMode != null) {
            this.advancedBlockingMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.ADVANCED_BLOCKING) > 0);
        }
        if (this.lockCraftingMode != null) {
            this.lockCraftingMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.LOCK_CRAFTING) > 0);
        }
    }
}
