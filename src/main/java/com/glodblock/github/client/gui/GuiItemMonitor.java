package com.glodblock.github.client.gui;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.base.FCGuiMonitor;
import com.glodblock.github.client.gui.container.ContainerItemMonitor;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.ItemRepo;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;

public class GuiItemMonitor extends FCGuiMonitor<IAEItemStack> {

    public GuiItemMonitor(final InventoryPlayer inventoryPlayer, final ITerminalHost te, final ContainerItemMonitor c) {
        super(inventoryPlayer, te, c);
        this.repo = new ItemRepo(getScrollBar(), this);
    }

    @Override
    public void postUpdate(final List<IAEItemStack> list) {
        for (final IAEItemStack is : list) {
            this.repo.postUpdate(is);
        }
        this.repo.updateView();
        this.setScrollBar();
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(this.getGuiDisplayName(GuiText.Terminal.getLocal()), 8, 6, 4210752);
        this.fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        if (ModAndClassUtil.isSearchBar && AEConfig.instance.preserveSearchBar && searchField != null)
            handleTooltip(mouseX, mouseY, searchField.new TooltipProvider());
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        setSearchString(displayName, true);
    }
}
