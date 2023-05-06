package com.glodblock.github.crossmod.extracells;

import java.util.List;

import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class EC2Replacer {

    /**
     * Should be called if and only if replaceEC2 config is true and EC2 not present. Registers proxy items for the
     * entire EC2 mod. After running this once, you should set replaceEC2 back to false.
     */
    public static void initReplacer() {
        System.out.println("Extra Cells not detected, and configuration set. Replacing extra cells.");
        ItemReplacements.postinit();
    }

    public static void replaceExtraCells(FMLMissingMappingsEvent event) {
        List<FMLMissingMappingsEvent.MissingMapping> mappings = event.getAll();
        for (FMLMissingMappingsEvent.MissingMapping m : mappings) {
            if (m.type == GameRegistry.Type.ITEM && ItemReplacements.registry.containsKey(m.name)) {
                Item toRemap = ItemReplacements.registry.get(m.name);
                m.remap(toRemap);
            } else {
                m.warn();
            }
        }
    }
}
