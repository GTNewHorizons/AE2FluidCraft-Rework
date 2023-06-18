package com.glodblock.github.crossmod.extracells;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.common.item.ItemMultiFluidStorageCell;
import com.glodblock.github.crossmod.extracells.parts.*;
import com.glodblock.github.crossmod.extracells.storage.*;
import com.glodblock.github.loader.ItemAndBlockHolder;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;

/**
 * A shell class to organize proxy replacements and hide the ugliness
 */
public class ItemReplacements {

    protected static Map<String, Item> registry;

    static void postinit() {
        registry = new HashMap<>(23);
        ProxyFluidBusIO.init();
        ItemReplacements.proxyItems();
        ItemReplacements.proxyPartItems();
        if (Loader.isModLoaded("OpenComputers")) {
            deprecateOC();
        }
    }

    /**
     * Register proxy items (non-parts)
     */
    static void proxyItems() {
        deprecateFluidStorage("storage.fluid", 0, ItemAndBlockHolder.CELL1KM, 1, 8, 0.5);
        deprecateFluidStorage("storage.fluid", 1, ItemAndBlockHolder.CELL4KM, 4, 32, 1.0);
        deprecateFluidStorage("storage.fluid", 2, ItemAndBlockHolder.CELL16KM, 16, 128, 1.5);
        deprecateFluidStorage("storage.fluid", 3, ItemAndBlockHolder.CELL64KM, 64, 512, 2.0);
        deprecateFluidStorage("storage.fluid", 4, ItemAndBlockHolder.CELL256KM, 256, 2048, 2.5);
        deprecateFluidStorage("storage.fluid", 5, ItemAndBlockHolder.CELL1024KM, 1024, 8192, 3.0);
        deprecateFluidStorage("storage.fluid", 6, ItemAndBlockHolder.CELL4096KM, 4096, 32768, 3.5);
        deprecateFluidStorage("storage.fluid", 7, ItemAndBlockHolder.CELL16384KM, 16384, 131072, 4.0);
        // Thanks, AEApi.
        AEApi.instance().registries().cell().addCellHandler(new ProxyItemCellHandler());
        AEApi.instance().registries().cell().addCellHandler(new ProxyVoidCellHandler());
        deprecateItemStorage(0, AEApi.instance().definitions().items().cell256k(), 256, 2048, 2.5);
        deprecateItemStorage(1, AEApi.instance().definitions().items().cell1024k(), 1024, 8192, 3.0);
        deprecateItemStorage(2, AEApi.instance().definitions().items().cell4096k(), 4096, 32768, 3.5);
        deprecateItemStorage(3, AEApi.instance().definitions().items().cell16384k(), 16384, 131072, 4.0);
        deprecateItemStorage(4, AEApi.instance().definitions().items().cellContainer(), 65536, 8, 2.0, 1);
        deprecateExtremeStorage(
                "storage.physical.advanced.quantum",
                0,
                AEApi.instance().definitions().items().cellQuantum(),
                Integer.MAX_VALUE / 16,
                4096,
                1000.0);
        deprecateExtremeStorage(
                "storage.physical.advanced.singularity",
                0,
                AEApi.instance().definitions().items().cellSingularity(),
                Long.MAX_VALUE / 16,
                4096,
                15000.0);
        // Deprecate void cell manually
        ProxyVoidStorageCell voidCell = new ProxyVoidStorageCell();
        GameRegistry.registerItem(voidCell, "ec2placeholder.storage.physical.void");
        registry.put("extracells:storage.physical.void", voidCell);
        deprecateItem("pattern.fluid", ItemAndBlockHolder.PATTERN);
        deprecateWireless("terminal.fluid.wireless", ItemAndBlockHolder.WIRELESS_FLUID_TERM);

        /* Storage casings */
        deprecateItem("storage.casing", 0, AEApi.instance().definitions().materials().emptyAdvancedStorageCell());
        deprecateItem("storage.casing", 1, ItemAndBlockHolder.CELL_HOUSING, 2);

        /* Storage components (1k component, etc.) */
        deprecateItem("storage.component", 0, AEApi.instance().definitions().materials().cell256kPart());
        deprecateItem("storage.component", 1, AEApi.instance().definitions().materials().cell1024kPart());
        deprecateItem("storage.component", 2, AEApi.instance().definitions().materials().cell4096kPart());
        deprecateItem("storage.component", 3, AEApi.instance().definitions().materials().cell16384kPart());
        deprecateItem("storage.component", 4, ItemAndBlockHolder.CELL_PART, 0);
        deprecateItem("storage.component", 5, ItemAndBlockHolder.CELL_PART, 1);
        deprecateItem("storage.component", 6, ItemAndBlockHolder.CELL_PART, 2);
        deprecateItem("storage.component", 7, ItemAndBlockHolder.CELL_PART, 3);
        deprecateItem("storage.component", 8, ItemAndBlockHolder.CELL_PART, 4);
        deprecateItem("storage.component", 9, ItemAndBlockHolder.CELL_PART, 5);
        deprecateItem("storage.component", 10, ItemAndBlockHolder.CELL_PART, 6);
        deprecateWireless("terminal.universal.wireless", ItemAndBlockHolder.WIRELESS_ULTRA_TERM);

    }

