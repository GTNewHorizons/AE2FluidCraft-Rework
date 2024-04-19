package com.glodblock.github.util;

import java.util.ArrayList;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.StatCollector;

import com.glodblock.github.FluidCraft;

public class NameConst {

    public static final String BLOCK_CERTUS_QUARTZ_TANK = "certus_quartz_tank";
    public static final String BLOCK_FLUID_DISCRETIZER = "fluid_discretizer";
    public static final String BLOCK_FLUID_PATTERN_ENCODER = "fluid_pattern_encoder";
    public static final String BLOCK_FLUID_PACKET_DECODER = "fluid_packet_decoder";
    public static final String BLOCK_FLUID_INTERFACE = "fluid_interface";
    public static final String BLOCK_INGREDIENT_BUFFER = "ingredient_buffer";
    public static final String BLOCK_FLUID_BUFFER = "fluid_buffer";
    public static final String BLOCK_FLUID_AUTO_FILLER = "fluid_auto_filler";
    public static final String BLOCK_LARGE_INGREDIENT_BUFFER = "large_ingredient_buffer";
    public static final String BLOCK_OC_PATTERN_EDITOR = "oc_pattern_editor";
    public static final String BLOCK_LEVEL_MAINTAINER = "level_maintainer";
    public static final String BLOCK_WALRUS = "walrus";
    public static final String ITEM_FLUID_DROP = "fluid_drop";
    public static final String ITEM_FLUID_ENCODED_PATTERN = "fluid_encoded_pattern";
    public static final String ITEM_PART_FLUID_STORAGE_MONITOR = "part_fluid_storage_monitor";
    public static final String ITEM_PART_FLUID_CONVERSION_MONITOR = "part_fluid_conversion_monitor";
    public static final String ITEM_PART_FLUID_TERMINAL = "part_fluid_terminal";
    public static final String ITEM_PART_LEVEL_TERMINAL = "part_level_terminal";
    public static final String ITEM_PART_FLUID_PATTERN_TERMINAL = "part_fluid_pattern_terminal";
    public static final String ITEM_PART_FLUID_PATTERN_TERMINAL_EX = "part_fluid_pattern_terminal_ex";
    public static final String ITEM_PART_FLUID_INTERFACE = "part_fluid_interface";
    public static final String ITEM_PART_FLUID_P2P_INTERFACE = "part_fluid_p2p_interface";
    public static final String ITEM_PART_FLUID_IMPORT = "part_fluid_import";
    public static final String ITEM_PART_FLUID_EXPORT = "part_fluid_export";
    public static final String ITEM_PART_FLUID_STORAGE_BUS = "part_fluid_storage_bus";
    public static final String ITEM_PART_FLUID_LEVEL_EMITTER = "part_fluid_level_emitter";
    public static final String ITEM_FLUID_PACKET = "fluid_packet";
    public static final String ITEM_FLUID_STORAGE = "fluid_storage";
    public static final String ITEM_MULTI_FLUID_STORAGE = "multi_fluid_storage";
    public static final String ITEM_QUANTUM_FLUID_STORAGE = ITEM_FLUID_STORAGE + ".quantum";
    public static final String ITEM_SINGULARITY_FLUID_STORAGE = ITEM_FLUID_STORAGE + ".singularity";
    public static final String ITEM_INFINITY_FLUID_STORAGE = ITEM_FLUID_STORAGE + ".infinity";
    public static final String ITEM_INFINITY_WATER_FLUID_STORAGE = ITEM_INFINITY_FLUID_STORAGE + ".water";
    public static final String ITEM_INFINITY_LAVA_FLUID_STORAGE = ITEM_INFINITY_FLUID_STORAGE + ".lava";
    public static final String ITEM_CREATIVE_FLUID_STORAGE = "creative_" + ITEM_FLUID_STORAGE;
    public static final String ITEM_FLUID_PORTABLE_CELL = "portable_fluid_cell";
    public static final String ITEM_WIRELESS_FLUID_TERMINAL = "wireless_fluid_terminal";
    public static final String ITEM_WIRELESS_INTERFACE_TERMINAL = "wireless_interface_terminal";
    public static final String ITEM_WIRELESS_LEVEL_TERMINAL = "wireless_level_terminal";
    public static final String ITEM_WIRELESS_ULTRA_TERMINAL = "wireless_ultra_terminal";
    public static final String ITEM_WIRELESS_FLUID_PATTERN_TERMINAL = "wireless_fluid_pattern_terminal";
    public static final String ITEM_FLUID_PART = "fluid_part";
    public static final String ITEM_FLUID_STORAGE_HOUSING = "fluid_storage_housing";
    public static final String ITEM_MAGNET_CARD = "magnet_card";
    public static final String ITEM_QUANTUM_BRIDGE_CARD = "quantum_bridge_card";
    public static final String ITEM_ENERGY_CARD = "energy_card";

