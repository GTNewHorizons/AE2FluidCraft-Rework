package com.glodblock.github.proxy;

import net.minecraft.item.ItemStack;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.common.tile.TileWalrus;
import com.glodblock.github.crossmod.extracells.EC2Replacer;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.inventory.external.AEFluidInterfaceHandler;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.SPacketMEUpdateBuffer;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.Upgrades;
import appeng.helpers.InterfaceTerminalSupportedClassProvider;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class CommonProxy {

    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(FluidCraft.MODID);

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {
        this.registerMovables();
        FMLCommonHandler.instance().bus().register(SPacketMEUpdateBuffer.class);
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
        Upgrades.ADVANCED_BLOCKING.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE), 1);
        Upgrades.ADVANCED_BLOCKING.registerItem(new ItemStack(ItemAndBlockHolder.INTERFACE), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_STORAGE_BUS), 5);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlockHolder.FLUID_STORAGE_BUS), 1);
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
        InterfaceTerminalSupportedClassProvider.register(PartFluidP2PInterface.class);
        InterfaceTerminalSupportedClassProvider.register(PartFluidInterface.class);
        InterfaceTerminalSupportedClassProvider.register(TileFluidInterface.class);
    }

    public void registerRenderers() {}

    public void registerMovables() {
        IAppEngApi api = AEApi.instance();
        api.registries().movable().whiteListTileEntity(TileWalrus.class);
    }
}
