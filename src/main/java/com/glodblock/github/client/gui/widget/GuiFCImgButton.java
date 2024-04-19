package com.glodblock.github.client.gui.widget;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.NameConst;

import appeng.client.gui.widgets.ITooltip;

public class GuiFCImgButton extends GuiButton implements ITooltip {

    private static final Pattern COMPILE = Pattern.compile("%s");
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private static Map<EnumPair, ButtonAppearance> appearances;
    private final String buttonSetting;
    private boolean halfSize = false;
    private String fillVar;
    private String currentValue;
    private final boolean background;
    private static final String prefix = NameConst.TT_KEY;

    public GuiFCImgButton(final int x, final int y, final String idx, final String val) {
        this(x, y, idx, val, true);
    }

    public GuiFCImgButton(final int x, final int y, final String idx, final String val, boolean background) {
        super(0, 0, 16, "");
        this.background = background;
        this.buttonSetting = idx;
        this.currentValue = val;
        this.xPosition = x;
        this.yPosition = y;
        this.width = 16;
        this.height = 16;

        if (appearances == null) {
            appearances = new HashMap<>();
            this.registerApp(0, "NOT_COMBINE", "DONT_COMBINE", "not_combine");
            this.registerApp(1, "FORCE_COMBINE", "DO_COMBINE", "combine");
            this.registerApp(2, "FORCE_PRIO", "DO_PRIO", "prio");
            this.registerApp(3, "NOT_PRIO", "DONT_PRIO", "not_prio");
            this.registerApp(4, "SUBMIT", "SUBMIT", "submit");
            this.registerApp(5, "EDIT", "YES", "edit");
            this.registerApp(6, "DISABLE", "DISABLE", "disable");
            this.registerApp(7, "ENABLE", "ENABLE", "enable");
            this.registerApp(10, "FLUID_TEM", "YES", "fluid_terminal_w");
            this.registerApp(11, "CRAFT_TEM", "YES", "craft_terminal_w");
            this.registerApp(12, "PATTERN_TEM", "YES", "pattern_terminal_w");
            this.registerApp(13, "ESSENTIA_TEM", "YES", "essentia_terminal_w");
            this.registerApp(14, "INTERFACE_TEM", "YES", "interface_terminal_w");
            this.registerApp(15, "PATTERN_EX_TEM", "YES", "pattern_terminal_ex_w");
            this.registerApp(16, "FILL_PATTERN", "DO_FILL", "fill_pattern");
            this.registerApp(17, "NOT_FILL_PATTERN", "DONT_FILL", "not_fill_pattern");
            this.registerApp(20, "LEVEL_TEM", "YES", "level_terminal_w");
            this.registerApp(21, "SWITCH", "ON", "edit");
            this.registerApp(22, "SWITCH", "OFF", "view");
            this.registerApp(21, "SWITCH", "ENABLE", "enable");
            this.registerApp(22, "SWITCH", "DISABLE", "disable");
            this.registerApp(23, "CONFIG", "YES", "open_configuration");
            this.registerApp(24, "HIGHLIGHT", "YES", "block_highlight");
            this.registerApp(30, "MAGNET_CARD", "INV", "magnet_card.inv");
            this.registerApp(30, "MAGNET_CARD", "ME", "magnet_card.me");
            this.registerApp(30, "MAGNET_CARD", "FILTER", "magnet_card.filter");
            this.registerApp(31, "MAGNET_CARD", "OFF", "magnet_card.off");
            this.registerApp(32, "DATA_SYNC", "ENABLE", "data_sync.enable");
            this.registerApp(33, "ENABLE_12x", "ENABLE", "enable");
            this.registerApp(34, "DISABLE_12x", "DISABLE", "disable");
            this.registerApp(35, "DATA_SYNC", "DISABLE", "data_sync.disable");
            this.registerApp(36, "RESTOCK", "ENABLE", "restock.on");
            this.registerApp(37, "RESTOCK", "DISABLE", "restock.off");
        }
    }

    private void registerApp(final int iconIndex, final String setting, final String val, final String title) {
        final ButtonAppearance a = new ButtonAppearance();
        a.displayName = StatCollector.translateToLocal(prefix + title);
        if (StatCollector.translateToLocal(prefix + title + ".hint").equals(prefix + title + ".hint")) {
            a.displayValue = null;
        } else {
            a.displayValue = StatCollector.translateToLocal(prefix + title + ".hint");
        }
        a.index = iconIndex % 10;
        a.page = iconIndex / 10;
        appearances.put(new EnumPair(setting, val), a);
    }

    public void setVisibility(final boolean vis) {
        this.visible = vis;
        this.enabled = vis;
    }

