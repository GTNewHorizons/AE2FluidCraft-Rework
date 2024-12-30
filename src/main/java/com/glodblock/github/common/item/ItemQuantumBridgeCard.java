package com.glodblock.github.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemQuantumBridgeCard extends FCBaseItem {

    public ItemQuantumBridgeCard() {
        super();
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_QUANTUM_BRIDGE_CARD);
        this.setTextureName(FluidCraft.resource(NameConst.ITEM_QUANTUM_BRIDGE_CARD).toString());
    }

    @Override
    public ItemQuantumBridgeCard register() {
        GameRegistry.registerItem(this, NameConst.ITEM_QUANTUM_BRIDGE_CARD, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> toolTip, boolean flag) {
        if (isShiftKeyDown()) {
            toolTip.addAll(
                    RenderUtil.listFormattedStringToWidth(NameConst.i18n(NameConst.TT_QUANTUM_BRIDGE_CARD_DESC)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
