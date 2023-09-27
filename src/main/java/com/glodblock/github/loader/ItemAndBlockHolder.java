package com.glodblock.github.loader;

import static com.glodblock.github.common.storage.FluidCellInventory.singleByteAmount;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.block.BlockCertusQuartzTank;
import com.glodblock.github.common.block.BlockFluidAutoFiller;
import com.glodblock.github.common.block.BlockFluidBuffer;
import com.glodblock.github.common.block.BlockFluidDiscretizer;
import com.glodblock.github.common.block.BlockFluidInterface;
import com.glodblock.github.common.block.BlockFluidPacketDecoder;
import com.glodblock.github.common.block.BlockFluidPatternEncoder;
import com.glodblock.github.common.block.BlockIngredientBuffer;
import com.glodblock.github.common.block.BlockLargeIngredientBuffer;
import com.glodblock.github.common.block.BlockLevelMaintainer;
import com.glodblock.github.common.block.BlockOCPatternEditor;
import com.glodblock.github.common.block.BlockWalrus;
import com.glodblock.github.common.item.ItemBasicFluidStorageCell;
import com.glodblock.github.common.item.ItemBasicFluidStoragePart;
import com.glodblock.github.common.item.ItemCreativeFluidStorageCell;
import com.glodblock.github.common.item.ItemFluidConversionMonitor;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidExportBus;
import com.glodblock.github.common.item.ItemFluidExtremeStorageCell;
import com.glodblock.github.common.item.ItemFluidImportBus;
import com.glodblock.github.common.item.ItemFluidLevelEmitter;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemFluidStorageHousing;
import com.glodblock.github.common.item.ItemFluidStorageMonitor;
import com.glodblock.github.common.item.ItemMultiFluidStorageCell;
import com.glodblock.github.common.item.ItemPartFluidInterface;
import com.glodblock.github.common.item.ItemPartFluidP2PInterface;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminal;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminalEx;
import com.glodblock.github.common.item.ItemPartFluidStorageBus;
import com.glodblock.github.common.item.ItemPartFluidTerminal;
import com.glodblock.github.common.item.ItemPortableFluidCell;
import com.glodblock.github.common.item.ItemWirelessFluidTerminal;
import com.glodblock.github.common.item.ItemWirelessInterfaceTerminal;
import com.glodblock.github.common.item.ItemWirelessPatternTerminal;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.util.NameConst;

public class ItemAndBlockHolder {

