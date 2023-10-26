package com.glodblock.github.util;

import net.minecraft.util.StatCollector;

import appeng.core.AELog;

public enum FCGuiColors {

    // ARGB Colors: Name and default value
    StateNone(0x00000000),
    StateIdle(0xFF55FF55),
    StateCraft(0xFFFFFF55),
    StateExport(0xFFAA00AA),
    StateError(0xFFFF5555);

    private final String root;
    private final int color;

    FCGuiColors() {
        this.root = "ae2fc.gui.color";
        this.color = 0x000000;
    }

    FCGuiColors(final int hex) {
        this.root = "ae2fc.gui.color";
        this.color = hex;
    }

    public int getColor() {
        String hex = StatCollector.translateToLocal(this.getUnlocalized());
        int color = this.color;

        if (hex.length() <= 8) {
            try {
                color = Integer.parseUnsignedInt(hex, 16);
            } catch (final NumberFormatException e) {
                AELog.warn("Couldn't format color correctly for: " + this.root + " -> " + hex);
            }
        }
        return color;
    }

    public String getUnlocalized() {
        return this.root + '.' + this.toString();
    }
}