    /**
     * Deprecate OC upgrades. Wrapper so we don't need a hard dependency
     */
    @Optional.Method(modid = "OpenComputers")
    static void deprecateOC() {
        ItemInfo info = Items.get("me_upgrade1");
        if (info != null) {
            // Note EC tier 1, 2, 3 corresponds to the correct meta equivalent in OC, so we can be lazy
            deprecateItem("oc.upgrade", 0, info.item(), 0);
            deprecateItem("oc.upgrade", 1, info.item(), 1);
            deprecateItem("oc.upgrade", 2, info.item(), 2);
        } else {
            System.out.println("OpenComputers detected, but could not replace items: me_upgrade1");
        }
    }

    /**
     * Register proxy part items.
     */
    static void proxyPartItems() {
        deprecateItemPart(0, ItemAndBlockHolder.FLUID_EXPORT_BUS, ProxyFluidBusIO::new);
        deprecateItemPart(1, ItemAndBlockHolder.FLUID_IMPORT_BUS, ProxyFluidBusIO::new);
        deprecateItemPart(2, ItemAndBlockHolder.FLUID_STORAGE_BUS, ProxyFluidStorage::new);
        deprecateItemPart(3, ItemAndBlockHolder.FLUID_TERM, ProxyPart::new);
        deprecateItemPart(4, ItemAndBlockHolder.FLUID_LEVEL_EMITTER, ProxyLevelEmitter::new);
        deprecateItemPart(9, ItemAndBlockHolder.FLUID_INTERFACE, ProxyFluidInterface::new);
        deprecateItemPart(10, ItemAndBlockHolder.FLUID_STORAGE_MONITOR, ProxyStorageMonitor::new);
        deprecateItemPart(11, ItemAndBlockHolder.FLUID_CONVERSION_MONITOR, ProxyStorageMonitor::new);
        deprecateItemPart(12, AEApi.instance().definitions().parts().exportBus(), ProxyOreDictExportBus::new);
    }

    private static ProxyItem getOrBuildItem(String srcName) {
        final String fullName = "extracells:" + srcName;
        ProxyItem proxy = (ProxyItem) registry.get(fullName);
        if (proxy == null) {
            proxy = new ProxyItem(srcName);
            registry.put(fullName, proxy);
            proxy.register();
        }
        return proxy;
    }

    private static ProxyFluidStorageCell getOrBuildFluidStorage(String srcName) {
        final String fullName = "extracells:" + srcName;
        ProxyFluidStorageCell proxy = (ProxyFluidStorageCell) registry.get(fullName);
        if (proxy == null) {
            proxy = new ProxyFluidStorageCell(srcName);
            registry.put(fullName, proxy);
            proxy.register();
        }
        return proxy;
    }

    private static ProxyItemStorageCell getOrBuildItemStorage() {
        final String fullName = "extracells:storage.physical";
        ProxyItemStorageCell proxy = (ProxyItemStorageCell) registry.get(fullName);
        if (proxy == null) {
            proxy = new ProxyItemStorageCell("storage.physical");
            registry.put(fullName, proxy);
            proxy.register();
        }
        return proxy;
    }

    private static ProxyExtremeStorageCell getOrBuildItemStorage(String srcName) {
        final String fullName = "extracells:" + srcName;
        ProxyExtremeStorageCell proxy = (ProxyExtremeStorageCell) registry.get(fullName);
        if (proxy == null) {
            proxy = new ProxyExtremeStorageCell(srcName);
            registry.put(fullName, proxy);
            proxy.register();
        }
        return proxy;
    }

    /**
     * Deprecate a simple item.
     *
     * @param srcName     name of the to-be-replaced item without the mod id prefix
     * @param srcMeta     meta of the to-be-replaced item
     * @param replacement item that will replace src
     * @param targetMeta  meta of the item that will replace src
     */
    private static void deprecateItem(String srcName, int srcMeta, Item replacement, int targetMeta) {
        getOrBuildItem(srcName).addMetaReplacement(srcMeta, replacement, targetMeta);
    }

    private static void deprecateItem(String srcName, Item replacement) {
        deprecateItem(srcName, 0, replacement, 0);
    }

