package com.stewbasic.command_item;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * WIP. See {@link net.minecraft.client.gui.GuiCommandBlock}
 * {@link net.minecraft.client.gui.GuiScreenBook}
 */
@SideOnly(Side.CLIENT)
public class GuiScreenCommandItem extends GuiContainer {
	private static int guiWidth = 176, guiHeight = 193, tabHeight = 24,
			hotbarTop = 169, inventoryTop = 93;

	private static enum Tab {
		COMMANDS, DISPLAY, OPTIONS
	}

	private static class DisplayContainer extends Container {
		public DisplayContainer(EntityPlayer player) {
			addSlotToContainer(new Slot(new InventoryCraftResult(), 0,
					guiWidth - 24, 28) {
				@Override
				public boolean canBeHovered() {
					return false;
				}
			});
			InventoryPlayer playerInventory = player.inventory;
			for (int i = 0; i < 36; ++i) {
				addSlotToContainer(new Slot(playerInventory, i,
						8 + (i % 9) * 18, ((i < 9) ? hotbarTop : inventoryTop)
								+ (i / 9) * 18));
			}
		}

		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return false;
		}
	}

	private static final ResourceLocation OPTIONS_TEXTURE = new ResourceLocation(
			CommandItemMod.MODID + ":textures/gui/options_pane.png");
	private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation(
			CommandItemMod.MODID + ":textures/gui/text_pane.png");
	private Container displayContainer, dummyContainer;
	private Slot display;
	private Tab tab;
	private GuiTextField commands, name, lore;

	public GuiScreenCommandItem(EntityPlayer player) {
		super(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer playerIn) {
				return false;
			}
		});
		dummyContainer = inventorySlots;
		displayContainer = new DisplayContainer(player);
		xSize = guiWidth;
		ySize = guiHeight;
		display = displayContainer.getSlot(0);
		setTab(Tab.COMMANDS);
	}

	private void setTab(Tab tab) {
		this.tab = tab;
		inventorySlots = (tab == Tab.OPTIONS) ? displayContainer
				: dummyContainer;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		commands = new GuiTextField(0, fontRendererObj, 8, tabHeight + 5,
				xSize - 16, ySize - tabHeight - 13);
		commands.setFocused(true);
		commands.setText("Test");
		name = new GuiTextField(0, fontRendererObj, 8, tabHeight + 5,
				xSize - 16, 20);
		lore = new GuiTextField(0, fontRendererObj, 8, tabHeight + 29,
				xSize - 16, ySize - tabHeight - 37);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks,
			int mouseX, int mouseY) {
		switch (tab) {
		case COMMANDS:
			this.mc.getTextureManager().bindTexture(TEXT_TEXTURE);
			this.drawTexturedModalRect(guiLeft, guiTop + tabHeight, 0,
					tabHeight, xSize, ySize - tabHeight);
			this.drawTexturedModalRect(guiLeft, guiTop, 0, ySize, xSize,
					tabHeight);
			break;
		case DISPLAY:
			this.mc.getTextureManager().bindTexture(TEXT_TEXTURE);
			this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
			break;
		case OPTIONS:
			this.mc.getTextureManager().bindTexture(OPTIONS_TEXTURE);
			this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
			break;
		}
	}

	private void drawCenteredString(String text, int x, int y) {
		x -= fontRendererObj.getStringWidth(text) / 2;
		fontRendererObj.drawString(text, x + 1, y + 1, 0x888888);
		fontRendererObj.drawString(text, x, y, 0xFFFFFF);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		drawCenteredString("Commands", 29, 8);
		drawCenteredString("Display", 89, 8);
		drawCenteredString("Options", 149, 8);
		switch (tab) {
		case COMMANDS:
			commands.drawTextBox();
			break;
		case DISPLAY:
			name.drawTextBox();
			lore.drawTextBox();
			break;
		case OPTIONS:
			break;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		switch (tab) {
		case COMMANDS:
			commands.mouseClicked(mouseX, mouseY, mouseButton);
			break;
		case DISPLAY:
			name.mouseClicked(mouseX, mouseY, mouseButton);
			lore.mouseClicked(mouseX, mouseY, mouseButton);
			break;
		case OPTIONS:
			break;
		}
		int dx = mouseX - guiLeft, dy = mouseY - guiTop;
		if (dy >= 0 && dy < tabHeight && dx >= 0 && dx < xSize) {
			int tabIndex = (dx * 3) / xSize;
			setTab((tabIndex == 0) ? Tab.COMMANDS
					: ((tabIndex == 1) ? Tab.DISPLAY : Tab.OPTIONS));
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int clickedButton,
			int clickType) {
		if (clickedButton == 0 && clickType == 0) {
			display.inventory.setInventorySlotContents(0, slot.getStack());
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		switch (tab) {
		case COMMANDS:
			commands.textboxKeyTyped(typedChar, keyCode);
			break;
		case DISPLAY:
			name.textboxKeyTyped(typedChar, keyCode);
			lore.textboxKeyTyped(typedChar, keyCode);
			break;
		case OPTIONS:
			break;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen() {
		switch (tab) {
		case COMMANDS:
			commands.updateCursorCounter();
			break;
		case DISPLAY:
			name.updateCursorCounter();
			lore.updateCursorCounter();
			break;
		case OPTIONS:
			break;
		}
	}

}
