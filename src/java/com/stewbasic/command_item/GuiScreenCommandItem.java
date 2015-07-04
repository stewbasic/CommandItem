package com.stewbasic.command_item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * WIP. See {@link net.minecraft.client.gui.GuiCommandBlock}
 * {@link net.minecraft.client.gui.GuiScreenBook}
 */
@SideOnly(Side.CLIENT)
public class GuiScreenCommandItem extends GuiScreen {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
			CommandItemMod.MODID + ":textures/gui/options_pane.png");
	private GuiTextField text;
	protected int guiWidth = 176, guiHeight = 166, left = 0, top = 0;

	public GuiScreenCommandItem(EntityPlayer player) {

	}

	@Override
	public void initGui() {
		left = (width - guiWidth) / 2;
		top = (height - guiHeight) / 2;
		text = new GuiTextField(3, this.fontRendererObj, left + 10, top + 10,
				guiHeight - 20, 20);
		text.setText("Blah");
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
		this.drawTexturedModalRect(left, top, 0, 0, guiWidth, guiHeight);
		text.drawTextBox();
	}
}