    public static final String TT_KEY = FluidCraft.MODID + ".tooltip.";
    public static final String TT_CRAFTING_COMPLETE = TT_KEY + "crafting_complete";
    public static final String TT_FLUID_TERMINAL = TT_KEY + "fluid_terminal";
    public static final String TT_FLUID_TERMINAL_AMOUNT = TT_FLUID_TERMINAL + ".amount";
    public static final String TT_LEVEL_TERMINAL = TT_KEY + "level_terminal";
    public static final String TT_INVALID_FLUID = TT_KEY + "invalid_fluid";
    public static final String TT_FLUID_PACKET = TT_KEY + "fluid_packet";
    public static final String TT_ENCODE_PATTERN = TT_KEY + "encode_pattern";
    public static final String TT_EMPTY = TT_KEY + "empty";
    public static final String TT_DUMP_TANK = TT_KEY + "dump_tank";
    public static final String TT_SHIFT_FOR_MORE = TT_KEY + "shift_for_more";
    public static final String TT_CTRL_FOR_MORE = TT_KEY + "ctrl_for_more";
    public static final String TT_WALRUS = TT_KEY + "walrus.";
    public static final String TT_WALRUS_DESC = TT_WALRUS + "desc";
    public static final String TT_FLUID_BUFFER = TT_KEY + "fluid_buffer.";
    public static final String TT_FLUID_PACKET_DECODER = TT_KEY + "fluid_packet_decoder.";
    public static final String TT_FLUID_PACKET_DECODER_DESC = TT_FLUID_PACKET_DECODER + "desc";
    public static final String TT_FLUID_BUFFER_DESC = TT_FLUID_BUFFER + "desc";
    public static final String TT_FLUID_DISCRETIZER = TT_KEY + "discretizer.";
    public static final String TT_FLUID_DISCRETIZER_DESC = TT_FLUID_DISCRETIZER + "desc";
    public static final String TT_LEVEL_MAINTAINER = TT_KEY + "level_maintainer.";
    public static final String TT_LEVEL_MAINTAINER_DESC = TT_LEVEL_MAINTAINER + "desc";
    public static final String TT_LEVEL_MAINTAINER_WHO_AM_I = TT_LEVEL_MAINTAINER + "Who_Am_I";
    public static final String TT_LEVEL_MAINTAINER_TITLE = TT_LEVEL_MAINTAINER + "title";
    public static final String TT_LEVEL_MAINTAINER_CURRENT = TT_LEVEL_MAINTAINER + "current";
    public static final String TT_LEVEL_MAINTAINER_NONE = TT_LEVEL_MAINTAINER + "none";
    public static final String TT_LEVEL_MAINTAINER_REQUEST_SIZE = TT_LEVEL_MAINTAINER + "request_size";
    public static final String TT_LEVEL_MAINTAINER_BATCH_SIZE = TT_LEVEL_MAINTAINER + "batch_size";
    public static final String TT_LEVEL_MAINTAINER_IDLE = TT_LEVEL_MAINTAINER + "idle";
    public static final String TT_LEVEL_MAINTAINER_IDLE_DESC = TT_LEVEL_MAINTAINER + "idle_desc";
    public static final String TT_LEVEL_MAINTAINER_LINK = TT_LEVEL_MAINTAINER + "link";
    public static final String TT_LEVEL_MAINTAINER_LINK_DESC = TT_LEVEL_MAINTAINER + "link_desc";
    public static final String TT_LEVEL_MAINTAINER_EXPORT = TT_LEVEL_MAINTAINER + "export";
    public static final String TT_LEVEL_MAINTAINER_EXPORT_DESC = TT_LEVEL_MAINTAINER + "export_desc";
    public static final String TT_LEVEL_MAINTAINER_ERROR = TT_LEVEL_MAINTAINER + "error";
    public static final String TT_LEVEL_MAINTAINER_ERROR_DESC = TT_LEVEL_MAINTAINER + "error_desc";
    public static final String TT_QUANTUM_BRIDGE_CARD = TT_KEY + "quantum_bridge_card";
    public static final String TT_QUANTUM_BRIDGE_CARD_DESC = TT_QUANTUM_BRIDGE_CARD + ".desc";
    public static final String TT_MAGNET_CARD = TT_KEY + "magnet_card";
    public static final String TT_MAGNET_CARD_DESC = TT_MAGNET_CARD + ".desc";
    public static final String TT_ENERGY_CARD = TT_KEY + "energy_card";
    public static final String TT_ENERGY_CARD_DESC = TT_ENERGY_CARD + ".desc";
    public static final String TT_CELL_CONTENTS = TT_KEY + "cell_contents";
    public static final String TT_CELL_EMPTY = TT_KEY + "cell_empty";
    public static final String TT_CELL_PORTABLE = TT_KEY + "cell_portable";
    public static final String TT_WIRELESS = TT_KEY + "wireless.";
    public static final String TT_WIRELESS_INSTALLED = TT_WIRELESS + "installed";
    public static final String TT_FLUID_AUTO_FILLER = TT_KEY + "fluid_auto_filler";
    public static final String TT_ULTRA_TERMINAL = TT_KEY + "ultra_terminal";
    public static final String TT_ULTRA_TERMINAL_TIPS = TT_ULTRA_TERMINAL + ".tips";
    public static final String TT_ULTRA_TERMINAL_TIPS_DESC = TT_ULTRA_TERMINAL_TIPS + ".desc";

