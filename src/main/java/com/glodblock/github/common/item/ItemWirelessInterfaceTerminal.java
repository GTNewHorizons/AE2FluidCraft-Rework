package com.glodblock.github.common.item;

import java.util.EnumSet;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.helpers.WirelessInterfaceTerminalGuiObject;
import appeng.helpers.WirelessPatternTerminalGuiObject;
import com.glodblock.github.util.UltraTerminalModes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.item.WirelessInterfaceTerminalInventory;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemWirelessInterfaceTerminal extends ItemBaseWirelessTerminal
        implements IRegister<ItemWirelessInterfaceTerminal> {

    public ItemWirelessInterfaceTerminal() {
        super(GuiBridge.GUI_INTERFACE_TERMINAL);
        AEApi.instance().registries().wireless().registerWirelessHandler(this);
        this.setFeature(EnumSet.of(AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools));
        setUnlocalizedName(NameConst.ITEM_WIRELESS_INTERFACE_TERMINAL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_WIRELESS_INTERFACE_TERMINAL).toString());
    }

    @Override
    public ItemWirelessInterfaceTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_INTERFACE_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public IGuiItemObject getGuiObject(ItemStack is, World world, int x, int y, int z) {
        final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(is);
        if (wh == null) return null;
        return new WirelessInterfaceTerminalGuiObject(wh, is);
    }
}
