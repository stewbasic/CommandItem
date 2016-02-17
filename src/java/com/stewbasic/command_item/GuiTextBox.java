package com.stewbasic.command_item;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.MathHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Like {@link net.minecraft.client.gui.GuiTextField GuiTextField}, but with
 * multiple lines. It would be ideal to extend that class, but unfortunately it
 * makes most of its internals private. In particular we can't get newlines into
 * the {@link net.minecraft.client.gui.GuiTextField#text text field}.
 * <p/>
 * Note: Unlike GuiTextField we put the flashing cursor at selection end,
 * because that's what moves.
 */
public class GuiTextBox extends Gui {
    public interface GuiTextBoxListener {
        void onUpdate(int textBoxId, String textContents);
    }

    private final int textBoxId;
    public boolean allowFormatting = true, allowLineBreaks = true;

    private static final int foreColor = 0xffa0a0a0, backColor = 0xff000000,
            cursorColor = 0xffd0d0d0, margin = 4;
    private final FontRenderer fontRenderer;
    protected final int xPosition, yPosition, width, height, lineHeight, maxLines,
            textY;
    private String text = "";
    private int maxStringLength = 65536;
    private int cursorCounter;
    private boolean isFocused;
    // selectEndIndex is the index which moves; it need not be >
    // selectStartIndex.
    protected int lines, selectStartIndex, selectEndIndex, currentLine,
            currentLineStart;
    protected final int[] lineStart, lineEnd;
    private GuiTextBoxListener listener = null;

    private static int min(int a, int b) {
        return a < b ? a : b;
    }

    private static int max(int a, int b) {
        return a > b ? a : b;
    }

    public GuiTextBox(int textBoxId, FontRenderer fontRenderer, int xPosition,
                      int yPosition, int width, int height) {
        this.textBoxId = textBoxId;
        this.fontRenderer = fontRenderer;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        lineHeight = fontRenderer.FONT_HEIGHT + margin;
        maxLines = max(1, (height - 2 * margin) / lineHeight);
        textY = yPosition + (height - maxLines * lineHeight) / 2;
        lineStart = new int[maxLines];
        lineEnd = new int[maxLines];
        lines = 1;
        currentLine = currentLineStart = lineEnd[0] = lineStart[0] = 0;
    }

    // Non-static to be easily mockable.
    void guiDrawRect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }

    public void drawTextBox() {
        guiDrawRect(xPosition, yPosition, xPosition + width, yPosition + height,
                foreColor);
        guiDrawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition
                + height - 1, backColor);
        for (int l = 0; l < lines; ++l) {
            int i = (l == currentLine) ? currentLineStart : lineStart[l];
            int j = lineEnd[l];
            int y = textY + lineHeight * l;
            fontRenderer.drawStringWithShadow(text.substring(i, j), getX(i, i),
                    y, foreColor);
            if (isFocused && selectEndIndex >= i && selectEndIndex <= j
                    && cursorCounter / 10 % 2 == 0) {
                int cursorX = getX(i, selectEndIndex);
                guiDrawRect(cursorX, y - 1, cursorX + 1, y + lineHeight,
                        cursorColor);
            }
            int p = MathHelper.clamp_int(selectStartIndex, i, j);
            int q = MathHelper.clamp_int(selectEndIndex, i, j);
            if (p != q) {
                drawSelectionRect(getX(i, p), y, getX(i, q), y + lineHeight);
            }

        }
    }

    boolean isShiftKeyDown() {
        return GuiScreen.isShiftKeyDown();
    }

    boolean isCtrlKeyDown() {
        return GuiScreen.isCtrlKeyDown();
    }

    void setClipboardString(String s) {
        GuiScreen.setClipboardString(s);
    }

    String getClipboardString() {
        return GuiScreen.getClipboardString();
    }

    public void textboxKeyTyped(char typedChar, int keyCode) {
        if (!isFocused) {
            return;
        }
        int action = 0;
        if (keyCode == Keyboard.KEY_A && isCtrlKeyDown()) {
            action = 3;
            selectStartIndex = 0;
            setSelectEndIndex(text.length());
        } else if (keyCode == Keyboard.KEY_C && isCtrlKeyDown()) {
            action = 3;
            setClipboardString(getSelectedText());
        } else if (keyCode == Keyboard.KEY_V && isCtrlKeyDown()) {
            action = 3;
            writeText(getClipboardString());
        } else if (keyCode == Keyboard.KEY_X && isCtrlKeyDown()) {
            action = 2;
            setClipboardString(getSelectedText());
        } else if (keyCode == Keyboard.KEY_BACK) {
            action = 2;
            if (selectStartIndex == selectEndIndex) {
                moveSelectEndIndex(isCtrlKeyDown(), false);
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            action = 2;
            if (selectStartIndex == selectEndIndex) {
                moveSelectEndIndex(isCtrlKeyDown(), true);
            }
        } else if (keyCode == Keyboard.KEY_HOME) {
            action = 1;
            selectEndIndex = isCtrlKeyDown() ? 0
                    : lineStart[currentLine];
        } else if (keyCode == Keyboard.KEY_END) {
            action = 1;
            if (isCtrlKeyDown()) {
                selectEndIndex = text.length();
            } else {
                selectEndIndex = findLineBreak(selectEndIndex, true) - 1;
                if (selectEndIndex < 0) {
                    selectEndIndex = text.length();
                }
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            action = 1;
            moveSelectEndIndex(isCtrlKeyDown(), false);
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            action = 1;
            moveSelectEndIndex(isCtrlKeyDown(), true);
        } else if (keyCode == Keyboard.KEY_UP) {
            action = 1;
            moveLines(-1);
        } else if (keyCode == Keyboard.KEY_DOWN) {
            action = 1;
            moveLines(1);
        } else if (keyCode == Keyboard.KEY_PRIOR) {
            action = 1;
            moveLines(-maxLines);
        } else if (keyCode == Keyboard.KEY_NEXT) {
            action = 1;
            moveLines(maxLines);
        }
        if (action == 1) { // Move
            cursorCounter = 0;
            if (isShiftKeyDown()) {
                setSelectEndIndex(selectEndIndex);
            } else {
                setCursorIndex(selectEndIndex);
            }
        } else if (action == 2) { // Delete
            writeText("");
        } else if (action == 0) { // Type
            if (keyCode == Keyboard.KEY_RETURN) {
                // typedChar is input as '\r'.
                typedChar = '\n';
            }
            if (isAllowedCharacter(typedChar)) {
                writeText(Character.toString(typedChar));
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean focused = mouseX >= xPosition && mouseX < xPosition + width
                && mouseY >= yPosition && mouseY < yPosition + height;
        setFocused(focused);
        if (focused && mouseButton == 0) {
            int index, l = max(0, (mouseY - textY) / lineHeight);
            if (l < lines) {
                if (l < currentLine) {
                    currentLineStart = lineStart[l];
                }
                index = pixelsFromIndex(mouseX - xPosition - margin,
                        (l == currentLine) ? currentLineStart : lineStart[l],
                        lineEnd[l]);
            } else {
                index = text.length();
            }
            setCursorIndex(index);
        }
    }

    public void setListener(GuiTextBoxListener listener) {
        this.listener = listener;
    }

    public void updateCursorCounter() {
        ++cursorCounter;
    }

    /**
     * Sets the cursor to the given index, reducing the length of the text
     * selection to zero.
     *
     * @param index
     */
    public void setCursorIndex(int index) {
        selectStartIndex = MathHelper.clamp_int(index, 0, text.length());
        setSelectEndIndex(selectStartIndex);
    }

    /**
     * Sets the end index of the selection; this is the index which moves when
     * pressing shift+movement.
     *
     * @param index
     */
    public void setSelectEndIndex(int index) {
        selectEndIndex = MathHelper.clamp_int(index, 0, text.length());
        updateDisplayRegion();
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the textbox
     */
    public void setText(String newText) {
        if (newText.length() > maxStringLength) {
            text = newText.substring(0, maxStringLength);
        } else {
            text = newText;
        }
        text = filterAllowedCharacters(text);
        setCursorIndex(text.length());
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText() {
        return text.substring(min(selectStartIndex, selectEndIndex),
                max(selectStartIndex, selectEndIndex));
    }

    /**
     * replaces selected text, or inserts text at the position on the cursor
     */
    public void writeText(String newText) {
        newText = filterAllowedCharacters(newText);
        int i = min(selectStartIndex, selectEndIndex);
        int j = max(selectStartIndex, selectEndIndex);
        int k = min(newText.length(), maxStringLength - text.length() - (i - j));
        text = text.substring(0, i) + newText.substring(0, k)
                + text.substring(j);
        setCursorIndex(i + k);
        if (listener != null) {
            listener.onUpdate(textBoxId, text);
        }
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean focused) {
        if (focused && !isFocused) {
            cursorCounter = 0;
        }
        isFocused = focused;
    }

    void drawSelectionRect(int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            int x = x1;
            x1 = x2;
            x2 = x;
        }

        if (y1 < y2) {
            int y = y1;
            y1 = y2;
            y2 = y;
        }

        if (x2 > xPosition + width) {
            x2 = xPosition + width;
        }

        if (x1 > xPosition + width) {
            x1 = xPosition + width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GL11.GL_OR_REVERSE);
        worldrenderer.startDrawingQuads();
        worldrenderer.addVertex((double) x1, (double) y2, 0.0D);
        worldrenderer.addVertex((double) x2, (double) y2, 0.0D);
        worldrenderer.addVertex((double) x2, (double) y1, 0.0D);
        worldrenderer.addVertex((double) x1, (double) y1, 0.0D);
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    // TODO: Avoid some work if the text is unchanged?
    private void updateDisplayRegion() {
        // Ensure displayEndIndex is displayed, and try to keep index
        // lineStart[0] on the first line.
        // First determine the new value for currentLine and lineStart[0].
        int lineBreak, textWidth = width - 2 * margin;
        if (selectEndIndex < lineStart[0]) {
            currentLine = 0;
            lineBreak = findLineBreak(selectEndIndex, false);
        } else {
            lineBreak = selectEndIndex + 1;
            currentLine = -1;
            do {
                ++currentLine;
                lineBreak = findLineBreak(lineBreak - 1, false);
                if (lineBreak <= lineStart[0]) {
                    break;
                }
            } while (lineBreak > 0 && currentLine < maxLines - 1);
        }
        for (lines = 0; lines < maxLines; ++lines) {
            lineStart[lines] = lineBreak;
            int nextLineBreak = findLineBreak(lineBreak, true);
            int endIndex = (nextLineBreak == -1) ? text.length() : max(
                    lineBreak, nextLineBreak - 1);
            if (lines == currentLine) {
                currentLineStart = MathHelper
                        .clamp_int(
                                currentLineStart,
                                pixelsFromIndex(-textWidth, selectEndIndex,
                                        lineBreak),
                                min(selectEndIndex,
                                        pixelsFromIndex(-textWidth, endIndex,
                                                lineBreak)));
                lineBreak = currentLineStart;
            }
            lineEnd[lines] = pixelsFromIndex(textWidth, lineBreak, endIndex);
            lineBreak = nextLineBreak;
            if (lineBreak == -1) {
                ++lines;
                break;
            }
        }
    }

    private int getX(int lineStart, int i) {
        return xPosition
                + margin
                + (lineStart < i ? fontRenderer.getStringWidth(text.substring(
                lineStart, i)) : 0);
    }

    /**
     * Find the index of the start of a line
     *
     * @param i       Index to search from.
     * @param forward Whether to search forwards.
     * @return -1 if searching forwards and there are no more lines. Otherwise
     * the index of the start of a line.
     */
    private int findLineBreak(int i, boolean forward) {
        if (forward) {
            int j = text.indexOf("\n", i);
            return (j == -1) ? j : (j + 1);
        } else {
            return text.lastIndexOf("\n", i - 1) + 1;
        }
    }

    /**
     * Changes selectEndIndex.
     *
     * @param word    Whether to move by a word.
     * @param forward Whether to move forward.
     */
    private void moveSelectEndIndex(boolean word, boolean forward) {
        if (word) {
            if (forward) {
                int j = text.indexOf(" ", selectEndIndex), k = text.indexOf(
                        "\n", selectEndIndex);
                j = (j == -1) ? k : min(j, k);
                selectEndIndex = (j == -1) ? text.length() : (j + 1);
            } else {
                selectEndIndex = max(text.lastIndexOf(" ", selectEndIndex - 2),
                        text.lastIndexOf("\n", selectEndIndex - 2)) + 1;
            }
        } else {
            selectEndIndex += forward ? 1 : -1;
        }
        selectEndIndex = min(text.length(), max(0, selectEndIndex));
    }

    /**
     * Changes selectEndIndex vertically.
     *
     * @param lines Number of lines to move; negative to move up.
     */
    private void moveLines(int lines) {
        int pixels = fontRenderer.getStringWidth(text.substring(
                currentLineStart, selectEndIndex));
        if (lines < 0) {
            selectEndIndex = findLineBreak(selectEndIndex, false);
            for (; lines < 0; ++lines) {
                if (selectEndIndex == 0) {
                    break;
                }
                selectEndIndex = findLineBreak(selectEndIndex - 1, false);
            }
            // Hack to ensure we display the start of the line.
            currentLineStart = selectEndIndex;
        }
        for (; lines > 0; --lines) {
            selectEndIndex = findLineBreak(selectEndIndex, true);
            if (selectEndIndex == -1) {
                selectEndIndex = text.length();
                return;
            }
        }
        int nextLine = findLineBreak(selectEndIndex, true) - 1;
        if (nextLine < 0) {
            nextLine = text.length();
        }
        selectEndIndex = pixelsFromIndex(pixels, selectEndIndex, nextLine);
    }

    /**
     * Finds the character index a given pixel distance from the starting index.
     *
     * @param index  Index of start character.
     * @param pixels Number of pixels to move. Set negative to move left.
     * @param cap    Force return value to be between index and cap.
     * @return Index of end character.
     */
    private int pixelsFromIndex(int pixels, int index, int cap) {
        index = MathHelper.clamp_int(index, 0, text.length());
        if (pixels >= 0) {
            String s = text.substring(index,
                    MathHelper.clamp_int(cap, index, text.length()));
            return index + fontRenderer.trimStringToWidth(s, pixels).length();
        } else {
            String s = text.substring(MathHelper.clamp_int(cap, 0, index),
                    index);
            return index
                    - fontRenderer.trimStringToWidth(s, -pixels, true).length();
        }
    }

    /**
     * Compared to
     * {@link net.minecraft.util.ChatAllowedCharacters#isAllowedCharacter(char)}
     * we may allow line breaks and the section symbol (167) for formatting
     * codes.
     *
     * @param character
     * @return Whether the character is allowed in the textbox
     */
    private boolean isAllowedCharacter(char character) {
        if (character == 167)
            return allowFormatting;
        if (character == 10)
            return allowLineBreaks;
        return character >= 32 && character != 127;
    }

    private String filterAllowedCharacters(String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (isAllowedCharacter(c)) {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }
}
