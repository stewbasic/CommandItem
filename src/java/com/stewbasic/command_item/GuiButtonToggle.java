package com.stewbasic.command_item;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonToggle extends GuiButton {
	private boolean state = false;
	private String textTrue, textFalse;

	public GuiButtonToggle(int buttonId, int x, int y, int width, int height,
			String textFalse, String textTrue) {
		super(buttonId, x, y, width, height, textFalse);
		this.textFalse = textFalse;
		this.textTrue = textTrue;
	}

	public void setState(boolean state) {
		this.state = state;
		displayString = state ? textTrue : textFalse;
	}

	public boolean getState() {
		return state;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		setState(!state);
	}
}
