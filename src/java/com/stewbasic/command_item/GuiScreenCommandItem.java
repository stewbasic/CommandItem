package com.stewbasic.command_item;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import com.stewbasic.command_item.GuiTextBox.GuiTextBoxListener;

/**
 * A GUI for configuring a command rune. The settings are stored on the command
 * slate, which can be crafted directly into a rune. Based loosely on
 * {@link net.minecraft.client.gui.GuiCommandBlock} and
 * {@link net.minecraft.client.gui.GuiScreenBook}.
 */
@SideOnly(Side.CLIENT)
public class GuiScreenCommandItem extends GuiContainer implements
		GuiTextBoxListener {
	private static int guiWidth = 176, guiHeight = 193, tabHeight = 24,
			hotbarTop = 169, inventoryTop = 97;

	private static enum Tab {
		COMMANDS, DISPLAY, OPTIONS
	}

	private static enum Controls {
		COMMANDS, NAME, LORE, KEEP, DURATION, STACKSIZE, MIMIC;
	}

	// TODO: Allow normal interaction with inventory, just redefine shift-click
	// to take appearance.
	// TODO: Shift click preview slot to "craft" a CommandRune.
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
	private Tab tab;
	private GuiTextBox commands, name, lore;
	private GuiButtonToggle keep;
	private GuiSlider duration, stacksize;
	private int ticksToUpdate = 0, dirtyFields = 0;
	private boolean mouseDown = false;
	private final CommandRune commandRune;
	// A dummy command stack for holding the current configuration.
	private ItemStack stack = null;

	public GuiScreenCommandItem(EntityPlayer player) {
		super(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer playerIn) {
				return false;
			}
		});
		dummyContainer = inventorySlots;
		commandRune = CommandItemMod.proxy.commandRune;
		stack = new ItemStack(commandRune);
		CommandSlate commandSlate = CommandItemMod.proxy.commandSlate;
		ItemStack heldStack = player.getHeldItem();
		if (heldStack != null && heldStack.getItem() == commandSlate
				&& commandSlate.hasConfigNBT(heldStack)) {
			NBTTagCompound config = new NBTTagCompound();
			commandRune.copyNBT(commandSlate.getConfigNBT(heldStack), config);
			stack.setTagCompound(config);
		}
		displayContainer = new DisplayContainer(player);
		xSize = guiWidth;
		ySize = guiHeight;
		displayContainer.getSlot(0).inventory
				.setInventorySlotContents(0, stack);
		setTab(Tab.COMMANDS);
	}

	@SuppressWarnings("unchecked")
	private void setTab(Tab tab) {
		this.tab = tab;
		inventorySlots = (tab == Tab.OPTIONS) ? displayContainer
				: dummyContainer;
		buttonList.clear();
		if (tab == Tab.OPTIONS) {
			buttonList.add(keep);
			buttonList.add(duration);
			buttonList.add(stacksize);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		commands = new GuiTextBox(Controls.COMMANDS.ordinal(), fontRendererObj,
				8, tabHeight + 5, xSize - 16, ySize - tabHeight - 13);
		commands.setFocused(true);
		commands.allowFormatting = false;
		commands.setListener(this);
		name = new GuiTextBox(Controls.NAME.ordinal(), fontRendererObj, 8,
				tabHeight + 5, xSize - 16, 20);
		name.allowLineBreaks = false;
		name.setListener(this);
		lore = new GuiTextBox(Controls.LORE.ordinal(), fontRendererObj, 8,
				tabHeight + 29, xSize - 16, ySize - tabHeight - 37);
		lore.setListener(this);
		keep = new GuiButtonToggle(Controls.KEEP.ordinal(), guiLeft + 8,
				guiTop + 27, guiWidth - 40, 20, "Consume", "Keep");
		duration = new GuiSlider(Controls.DURATION.ordinal(), guiLeft + 8,
				guiTop + 51, guiWidth - 16, 20, "Duration", 0, 100);
		stacksize = new GuiSlider(Controls.STACKSIZE.ordinal(), guiLeft + 8,
				guiTop + 75, guiWidth - 16, 20, "Stack size", 1, 64);
		readFields();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		writeFields();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks,
			int mouseX, int mouseY) {
		switch (tab) {
		case COMMANDS:
			mc.getTextureManager().bindTexture(TEXT_TEXTURE);
			drawTexturedModalRect(guiLeft, guiTop + tabHeight, 0, tabHeight,
					xSize, ySize - tabHeight);
			drawTexturedModalRect(guiLeft, guiTop, 0, ySize, xSize, tabHeight);
			break;
		case DISPLAY:
			mc.getTextureManager().bindTexture(TEXT_TEXTURE);
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
			break;
		case OPTIONS:
			mc.getTextureManager().bindTexture(OPTIONS_TEXTURE);
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
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
		mouseDown = true;
		switch (tab) {
		case COMMANDS:
			commands.mouseClicked(mouseX - guiLeft, mouseY - guiTop,
					mouseButton);
			break;
		case DISPLAY:
			name.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton);
			lore.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton);
			break;
		case OPTIONS:
			break;
		}
		int dx = mouseX - guiLeft, dy = mouseY - guiTop;
		if (dy >= 0 && dy < tabHeight && dx >= 0 && dx < xSize) {
			int tabIndex = MathHelper.clamp_int((dx * 3) / xSize, 0, 2);
			setTab(Tab.values()[tabIndex]);
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		mouseDown = false;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int clickedButton,
			int clickType) {
		if (clickedButton == 0 && clickType == 0) {
			commandRune.setDisplay(stack, slot.getStack());
			markDirty(Controls.MIMIC.ordinal());
		}
	}

	/**
	 * We close the gui on Esc as in
	 * {@link net.minecraft.client.gui.GuiScreen#keyTyped(char, int) GuiScreen}.
	 * We don't want to call
	 * {@link net.minecraft.client.gui.inventory.GuiContainer#keyTyped(char, int)
	 * super.keyTyped} because it will close the screen when the inventory
	 * button is pressed.
	 * 
	 * @param typedChar
	 * @param keyCode
	 * @throws IOException
	 */
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
		if (keyCode == 1) {
			this.mc.displayGuiScreen((GuiScreen) null);
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
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
		if (ticksToUpdate > 1) {
			--ticksToUpdate;
		} else if (ticksToUpdate == 1 && !mouseDown) {
			ticksToUpdate = 0;
			writeFields();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		markDirty(button.id);
	}

	@Override
	public void onUpdate(int textBoxId, String textContents) {
		markDirty(textBoxId);
	}

	private void markDirty(int id) {
		// Consolidate updates by waiting for a pause in player input.
		ticksToUpdate = 20;
		dirtyFields |= (1 << id);
	}

	private void writeFields() {
		if (dirtyFields == 0) {
			return;
		}
		for (Controls control : Controls.values()) {
			if ((dirtyFields & 1) != 0) {
				switch (control) {
				case COMMANDS:
					commandRune.setCommandString(stack, commands.getText());
					break;
				case DURATION:
					commandRune.setDuration(stack, duration.getVal());
					break;
				case KEEP:
					commandRune.setKeep(stack, keep.getState());
					break;
				case LORE:
					commandRune.setLore(stack, lore.getText());
					break;
				case NAME:
					commandRune.setName(stack, name.getText());
					break;
				case STACKSIZE:
					commandRune.setStackSize(stack, stacksize.getVal());
					break;
				case MIMIC:
					// Already copied in handleMouseClick.
					break;
				}
			}
			dirtyFields /= 2;
		}
		NBTTagCompound tag = new NBTTagCompound();
		commandRune.copyNBT(stack.getTagCompound(), tag);
		if (CommandItemMod.DEBUG) {
			System.out.println(tag);
		}
		CommandItemMod.network.sendToServer(new UpdateCommandSlateMessage(tag));
	}

	private void readFields() {
		String s;
		s = commandRune.getCommandString(stack);
		commands.setText((s == null) ? "" : s);
		s = commandRune.getName(stack);
		name.setText((s == null) ? "" : s);
		s = commandRune.getLore(stack);
		lore.setText((s == null) ? "" : s);
		duration.setVal(commandRune.getMaxItemUseDuration(stack));
		stacksize.setVal(commandRune.getItemStackLimit(stack));
		keep.setState(commandRune.getKeep(stack));
	}
}
