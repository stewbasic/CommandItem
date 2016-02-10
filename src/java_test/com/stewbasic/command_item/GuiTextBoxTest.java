package com.stewbasic.command_item;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.input.Keyboard;

public class GuiTextBoxTest extends TestCase {
    private GuiTextBox textBox;
    private FontRenderer fontRenderer;

    @Override
    protected void setUp() {
        fontRenderer = mock(FontRenderer.class);
        fontRenderer.FONT_HEIGHT = 9;
        // (40 - 2 * 4) / (9 + 4) = 2 lines.
        textBox = spy(new GuiTextBox(2, fontRenderer, 10, 20, 100, 40));
        textBox.setFocused(true);
        setCtrl(false);
        setShift(false);
        doNothing().when(textBox).setClipboardString(anyString());
        doReturn("clipboard ").when(textBox).getClipboardString();
        doNothing().when(textBox).guiDrawRect(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        doNothing().when(textBox).drawSelectionRect(anyInt(), anyInt(), anyInt(), anyInt());
        resetRenderer();
    }

    private void setCtrl(boolean ctrl) {
        doReturn(ctrl).when(textBox).isCtrlKeyDown();
    }

    private void setShift(boolean shift) {
        doReturn(shift).when(textBox).isShiftKeyDown();
    }

    private void resetRenderer() {
        reset(fontRenderer);
        Answer answer = new Answer() {

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Treat all characters as 10 pixels wide.
                Object[] args = invocationOnMock.getArguments();
                String s = (String) args[0];
                if (args.length == 1) {
                    return s.length() * 10;
                }
                return s.substring(0, Math.min(s.length(), (Integer) args[1] / 10));
            }
        };
        doAnswer(answer).when(fontRenderer).trimStringToWidth(anyString(), anyInt(),
                anyBoolean());
        doAnswer(answer).when(fontRenderer).trimStringToWidth(anyString(), anyInt());
        doAnswer(answer).when(fontRenderer).getStringWidth(anyString());
        doReturn(0).when(fontRenderer).drawStringWithShadow(anyString(), anyFloat(), anyFloat(),
                anyInt());
    }

    public void testTyping() {
        GuiTextBox.GuiTextBoxListener listener = mock(GuiTextBox.GuiTextBoxListener.class);
        textBox.setListener(listener);
        textBox.textboxKeyTyped('A', Keyboard.KEY_F);
        textBox.textboxKeyTyped('\r', Keyboard.KEY_RETURN);
        textBox.textboxKeyTyped('B', Keyboard.KEY_O);
        textBox.writeText("CDEF");
        assertEquals("A\nBCDEF", textBox.getText());

        verify(listener).onUpdate(2, "A");
        verify(listener).onUpdate(2, "A\n");
        verify(listener).onUpdate(2, "A\nB");
        verify(listener).onUpdate(2, "A\nBCDEF");
        verifyNoMoreInteractions(listener);

        textBox.textboxKeyTyped(' ', Keyboard.KEY_BACK);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_DELETE);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_HOME);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_DELETE);
        assertEquals("A\nCDE", textBox.getText());
    }

    public void testMovement() {
        assertEquals(2, textBox.maxLines);
        textBox.writeText("Line 1.\nLine 2.\n3.\nLine 4.");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_UP);
        textBox.writeText("A");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_DOWN);
        textBox.writeText("B");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_PRIOR);
        textBox.writeText("C");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_END);
        textBox.writeText("D");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_HOME);
        textBox.writeText("E");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_NEXT);
        textBox.writeText("F");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        textBox.writeText("G");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_RIGHT);
        textBox.writeText("H");
        setCtrl(true);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_HOME);
        textBox.writeText("I");
        textBox.textboxKeyTyped(' ', Keyboard.KEY_END);
        textBox.writeText("J");
        assertEquals("ILine 1.\nELineC 2.D\n3.A\nLGFHinBe 4.J", textBox.getText());
    }

    public void testSelection() {
        textBox.writeText("ABCDEF\nGH");
        setShift(true);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_UP);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_RIGHT);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_RIGHT);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_BACK);
        assertEquals("ABC", textBox.getText());
        textBox.textboxKeyTyped(' ', Keyboard.KEY_HOME);
        setCtrl(true);
        textBox.textboxKeyTyped('C', Keyboard.KEY_C);
        assertEquals("ABC", textBox.getText());
        textBox.textboxKeyTyped('X', Keyboard.KEY_X);
        assertEquals("", textBox.getText());
        verify(textBox, times(2)).setClipboardString("ABC");
        textBox.textboxKeyTyped('V', Keyboard.KEY_V);
        textBox.textboxKeyTyped('V', Keyboard.KEY_V);
        setShift(false);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        textBox.writeText("_");
        assertEquals("clipboard _clipboard ", textBox.getText());
        textBox.textboxKeyTyped('A', Keyboard.KEY_A);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_DELETE);
        assertEquals("", textBox.getText());
    }

    public void testWordMovement() {
        textBox.writeText("Foo bar baz zap\nbam");
        setCtrl(true);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_BACK);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        setShift(true);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_LEFT);
        setCtrl(false);
        textBox.writeText("A");
        assertEquals("Foo Abaz bam", textBox.getText());
    }

    public void testScrolling() {
        textBox.writeText("A\nB\nC\nD\n0123456789ABCDEF");
        textBox.drawTextBox();
        verify(fontRenderer).drawStringWithShadow(eq("D"), eq(14.f), eq(27.f), anyInt());
        verify(fontRenderer).drawStringWithShadow(eq("789ABCDEF"), eq(14.f), eq(40.f), anyInt());
        resetRenderer();

        setShift(true);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_HOME);
        textBox.drawTextBox();
        verify(fontRenderer).drawStringWithShadow(eq("D"), eq(14.f), eq(27.f), anyInt());
        verify(fontRenderer).drawStringWithShadow(eq("012345678"), eq(14.f), eq(40.f), anyInt());
        verify(textBox).drawSelectionRect(104, 40, 14, 53);
        resetRenderer();

        setShift(false);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_UP);
        textBox.textboxKeyTyped(' ', Keyboard.KEY_UP);
        textBox.drawTextBox();
        verify(fontRenderer).drawStringWithShadow(eq("C"), eq(14.f), eq(27.f), anyInt());
        verify(fontRenderer).drawStringWithShadow(eq("D"), eq(14.f), eq(40.f), anyInt());
    }
}
