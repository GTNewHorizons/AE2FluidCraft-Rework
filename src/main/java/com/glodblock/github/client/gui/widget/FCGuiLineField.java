package com.glodblock.github.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;

public class FCGuiLineField extends FCGuiTextField {

    private final int _xPos;
    private final int _yPos;
    private final int _width;
    private final FontRenderer _fontRender;

    public FCGuiLineField(FontRenderer fontRenderer, int xPos, int yPos, int width) {
        this(fontRenderer, xPos, yPos, width, 5);
    }

    public FCGuiLineField(FontRenderer fontRenderer, int xPos, int yPos, int width, int height) {
        super(fontRenderer, xPos, yPos, width, height);
        this._xPos = xPos;
        this._yPos = yPos;
        this._width = width;
        this._fontRender = fontRenderer;
    }

    @Override
    public void drawTextBox() {
        if (this.getVisible()) {
            drawRect(this._xPos, this._yPos, this.xPosition + this.width, this.yPosition + this.height, getColor());
        }
    }
}