    public static BlockCertusQuartzTank CERTUS_QUARTZ_TANK = new BlockCertusQuartzTank().register();
    public static BlockFluidAutoFiller FLUID_AUTO_FILLER = new BlockFluidAutoFiller().register();
    public static BlockFluidDiscretizer DISCRETIZER = new BlockFluidDiscretizer().register();
    public static BlockLevelMaintainer LEVEL_MAINTAINER = new BlockLevelMaintainer().register();
    public static BlockFluidPatternEncoder ENCODER = new BlockFluidPatternEncoder().register();
    public static BlockFluidPacketDecoder DECODER = new BlockFluidPacketDecoder().register();
    public static BlockFluidInterface INTERFACE = new BlockFluidInterface().register();
    public static BlockIngredientBuffer BUFFER = new BlockIngredientBuffer().register();
    public static BlockFluidBuffer FLUID_BUFFER = new BlockFluidBuffer().register();
    public static BlockLargeIngredientBuffer LARGE_BUFFER = new BlockLargeIngredientBuffer().register();
    public static BlockOCPatternEditor OC_EDITOR = new BlockOCPatternEditor().register();
    public static BlockWalrus WALRUS = new BlockWalrus().register();
    public static ItemFluidDrop DROP = new ItemFluidDrop().register();
    public static ItemFluidEncodedPattern PATTERN = new ItemFluidEncodedPattern().register();
    public static ItemPartFluidTerminal FLUID_TERM = new ItemPartFluidTerminal().register();
    public static ItemPartFluidPatternTerminal FLUID_TERMINAL = new ItemPartFluidPatternTerminal().register();
    public static ItemPartFluidPatternTerminalEx FLUID_TERMINAL_EX = new ItemPartFluidPatternTerminalEx().register();
    public static ItemPartFluidInterface FLUID_INTERFACE = new ItemPartFluidInterface().register();
    public static ItemPartFluidP2PInterface FLUID_INTERFACE_P2P = new ItemPartFluidP2PInterface().register();
    public static ItemFluidImportBus FLUID_IMPORT_BUS = new ItemFluidImportBus().register();
    public static ItemFluidExportBus FLUID_EXPORT_BUS = new ItemFluidExportBus().register();
    public static ItemPartFluidStorageBus FLUID_STORAGE_BUS = new ItemPartFluidStorageBus().register();
    public static ItemFluidLevelEmitter FLUID_LEVEL_EMITTER = new ItemFluidLevelEmitter().register();
    public static ItemFluidStorageMonitor FLUID_STORAGE_MONITOR = new ItemFluidStorageMonitor().register();
    public static ItemFluidConversionMonitor FLUID_CONVERSION_MONITOR = new ItemFluidConversionMonitor().register();
    public static ItemPortableFluidCell PORTABLE_FLUID_CELL = new ItemPortableFluidCell().register();
    public static ItemWirelessFluidTerminal WIRELESS_FLUID_TERM = new ItemWirelessFluidTerminal().register();
    public static ItemWirelessUltraTerminal WIRELESS_ULTRA_TERM = new ItemWirelessUltraTerminal().register();
    public static ItemWirelessPatternTerminal WIRELESS_PATTERN_TERM = new ItemWirelessPatternTerminal().register();
    public static ItemWirelessInterfaceTerminal WIRELESS_INTERFACE_TERM = new ItemWirelessInterfaceTerminal()
            .register();
    public static ItemFluidPacket PACKET = new ItemFluidPacket().register();
    public static ItemBasicFluidStorageCell CELL1K = new ItemBasicFluidStorageCell(CellType.Cell1kPart, 0, 1)
            .register();
    public static ItemBasicFluidStorageCell CELL4K = new ItemBasicFluidStorageCell(CellType.Cell4kPart, 0, 4)
            .register();
    public static ItemBasicFluidStorageCell CELL16K = new ItemBasicFluidStorageCell(CellType.Cell16kPart, 0, 16)
            .register();
    public static ItemBasicFluidStorageCell CELL64K = new ItemBasicFluidStorageCell(CellType.Cell64kPart, 0, 64)
            .register();
    public static ItemBasicFluidStorageCell CELL256K = new ItemBasicFluidStorageCell(CellType.Cell256kPart, 1, 256)
            .register();
    public static ItemBasicFluidStorageCell CELL1024K = new ItemBasicFluidStorageCell(CellType.Cell1024kPart, 1, 1024)
            .register();
    public static ItemBasicFluidStorageCell CELL4096K = new ItemBasicFluidStorageCell(CellType.Cell4096kPart, 1, 4096)
            .register();
    public static ItemBasicFluidStorageCell CELL16384K = new ItemBasicFluidStorageCell(
            CellType.Cell16384kPart,
            1,
            16384).register();
    public static ItemMultiFluidStorageCell CELL1KM = new ItemMultiFluidStorageCell(CellType.Cell1kPart, 2, 1)
            .register();
    public static ItemMultiFluidStorageCell CELL4KM = new ItemMultiFluidStorageCell(CellType.Cell4kPart, 2, 4)
            .register();
    public static ItemMultiFluidStorageCell CELL16KM = new ItemMultiFluidStorageCell(CellType.Cell16kPart, 2, 16)
            .register();
    public static ItemMultiFluidStorageCell CELL64KM = new ItemMultiFluidStorageCell(CellType.Cell64kPart, 2, 64)
            .register();
    public static ItemMultiFluidStorageCell CELL256KM = new ItemMultiFluidStorageCell(CellType.Cell256kPart, 3, 256)
            .register();
    public static ItemMultiFluidStorageCell CELL1024KM = new ItemMultiFluidStorageCell(CellType.Cell1024kPart, 3, 1024)
            .register();
    public static ItemMultiFluidStorageCell CELL4096KM = new ItemMultiFluidStorageCell(CellType.Cell4096kPart, 3, 4096)
            .register();
    public static ItemMultiFluidStorageCell CELL16384KM = new ItemMultiFluidStorageCell(
            CellType.Cell16384kPart,
            3,
            16384).register();
    public static ItemFluidExtremeStorageCell QUANTUM_CELL = new ItemFluidExtremeStorageCell(
            NameConst.ITEM_QUANTUM_FLUID_STORAGE,
            Integer.MAX_VALUE / 16,
            8,
            1,
            4.5).register();
    public static ItemFluidExtremeStorageCell SINGULARITY_CELL = new ItemFluidExtremeStorageCell(
            NameConst.ITEM_SINGULARITY_FLUID_STORAGE,
            Long.MAX_VALUE / (singleByteAmount * 2),
            8,
            1,
            5).register();
    public static ItemCreativeFluidStorageCell CREATIVE_CELL = new ItemCreativeFluidStorageCell().register();
    public static ItemBasicFluidStoragePart CELL_PART = new ItemBasicFluidStoragePart().register();
    public static ItemFluidStorageHousing CELL_HOUSING = new ItemFluidStorageHousing().register();

    public static void loadSetting() {
        if (!Config.fluidCells) return;
        CellType.Cell1kPart.setItemInstance(CELL_PART);
        CellType.Cell4kPart.setItemInstance(CELL_PART);
        CellType.Cell16kPart.setItemInstance(CELL_PART);
        CellType.Cell64kPart.setItemInstance(CELL_PART);
        CellType.Cell256kPart.setItemInstance(CELL_PART);
        CellType.Cell1024kPart.setItemInstance(CELL_PART);
        CellType.Cell4096kPart.setItemInstance(CELL_PART);
        CellType.Cell16384kPart.setItemInstance(CELL_PART);
    }
}
