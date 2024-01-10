package com.glodblock.github.proxy;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemMagnetCard;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.common.tile.TileWalrus;
import com.glodblock.github.crossmod.extracells.EC2Replacer;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.inventory.external.AEFluidInterfaceHandler;
import com.glodblock.github.inventory.item.WirelessMagnetCardFilterInventory;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.wrapper.FCNetworkWrapper;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class CommonProxy {

    public final FCNetworkWrapper netHandler = new FCNetworkWrapper(FluidCraft.MODID);

    public CommonProxy() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void pickupEvent(EntityItemPickupEvent e) {
        try {
            if (Platform.isClient() || e.entityPlayer == null) return;
            EntityPlayer player = e.entityPlayer;
            EntityItem itemEntity = e.item;
            ItemStack stack = itemEntity.getEntityItem();
            World world = player.getEntityWorld();

            ImmutablePair<Integer, ItemStack> result = Util.Wireless.getUltraWirelessTerm(player);
            if (result == null) return;
            final ItemStack wirelessTerm = result.getRight();
            ItemMagnetCard.Mode mode = ItemMagnetCard.getMode(wirelessTerm);
            if (!Util.Wireless.hasMagnetCard(wirelessTerm) || mode != ItemMagnetCard.Mode.ME) return;
            IGridNode gridNode = Util.Wireless.getWirelessGrid(wirelessTerm);
            if (gridNode == null || !Util.Wireless.rangeCheck(wirelessTerm, player, gridNode)) return;
            WirelessMagnetCardFilterInventory inv = new WirelessMagnetCardFilterInventory(
                    wirelessTerm,
                    result.getLeft(),
                    gridNode,
                    player);

            IItemList<IAEItemStack> filteredList = inv.getAEFilteredItems();
            IAEItemStack ais = AEApi.instance().storage().createItemStack(stack);
            if (inv.getListMode() == ItemMagnetCard.ListMode.WhiteList) { // whitelisting
                if (!filteredList.isEmpty() && inv.isItemFiltered(stack, filteredList)) {
                    if (inv.doInject(ais, itemEntity, world)) {
                        stack = null;
                        itemEntity.setDead();
                        e.setCanceled(true);
                    }
                }
            } else if (inv.getListMode() == ItemMagnetCard.ListMode.BlackList) {
                if (!inv.isItemFiltered(stack, filteredList)) {
                    if (inv.doInject(ais, itemEntity, world)) {
                        stack = null;
                        itemEntity.setDead();
                        e.setCanceled(true);
                    }
                }
            }

        } catch (NullPointerException ex) {
            AELog.error(ex);
        }
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.PlayerTickEvent e) {
        EntityPlayer player = e.player;
        ImmutablePair<Integer, ItemStack> result = Util.Wireless.getUltraWirelessTerm(player);
        if (result == null) return;
        final ItemStack wirelessTerm = result.getRight();
        if (!Util.Wireless.hasMagnetCard(wirelessTerm)
                || ItemMagnetCard.getMode(wirelessTerm) == ItemMagnetCard.Mode.Off)
            return;
        IGridNode gridNode = Util.Wireless.getWirelessGrid(wirelessTerm);
        if (gridNode == null) return;
        WirelessMagnetCardFilterInventory inv = new WirelessMagnetCardFilterInventory(
                wirelessTerm,
                result.getLeft(),
                gridNode,
                player);
        ItemMagnetCard.doMagnet(wirelessTerm, e.player.worldObj, e.player, inv);

    }

    public void preInit(FMLPreInitializationEvent event) {
        ModAndClassUtil.init();
        ItemAndBlockHolder.init();
    }

    public void init(FMLInitializationEvent event) {
        this.registerMovables();
        if (ModAndClassUtil.ThE) {
            AspectUtil.init();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (!ModAndClassUtil.EC2 && Config.replaceEC2) {
            EC2Replacer.initReplacer();
        }
        if (ModAndClassUtil.isBigInterface) {
            Upgrades.PATTERN_CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 3);
            Upgrades.PATTERN_CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 3);
            Upgrades.PATTERN_CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE_P2P), 3);
        }
        Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 1);
        Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 1);
        Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE_P2P), 1);
        Upgrades.ADVANCED_BLOCKING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 1);
        Upgrades.ADVANCED_BLOCKING.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 1);
        Upgrades.LOCK_CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 1);
        Upgrades.LOCK_CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 1);
        Upgrades.LOCK_CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE_P2P), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_STORAGE_BUS), 5);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_STORAGE_BUS), 1);

        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL1K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL4K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL16K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL64K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL256K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL1024K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL4096K), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL16384K), 1);

        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL1KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL4KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL16KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL64KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL256KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL1024KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL4096KM), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlockHolder.CELL16384KM), 1);

        if (Config.fluidIOBus) {
            Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 2);
            Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 2);
            Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 1);
            Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 1);
            Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 4);
            Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 4);
            Upgrades.SUPERSPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 4);
            Upgrades.SUPERSPEED.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_IMPORT_BUS), 4);
            Upgrades.CRAFTING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_EXPORT_BUS), 1);
        }
        AEApi.instance().registries().externalStorage().addExternalStorageInterface(new AEFluidInterfaceHandler());
        AEApi.instance().registries().interfaceTerminal().register(PartFluidP2PInterface.class);
        AEApi.instance().registries().interfaceTerminal().register(PartFluidInterface.class);
        AEApi.instance().registries().interfaceTerminal().register(TileFluidInterface.class);
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {}

    public void registerRenderers() {}

    public void registerMovables() {
        IAppEngApi api = AEApi.instance();
        api.registries().movable().whiteListTileEntity(TileWalrus.class);
    }
}