    private static void deprecateItem(String srcName, int srcMeta, IItemDefinition replacement) {
        if (replacement.isEnabled()) {
            deprecateItem(
                    srcName,
                    srcMeta,
                    replacement.maybeItem().get(),
                    replacement.maybeStack(1).get().getItemDamage());
        }
    }

    private static void deprecateWireless(String srcName, ItemBaseWirelessTerminal replacement) {
        getOrBuildItem(srcName).addMetaReplacement(0, new ProxyItem.ProxyItemEntry(replacement, 0) {

            @Override
            NBTTagCompound replaceNBT(NBTTagCompound compound) {
                double power = compound.getDouble("power");
                compound.removeTag("power");
                compound.setDouble("internalCurrentPower", power);
                String key = compound.getString("key");
                compound.removeTag("key");
                compound.setString("encryptionKey", key);
                return compound;
            }
        });
    }

    private static void deprecateItemPart(int srcMeta, Item replacement,
            Function<ProxyPartItem, ProxyPart> partBuilder) {
        final String fullName = "extracells:part.base";
        ProxyPartItem proxyItem = (ProxyPartItem) registry.get(fullName);
        if (proxyItem == null) {
            proxyItem = new ProxyPartItem("part.base");
            registry.put(fullName, proxyItem);
            proxyItem.register();
        }
        proxyItem.addItemPart(srcMeta, replacement, partBuilder);
    }

    private static void deprecateItemPart(int srcMeta, IItemDefinition definition,
            Function<ProxyPartItem, ProxyPart> partBuilder) {
        if (definition.isEnabled()) {
            final String fullName = "extracells:part.base";
            ProxyPartItem proxyItem = (ProxyPartItem) registry.get(fullName);
            if (proxyItem == null) {
                proxyItem = new ProxyPartItem("part.base");
                registry.put(fullName, proxyItem);
                proxyItem.register();
            }
            proxyItem.addItemPart(srcMeta, definition, partBuilder);
        }
    }

    /**
     * Deprecate a fluid storage item. Note that we can't access the properties directly, so we need to do this
     */
    private static void deprecateFluidStorage(String srcName, int srcMeta, ItemMultiFluidStorageCell replacement,
            long kilobytes, int bytesPerType, double idleDrain) {
        ProxyFluidStorageCell proxyItem = getOrBuildFluidStorage(srcName);
        ProxyItem.ProxyStorageEntry entry = new ProxyItem.ProxyStorageEntry(
                replacement,
                0,
                kilobytes,
                bytesPerType,
                idleDrain);
        proxyItem.addMetaReplacement(srcMeta, entry);
    }

    /**
     * Deprecate a storage item. This goes through AEAPI, as AE2FC doesn't have any item cells. If the item is disabled
     * in AEAPI, the replacement will not be registered!
     */
    private static void deprecateItemStorage(int srcMeta, IItemDefinition replacement, long kilobytes, int bytesPerType,
            double idleDrain) {
        if (replacement.isEnabled()) {
            ProxyItemStorageCell item = getOrBuildItemStorage();
            int meta = replacement.maybeStack(1).get().getItemDamage();
            ProxyItem.ProxyItemEntry storage = new ProxyItem.ProxyStorageEntry(
                    replacement.maybeItem().get(),
                    meta,
                    kilobytes,
                    bytesPerType,
                    idleDrain);
            item.addMetaReplacement(srcMeta, storage);
        }
    }

    /**
     * Deprecate a storage item. This goes through AEAPI, as AE2FC doesn't have any item cells. If the item is disabled
     * in AEAPI, the replacement will not be registered!
     */
    private static void deprecateItemStorage(int srcMeta, IItemDefinition replacement, long kilobytes, int bytesPerType,
            double idleDrain, int types) {
        if (replacement.isEnabled()) {
            ProxyItemStorageCell item = getOrBuildItemStorage();
            int meta = replacement.maybeStack(1).get().getItemDamage();
            ProxyItem.ProxyItemEntry storage = new ProxyItem.ProxyStorageEntry(
                    replacement.maybeItem().get(),
                    kilobytes,
                    bytesPerType,
                    idleDrain,
                    types);
            item.addMetaReplacement(srcMeta, storage);
        }
    }

    private static void deprecateExtremeStorage(String srcName, int srcMeta, IItemDefinition replacement, long bytes,
            int bytesPerType, double idleDrain) {
        if (replacement.isEnabled()) {
            ProxyExtremeStorageCell item = getOrBuildItemStorage(srcName);
            int meta = replacement.maybeStack(1).get().getItemDamage();
            ProxyItem.ProxyItemEntry storage = new ProxyItem.ProxyStorageEntry(
                    replacement.maybeItem().get(),
                    meta,
                    bytes / 1024,
                    bytesPerType,
                    idleDrain);
            item.addMetaReplacement(srcMeta, storage);
        }
    }
}
