package com.glodblock.github.loader;

import static com.glodblock.github.common.storage.CellType.Cell16384kPart;
import static com.glodblock.github.loader.ItemAndBlockHolder.BUFFER;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL1024K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL1024KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL16384K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL16384KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL16K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL16KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL1K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL1KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL256K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL256KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL4096K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL4096KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL4K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL4KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL64K;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL64KM;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL_HOUSING;
import static com.glodblock.github.loader.ItemAndBlockHolder.CERTUS_QUARTZ_TANK;
import static com.glodblock.github.loader.ItemAndBlockHolder.DECODER;
import static com.glodblock.github.loader.ItemAndBlockHolder.DISCRETIZER;
import static com.glodblock.github.loader.ItemAndBlockHolder.ENCODER;
import static com.glodblock.github.loader.ItemAndBlockHolder.ENERGY_CARD;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_AUTO_FILLER;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_BUFFER;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_CONVERSION_MONITOR;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_EXPORT_BUS;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_IMPORT_BUS;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_INTERFACE;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_INTERFACE_P2P;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_LEVEL_EMITTER;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_STORAGE_BUS;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_STORAGE_MONITOR;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_TERM;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_TERMINAL;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_TERMINAL_EX;
import static com.glodblock.github.loader.ItemAndBlockHolder.INFINITY_WATER_CELL;
import static com.glodblock.github.loader.ItemAndBlockHolder.INTERFACE;
import static com.glodblock.github.loader.ItemAndBlockHolder.LARGE_BUFFER;
import static com.glodblock.github.loader.ItemAndBlockHolder.LEVEL_MAINTAINER;
import static com.glodblock.github.loader.ItemAndBlockHolder.LEVEL_TERMINAL;
import static com.glodblock.github.loader.ItemAndBlockHolder.MAGNET_CARD;
import static com.glodblock.github.loader.ItemAndBlockHolder.OC_EDITOR;
import static com.glodblock.github.loader.ItemAndBlockHolder.PATTERN;
import static com.glodblock.github.loader.ItemAndBlockHolder.PORTABLE_FLUID_CELL;
import static com.glodblock.github.loader.ItemAndBlockHolder.QUANTUM_BRIDGE_CARD;
import static com.glodblock.github.loader.ItemAndBlockHolder.QUANTUM_CELL;
import static com.glodblock.github.loader.ItemAndBlockHolder.SINGULARITY_CELL;
import static com.glodblock.github.loader.ItemAndBlockHolder.WALRUS;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_FLUID_TERM;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_INTERFACE_TERM;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_LEVEL_TERM;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_PATTERN_TERM;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_ULTRA_TERM;
import static com.glodblock.github.loader.recipe.WirelessTerminalEnergyRecipe.energyCards;
import static com.glodblock.github.loader.recipe.WirelessTerminalInfinityBoosterRecipe.infCards;
import static com.glodblock.github.loader.recipe.WirelessTerminalMagnetRecipe.magnetCards;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.fish;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.loader.recipe.WirelessTerminalEnergyRecipe;
import com.glodblock.github.loader.recipe.WirelessTerminalInfinityBoosterRecipe;
import com.glodblock.github.loader.recipe.WirelessTerminalMagnetRecipe;
import com.glodblock.github.util.ModAndClassUtil;