    private int getIconIndex() {
        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance app = appearances.get(new EnumPair(this.buttonSetting, this.currentValue));
            if (app == null) {
                return 8;
            }
            return app.index;
        }
        return 8;
    }

    private int getIconPage() {
        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance app = appearances.get(new EnumPair(this.buttonSetting, this.currentValue));
            if (app == null) {
                return 0;
            }
            return app.page;
        }
        return 0;
    }

    public String getSetting() {
        return this.buttonSetting;
    }

    public String getCurrentValue() {
        return this.currentValue;
    }

    public boolean getMouseIn() {
        return this.field_146123_n;
    }

    public void set(final String e) {
        if (!this.currentValue.equals(e)) {
            this.currentValue = e;
        }
    }

    public boolean isHalfSize() {
        return this.halfSize;
    }

    public void setHalfSize(final boolean halfSize) {
        this.halfSize = halfSize;
    }

    public String getFillVar() {
        return this.fillVar;
    }

    public void setFillVar(final String fillVar) {
        this.fillVar = fillVar;
    }

    @Override
    public int xPos() {
        return this.xPosition;
    }

    @Override
    public int yPos() {
        return this.yPosition;
    }

    @Override
    public int getWidth() {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public int getHeight() {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public String getMessage() {
        String displayName = null;
        String displayValue = null;

        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance buttonAppearance = appearances
                    .get(new EnumPair(this.buttonSetting, this.currentValue));
            if (buttonAppearance == null) {
                return "No Such Message";
            }
            displayName = buttonAppearance.displayName;
            displayValue = buttonAppearance.displayValue;
        }

        if (displayName != null) {
            String name = StatCollector.translateToLocal(displayName);
            if (name == null || name.isEmpty()) {
                name = displayName;
            }
            if (displayValue != null) {
                String value = StatCollector.translateToLocal(displayValue);

                if (this.fillVar != null) {
                    value = COMPILE.matcher(value).replaceFirst(this.fillVar);
                }

                value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
                final StringBuilder sb = new StringBuilder(value);

                int i = sb.lastIndexOf("\n");
                if (i <= 0) {
                    i = 0;
                }
                while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                    sb.replace(i, i + 1, "\n");
                }
                return name + '\n' + sb;
            }
            return name;
        }
        return null;
    }

    @Override
    public void drawButton(final Minecraft par1Minecraft, final int par2, final int par3) {
        if (this.visible) {
            final int iconIndex = this.getIconIndex();
            final int iconPage = this.getIconPage();

            if (this.halfSize) {
                this.width = 8;
                this.height = 8;

                GL11.glPushMatrix();
                GL11.glTranslatef(this.xPosition, this.yPosition, 0.0F);
                GL11.glScalef(0.5f / 16 * 3, 0.5f / 16 * 3, 0.5f / 16 * 3);

                if (this.enabled) {
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
                }

                par1Minecraft.renderEngine.bindTexture(FluidCraft.resource("textures/gui/states" + iconPage + ".png"));
                this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition
                        && par2 < this.xPosition + this.width
                        && par3 < this.yPosition + this.height;

                final int uv_y = (int) Math.floor(iconIndex / 3.0);
                final int uv_x = iconIndex - uv_y * 3;
                if (this.background) {
                    this.drawTexturedModalRect(
                            0,
                            0,
                            Math.round(32F * 16F / 3F),
                            Math.round(32F * 16F / 3F),
                            Math.round(16F * 16F / 3F),
                            Math.round(16F * 16F / 3F));
                }
                this.drawTexturedModalRect(
                        0,
                        0,
                        Math.round(uv_x * 16F * 16F / 3F),
                        Math.round(uv_y * 16F * 16F / 3F),
                        Math.round(16F * 16F / 3F),
                        Math.round(16F * 16F / 3F));

            } else {
                GL11.glPushMatrix();
                GL11.glTranslatef(this.xPosition, this.yPosition, 0.0F);
                GL11.glScalef(0.5f / 16 * 6, 0.5f / 16 * 6, 0.5f / 16 * 6);
                if (this.enabled) {
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
                }

                par1Minecraft.renderEngine.bindTexture(FluidCraft.resource("textures/gui/states" + iconPage + ".png"));
                this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition
                        && par2 < this.xPosition + this.width
                        && par3 < this.yPosition + this.height;

                final int uv_y = (int) Math.floor(iconIndex / 3.0);
                final int uv_x = iconIndex - uv_y * 3;
                if (this.background) {
                    this.drawTexturedModalRect(
                            0,
                            0,
                            Math.round(32F * 16F / 3F),
                            Math.round(32F * 16F / 3F),
                            Math.round(16F * 16F / 3F),
                            Math.round(16F * 16F / 3F));
                }
                this.drawTexturedModalRect(
                        0,
                        0,
                        Math.round(uv_x * 16F * 16F / 3F),
                        Math.round(uv_y * 16F * 16F / 3F),
                        Math.round(16F * 16F / 3F),
                        Math.round(16F * 16F / 3F));
            }
            this.mouseDragged(par1Minecraft, par2, par3);
            GL11.glPopMatrix();
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static final class EnumPair {

        final String setting;
        final String value;

        EnumPair(final String a, final String b) {
            this.setting = a;
            this.value = b;
        }

        @Override
        public int hashCode() {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final EnumPair other = (EnumPair) obj;
            return other.setting.equals(this.setting) && other.value.equals(this.value);
        }
    }

    private static class ButtonAppearance {

        public int index;
        public int page;
        public String displayName;
        public String displayValue;
    }
}
