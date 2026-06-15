package com.glodblock.github.util;

import com.gtnewhorizon.gtnhlib.color.ColorResource;

public class FCGuiColors {

    private static final ColorResource.Factory color = new ColorResource.Factory("ae2fc");

    public static final ColorResource
    // spotless:off
        guiTextColorGray            = color.rgb("guiTextColorGray",             "0x404040"),

        stateNone                   = color.argb("stateNone",                   "0x00000000"),
        stateIdle                   = color.argb("stateIdle",                   "0xFF55FF55"),
        stateCraft                  = color.argb("stateCraft",                  "0xFFFFFF55"),
        stateExport                 = color.argb("stateExport",                 "0xFFAA00AA"),
        stateError                  = color.argb("stateError",                  "0xFFFF5555"),
        itemSlotOverlayUnpowered    = color.argb("itemSlotOverlayUnpowered",    "0x66111111");
    // spotless:on
}