    public static final String TT_ULTRA_TERMINAL_RESTOCK = TT_ULTRA_TERMINAL + ".restock";
    public static final String TT_ULTRA_TERMINAL_RESTOCK_ON = TT_ULTRA_TERMINAL_RESTOCK + ".on";
    public static final String TT_ULTRA_TERMINAL_RESTOCK_OFF = TT_ULTRA_TERMINAL_RESTOCK + ".off";

    public static final String WAILA_KEY = FluidCraft.MODID + ".waila.";
    public static final String WAILA_ENABLE = WAILA_KEY + "enable";
    public static final String WAILA_DISABLE = WAILA_KEY + "disable";
    public static final String WAILA_SPEED = WAILA_KEY + "speed";
    public static final String RES_KEY = FluidCraft.MODID + ":";

    public static final String GUI_KEY = FluidCraft.MODID + ".gui.";
    public static final String GUI_FLUID_LEVEL_EMITTER = GUI_KEY + ITEM_PART_FLUID_LEVEL_EMITTER;
    public static final String GUI_FLUID_TERMINAL = GUI_KEY + ITEM_PART_FLUID_TERMINAL;
    public static final String GUI_FLUID_PATTERN_TERMINAL = GUI_KEY + ITEM_PART_FLUID_PATTERN_TERMINAL;
    public static final String GUI_FLUID_PATTERN_TERMINAL_EX = GUI_KEY + ITEM_PART_FLUID_PATTERN_TERMINAL_EX;
    public static final String GUI_LEVEL_MAINTAINER = GUI_KEY + BLOCK_LEVEL_MAINTAINER;
    public static final String GUI_LEVEL_TERMINAL = GUI_KEY + ITEM_PART_LEVEL_TERMINAL;
    public static final String GUI_FLUID_PATTERN_ENCODER = GUI_KEY + BLOCK_FLUID_PATTERN_ENCODER;
    public static final String GUI_FLUID_PACKET_DECODER = GUI_KEY + BLOCK_FLUID_PACKET_DECODER;
    public static final String GUI_FLUID_AUTO_FILLER = GUI_KEY + BLOCK_FLUID_AUTO_FILLER;
    public static final String GUI_INGREDIENT_BUFFER = GUI_KEY + BLOCK_INGREDIENT_BUFFER;
    public static final String GUI_LARGE_INGREDIENT_BUFFER = GUI_KEY + BLOCK_LARGE_INGREDIENT_BUFFER;
    public static final String GUI_FLUID_INTERFACE = GUI_KEY + BLOCK_FLUID_INTERFACE;
    public static final String GUI_OC_PATTERN_EDITOR = GUI_KEY + BLOCK_OC_PATTERN_EDITOR;
    public static final String GUI_FLUID_IMPORT = GUI_KEY + ITEM_PART_FLUID_IMPORT;
    public static final String GUI_FLUID_EXPORT = GUI_KEY + ITEM_PART_FLUID_EXPORT;
    public static final String GUI_FLUID_STORAGE_BUS = GUI_KEY + ITEM_PART_FLUID_STORAGE_BUS;
    public static final String GUI_MAGNET_CARD = GUI_KEY + ITEM_MAGNET_CARD;
    public static final String GUI_MAGNET_CARD_NBT = GUI_MAGNET_CARD + ".nbt";
    public static final String GUI_MAGNET_CARD_META = GUI_MAGNET_CARD + ".meta";
    public static final String GUI_MAGNET_CARD_ORE = GUI_MAGNET_CARD + ".ore";
    public static final String GUI_MAGNET_CARD_WhiteList = GUI_MAGNET_CARD + ".whitelist";
    public static final String GUI_MAGNET_CARD_BlackList = GUI_MAGNET_CARD + ".blacklist";

    public static String i18n(String t, String delimiter, boolean hint) {
        if (!hint) {
            return I18n.format(t);
        }
        ArrayList<String> arr = new ArrayList<>();
        arr.add(I18n.format(t));
        if (!StatCollector.translateToLocal(t + ".hint").equals(t + ".hint")) {
            arr.add(I18n.format(t + ".hint"));
        }
        return String.join(delimiter, arr);
    }

    public static String i18n(String t) {
        return i18n(t, "\n", true);
    }

    public static String i18n(String t, boolean warp) {
        if (warp) {
            return i18n(t);
        } else {
            return i18n(t, "", true);
        }
    }
}
