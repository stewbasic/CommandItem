package com.stewbasic.command_item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;

/**
 * This class is very similar to
 * {@link net.minecraft.client.gui.GuiOptionSlider}. Unfortunately the latter is
 * tied to GameSettings, so we can't use it directly.
 */
public class GuiSlider extends GuiButton {
    private final static int forecolor = 0xffdddddd, backcolor = 0xff000000;

    private final int minVal, valRange, boxX, boxWidth;
    private final String label;

    private int currentVal;
    private String text;
    private boolean dragging = false;

    /**
     * @param buttonId See GuiButton.id
     * @param x        Left of containing box
     * @param y        Top of containing box
     * @param width    Width of containing box
     * @param height   Height of containing box
     * @param label    Label of quantity
     * @param minVal   Minimum value
     * @param maxVal   Maximum value
     */
    public GuiSlider(int buttonId, int x, int y, int width, int height,
                     String label, int minVal, int maxVal) {
        // The slider is a square button with size height.
        //noinspection SuspiciousNameCombination
        super(buttonId, x, y, height, height, "");
        this.minVal = minVal;
        valRange = maxVal - minVal;
        boxX = x;
        boxWidth = width;
        this.label = label;
        setVal(minVal);
    }

    private void set(int val, int x) {
        currentVal = val;
        xPosition = x;
        text = label + "=" + val;
    }

    public void setVal(int val) {
        val = MathHelper.clamp_int(val, minVal, minVal + valRange);
        set(val, boxX + (boxWidth - width) * (val - minVal) / valRange);
    }

    public int getVal() {
        return currentVal;
    }

    private void setMouseX(int x) {
        x -= height / 2 + boxX;
        x = MathHelper.clamp_int(x, 0, boxWidth - width);
        set(minVal + valRange * x / (boxWidth - width), x + boxX);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        Gui.drawRect(boxX, yPosition, boxX + boxWidth, yPosition + height,
                backcolor);
        super.drawButton(mc, mouseX, mouseY);
        drawCenteredString(mc.fontRendererObj, text, boxX + boxWidth / 2,
                yPosition + (height - 8) / 2, forecolor);
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (visible && dragging) {
            setMouseX(mouseX);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        int dx = mouseX - boxX, dy = mouseY - yPosition;
        if (dx >= 0 && dx < boxWidth && dy >= 0 && dy < height) {
            dragging = true;
            setMouseX(mouseX);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }
}