import appeng.api.AEApi;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.registries.ItemEnum;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();

    public static final ItemStack AE2_INTERFACE = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockInterface", 1);
    public static final ItemStack AE2_PROCESS_ENG = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            24);
    public static final ItemStack AE2_STORAGE_BUS = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            220);
    public static final ItemStack AE2_CONDENSER = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockCondenser", 1);
    public static final ItemStack AE2_GLASS_CABLE = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            16);
    public static final ItemStack AE2_PROCESS_CAL = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            23);
    public static final ItemStack AE2_WORK_BENCH = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockCellWorkbench", 1);
    public static final ItemStack AE2_PATTERN_TERM = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            340);
    public static final ItemStack AE2_PROCESS_LOG = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            22);
    public static final ItemStack AE2_PURE_CERTUS = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            10);
    public static final ItemStack AE2_QUARTZ_GLASS = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockQuartzGlass", 1);
    public static final ItemStack AE2_LAMP_GLASS = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockQuartzLamp", 1);
    public static final ItemStack AE2_CELL_1K = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            35);
    public static final ItemStack AE2_CORE_ANN = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            44);
    public static final ItemStack AE2_CORE_FOM = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            43);
    public static final ItemStack PISTON = new ItemStack(Blocks.piston, 1);
    public static final ItemStack STICKY_PISTON = new ItemStack(Blocks.sticky_piston, 1);
    public static final ItemStack AE2_BLANK_PATTERN = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            52);
    public static final ItemStack AE2_TERMINAL = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            380);
    public static final ItemStack AE2_INTERFACE_TERMINAL = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            480);
    public static final ItemStack AE2_CRAFTING_CP_UNIT = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "tile.BlockCraftingUnit"),
            1,
            1);
    public static final ItemStack AE2_PATTERN_CAPACITY_CARD = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            54);
    public static final ItemStack AE2_WIRELESS_TERMINAL = GameRegistry
            .findItemStack("appliedenergistics2", "item.ToolWirelessTerminal", 1);
    public static final ItemStack AE2_P2P_ME = AEApi.instance().definitions().parts().p2PTunnelME().maybeStack(1).get();
    public static final ItemStack AE2_ME_CHEST = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockChest", 1);
    public static final ItemStack AE2_ENERGY_CELL = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockEnergyCell", 1);
    public static final ItemStack AE2_MATTER_CONDENSER = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockCondenser", 1);
    public static final ItemStack AE2_QUANTUM_RING = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockQuantumRing", 1);
    public static final ItemStack AE2_DENSE_ENERGY_CELL = GameRegistry
            .findItemStack("appliedenergistics2", "tile.BlockDenseEnergyCell", 1);
    public static final ItemStack THE_WIRELESS_TERM = GameRegistry
            .findItemStack("thaumicenergistics", "wireless.essentia.terminal", 1);
    public static final ItemStack AE2_ANNIHILATION_PLANE = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
            1,
            301);
    public static final ItemStack AE2_ADVANCED_CARD = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            28);
    public static final ItemStack AE2_FLUIX_PEARL = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            9);
    public static final ItemStack AE2_WIRELESS_RECEIVER = new ItemStack(
            GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
            1,
            41);
    private static final ItemStack WCT_WIRELESS_TERM = GameRegistry
            .findItemStack("ae2wct", "wirelessCraftingTerminal", 1);
    public static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    public static final ItemStack IRON_BAR = new ItemStack(Blocks.iron_bars, 1);
    public static final ItemStack REDSTONE_BLOCK = new ItemStack(redstone_block, 1);
    public static final ItemStack LAPIS_BLOCK = new ItemStack(lapis_block, 1);
    public static final ItemStack IRON_BLOCK = new ItemStack(iron_block, 1);

    @Override
    public void run() {
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        PORTABLE_FLUID_CELL.stack(),
                        "ABC",
                        'A',
                        AE2_ME_CHEST,
                        'B',
                        CellType.Cell1kPart.stack(1),
                        'C',
                        AE2_ENERGY_CELL));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        INTERFACE.stack(),
                        "IPI",
                        "GEG",
                        "IPI",
                        'I',
                        "ingotIron",
                        'P',
                        "dyeBlue",
                        'G',
                        "blockGlass",
                        'E',
                        AE2_INTERFACE));
        GameRegistry.addShapelessRecipe(FLUID_INTERFACE.stack(), INTERFACE.stack());
        if (Config.fluidP2PInterface) {
            GameRegistry.addShapelessRecipe(FLUID_INTERFACE_P2P.stack(), AE2_P2P_ME, INTERFACE.stack());
            GameRegistry.addShapelessRecipe(FLUID_INTERFACE_P2P.stack(), AE2_P2P_ME, FLUID_INTERFACE.stack());
        }
        GameRegistry.addShapelessRecipe(INTERFACE.stack(), FLUID_INTERFACE.stack());
        GameRegistry.addShapelessRecipe(WIRELESS_FLUID_TERM.stack(), AE2_WIRELESS_TERMINAL, BUCKET);
        GameRegistry.addShapelessRecipe(WIRELESS_PATTERN_TERM.stack(), WIRELESS_FLUID_TERM, FLUID_TERMINAL);
        GameRegistry.addShapelessRecipe(WIRELESS_INTERFACE_TERM.stack(), AE2_WIRELESS_TERMINAL, AE2_INTERFACE_TERMINAL);
        GameRegistry.addShapelessRecipe(WIRELESS_LEVEL_TERM.stack(), AE2_WIRELESS_TERMINAL, LEVEL_MAINTAINER);
        GameRegistry
                .addRecipe(new ShapedOreRecipe(CERTUS_QUARTZ_TANK.stack(), "GGG", "G G", "GGG", 'G', AE2_QUARTZ_GLASS));
        GameRegistry.addShapelessRecipe(FLUID_AUTO_FILLER.stack(), FLUID_TERMINAL, ENCODER);
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        DISCRETIZER.stack(),
                        "IPI",
                        "TMT",
                        "IPI",
                        'I',
                        "ingotIron",
                        'P',
                        AE2_PROCESS_ENG,
                        'T',
                        AE2_STORAGE_BUS,
                        'M',
                        AE2_CONDENSER));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        DECODER.stack(),
                        "IHI",
                        "CFC",
                        "IPI",
                        'I',
                        "ingotIron",
                        'H',
                        Blocks.hopper,
                        'C',
                        AE2_GLASS_CABLE,
                        'F',
                        INTERFACE,
                        'P',
                        AE2_PROCESS_CAL));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ENCODER.stack(),
                        "LPL",
                        "IWI",
                        "III",
                        'I',
                        "ingotIron",
                        'L',
                        "blockLapis",
                        'P',
                        AE2_PROCESS_ENG,
                        'W',
                        AE2_WORK_BENCH));
        GameRegistry.addShapelessRecipe(FLUID_STORAGE_BUS.stack(), FLUID_INTERFACE, STICKY_PISTON, PISTON);
        GameRegistry.addShapelessRecipe(FLUID_STORAGE_BUS.stack(), INTERFACE, STICKY_PISTON, PISTON);
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        FLUID_STORAGE_MONITOR.stack(),
                        "EIP",
                        "P  ",
                        'I',
                        "itemIlluminatedPanel",
                        'E',
                        FLUID_LEVEL_EMITTER,
                        'P',
                        "dyeBlue"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        FLUID_CONVERSION_MONITOR.stack(),
                        "ASF",
                        'A',
                        AE2_CORE_ANN,
                        'S',
                        FLUID_STORAGE_MONITOR,
                        'F',
                        AE2_CORE_FOM));
        GameRegistry.addShapelessRecipe(FLUID_TERMINAL.stack(), AE2_PATTERN_TERM, ENCODER);
        GameRegistry.addShapelessRecipe(
                FLUID_TERMINAL_EX.stack(),
                FLUID_TERMINAL.stack(),
                AE2_PROCESS_CAL,
                AE2_PROCESS_ENG,
                AE2_PROCESS_LOG);
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        BUFFER.stack(),
                        "ILI",
                        "AGF",
                        "IBI",
                        'I',
                        "ingotIron",
                        'G',
                        AE2_QUARTZ_GLASS,
                        'L',
                        AE2_CELL_1K,
                        'A',
                        AE2_CORE_ANN,
                        'F',
                        AE2_CORE_FOM,
                        'B',
                        BUCKET));
        GameRegistry.addRecipe(new ShapedOreRecipe(WALRUS, "FFF", "F F", "FFF", 'F', fish));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        FLUID_LEVEL_EMITTER.stack(),
                        "RPD",
                        'R',
                        redstone_torch,
                        'P',
                        AE2_PROCESS_CAL,
                        'D',
                        "dyeBlue"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        LARGE_BUFFER.stack(),
                        "BGB",
                        "GEG",
                        "BGB",
                        'B',
                        BUFFER.stack(),
                        'G',
                        AE2_QUARTZ_GLASS,
                        'E',
                        AE2_PROCESS_ENG));
        GameRegistry.addShapelessRecipe(AE2_BLANK_PATTERN, PATTERN.stack());
        GameRegistry.addShapelessRecipe(FLUID_TERM.stack(), AE2_TERMINAL, BUFFER);
        GameRegistry.addShapelessRecipe(LEVEL_TERMINAL.stack(), AE2_TERMINAL, LEVEL_MAINTAINER);
        GameRegistry.addShapelessRecipe(
                FLUID_BUFFER.stack(),
                LARGE_BUFFER.stack(),
                Config.fluidCells ? CELL1K.getComponent()
                        : ModAndClassUtil.EC2 ? ItemEnum.STORAGECOMPONENT.getDamagedStack(4) : null);
        GameRegistry
                .addShapelessRecipe(LEVEL_MAINTAINER.stack(), AE2_CRAFTING_CP_UNIT, ENCODER, AE2_PATTERN_CAPACITY_CARD);
        if (Config.fluidCells) {
            OreDictionary.registerOre("anyCertusCrystal", AE2_PURE_CERTUS);
            for (ItemStack it : OreDictionary.getOres("crystalCertusQuartz"))
                OreDictionary.registerOre("anyCertusCrystal", it);

            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell1kPart.stack(1),
                            "DCD",
                            "CEC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            "anyCertusCrystal",
                            'E',
                            AE2_PROCESS_ENG));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell4kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell1kPart.stack(1),
                            'P',
                            AE2_PROCESS_CAL,
                            'G',
                            AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell16kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell4kPart.stack(1),
                            'P',
                            AE2_PROCESS_LOG,
                            'G',
                            AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell64kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell16kPart.stack(1),
                            'P',
                            AE2_PROCESS_ENG,
                            'G',
                            AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell256kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell64kPart.stack(1),
                            'P',
                            AE2_PROCESS_CAL,
                            'G',
                            AE2_LAMP_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell1024kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell256kPart.stack(1),
                            'P',
                            AE2_PROCESS_LOG,
                            'G',
                            AE2_LAMP_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell4096kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell1024kPart.stack(1),
                            'P',
                            AE2_PROCESS_CAL,
                            'G',
                            AE2_LAMP_GLASS));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CellType.Cell16384kPart.stack(1),
                            "DPD",
                            "CGC",
                            "DCD",
                            'D',
                            "dyeRed",
                            'C',
                            CellType.Cell4096kPart.stack(1),
                            'P',
                            AE2_PROCESS_ENG,
                            'G',
                            AE2_LAMP_GLASS));

            FCBaseItemCell[] cells = new FCBaseItemCell[] { CELL1K, CELL4K, CELL16K, CELL64K, CELL256K, CELL1024K,
                    CELL4096K, CELL16384K, CELL1KM, CELL4KM, CELL16KM, CELL64KM, CELL256KM, CELL1024KM, CELL4096KM,
                    CELL16384KM };
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            QUANTUM_CELL.stack(),
                            "RCR",
                            "CEC",
                            "RCR",
                            'C',
                            Cell16384kPart.stack(1),
                            'E',
                            AE2_DENSE_ENERGY_CELL,
                            'R',
                            AE2_QUANTUM_RING));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            SINGULARITY_CELL.stack(),
                            "CCC",
                            "CMC",
                            "CCC",
                            'C',
                            QUANTUM_CELL,
                            'M',
                            AE2_MATTER_CONDENSER));
            for (FCBaseItemCell cell : cells) {
                if (cell.getHousing().getItemDamage() == 0) {
                    GameRegistry.addRecipe(
                            new ShapedOreRecipe(
                                    cell,
                                    "GDG",
                                    "DCD",
                                    "III",
                                    'D',
                                    "dyeRed",
                                    'G',
                                    AE2_QUARTZ_GLASS,
                                    'C',
                                    cell.getComponent(),
                                    'I',
                                    "ingotIron"));
                } else if (cell.getHousing().getItemDamage() == 1) {
                    GameRegistry.addRecipe(
                            new ShapedOreRecipe(
                                    cell,
                                    "GDG",
                                    "DCD",
                                    "III",
                                    'D',
                                    "dustRedstone",
                                    'G',
                                    AE2_LAMP_GLASS,
                                    'C',
                                    cell.getComponent(),
                                    'I',
                                    "gemDiamond"));
                } else if (cell.getHousing().getItemDamage() == 2) {
                    GameRegistry.addRecipe(
                            new ShapedOreRecipe(
                                    cell,
                                    "GDG",
                                    "DCD",
                                    "III",
                                    'D',
                                    "dyeBlue",
                                    'G',
                                    AE2_QUARTZ_GLASS,
                                    'C',
                                    cell.getComponent(),
                                    'I',
                                    "ingotIron"));
                } else if (cell.getHousing().getItemDamage() == 3) {
                    GameRegistry.addRecipe(
                            new ShapedOreRecipe(
                                    cell,
                                    "GDG",
                                    "DCD",
                                    "III",
                                    'D',
                                    "dustGlowstone",
                                    'G',
                                    AE2_LAMP_GLASS,
                                    'C',
                                    cell.getComponent(),
                                    'I',
                                    "gemDiamond"));
                }
                GameRegistry.addRecipe(new ShapelessOreRecipe(cell, cell.getHousing(), cell.getComponent()));
            }
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CELL_HOUSING.stack(1, 0),
                            "GDG",
                            "D D",
                            "III",
                            'D',
                            "dyeRed",
                            'G',
                            AE2_QUARTZ_GLASS,
                            'I',
                            "ingotIron"));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CELL_HOUSING.stack(1, 1),
                            "GDG",
                            "D D",
                            "III",
                            'D',
                            "dustRedstone",
                            'G',
                            AE2_LAMP_GLASS,
                            'I',
                            "gemDiamond"));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CELL_HOUSING.stack(1, 2),
                            "GDG",
                            "D D",
                            "III",
                            'D',
                            "dyeBlue",
                            'G',
                            AE2_QUARTZ_GLASS,
                            'I',
                            "ingotIron"));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            CELL_HOUSING.stack(1, 3),
                            "GDG",
                            "D D",
                            "III",
                            'D',
                            "dustGlowstone",
                            'G',
                            AE2_LAMP_GLASS,
                            'I',
                            "gemDiamond"));
        }

        if (Config.fluidIOBus) {
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            FLUID_EXPORT_BUS,
                            "ICI",
                            "BPB",
                            'B',
                            "dyeBlue",
                            'I',
                            "ingotIron",
                            'P',
                            PISTON,
                            'C',
                            AE2_CORE_FOM));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            FLUID_IMPORT_BUS,
                            "BCB",
                            "IPI",
                            'B',
                            "dyeBlue",
                            'I',
                            "ingotIron",
                            'P',
                            PISTON,
                            'C',
                            AE2_CORE_ANN));
        }

        if (ModAndClassUtil.OC) {
            ItemStack CHIP_T1 = new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24);
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            OC_EDITOR,
                            "IMI",
                            "CBC",
                            "IPI",
                            'I',
                            IRON_BAR,
                            'M',
                            CHIP_T1,
                            'C',
                            "oc:cable",
                            'B',
                            BUCKET,
                            'P',
                            AE2_BLANK_PATTERN));
        }
    }

    public static void addTestItem() {
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ENERGY_CARD.stack(),
                        "EEE",
                        "EAE",
                        "EEE",
                        'E',
                        AE2_DENSE_ENERGY_CELL,
                        'A',
                        AE2_ADVANCED_CARD));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        MAGNET_CARD.stack(),
                        "RBL",
                        "IAI",
                        "III",
                        'I',
                        IRON_BLOCK,
                        'R',
                        REDSTONE_BLOCK,
                        'B',
                        AE2_ANNIHILATION_PLANE,
                        'L',
                        LAPIS_BLOCK,
                        'A',
                        AE2_ADVANCED_CARD));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        QUANTUM_BRIDGE_CARD.stack(),
                        "   ",
                        "P P",
                        " R ",
                        'P',
                        AE2_FLUIX_PEARL,
                        'R',
                        AE2_WIRELESS_RECEIVER));
        GameRegistry.addRecipe(
                new ShapelessOreRecipe(INFINITY_WATER_CELL, CELL_HOUSING.stack(1, 1), CellType.Cell64kPart.stack(1)));
    }

    public static void runTerminalRecipe() {
        for (ItemStack c : infCards) {
            GameRegistry.addRecipe(new WirelessTerminalInfinityBoosterRecipe(WIRELESS_PATTERN_TERM.stack(), c));
            GameRegistry.addRecipe(new WirelessTerminalInfinityBoosterRecipe(WIRELESS_FLUID_TERM.stack(), c));
            GameRegistry.addRecipe(new WirelessTerminalInfinityBoosterRecipe(WIRELESS_ULTRA_TERM.stack(), c));
            GameRegistry.addRecipe(new WirelessTerminalInfinityBoosterRecipe(WIRELESS_INTERFACE_TERM.stack(), c));
            GameRegistry.addRecipe(new WirelessTerminalInfinityBoosterRecipe(WIRELESS_LEVEL_TERM.stack(), c));
        }
        for (ItemStack m : magnetCards) {
            GameRegistry.addRecipe(new WirelessTerminalMagnetRecipe(WIRELESS_ULTRA_TERM.stack(), m));
        }
        for (ItemStack e : energyCards) {
            GameRegistry.addRecipe(new WirelessTerminalEnergyRecipe(WIRELESS_ULTRA_TERM.stack(), e));
        }

        ItemStack[] term = { AE2_WIRELESS_TERMINAL, WIRELESS_FLUID_TERM.stack(), WIRELESS_PATTERN_TERM.stack(),
                WIRELESS_INTERFACE_TERM.stack(), WIRELESS_LEVEL_TERM.stack(), THE_WIRELESS_TERM, WCT_WIRELESS_TERM };
        GameRegistry.addShapelessRecipe(
                WIRELESS_ULTRA_TERM.stack(),
                Arrays.stream(term).filter(Objects::nonNull).toArray());

    }
}
