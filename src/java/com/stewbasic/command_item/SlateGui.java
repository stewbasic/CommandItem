package com.stewbasic.command_item;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
class SlateGui extends GuiContainer implements
        GuiTextBox.GuiTextBoxListener {
    final static int guiWidth = 242, guiHeight = 196, tabHeight = 20,
            tabWidth = 66, hotbarTop = 172, inventoryTop = 96;

    private static enum Tab {
        COMMANDS, DISPLAY, CONFIG, OUTPUT
    }

    private static enum Controls {
        COMMANDS, NAME, LORE, KEEP, DURATION, STACK_SIZE, MIMIC
    }

    private final static ResourceLocation CONFIG_TEXTURE = new ResourceLocation(
            CommandItemMod.MODID + ":textures/gui/config_pane.png");
    private final static ResourceLocation OUTPUT_TEXTURE = new ResourceLocation(
            CommandItemMod.MODID + ":textures/gui/output_pane.png");

    private final Container displayContainer;
    private final SlateGuiContainer container;
    private final ItemStack stack;
    private final CommandRune commandRune;

    private Tab tab;
    private GuiTextBox commands, name, lore;
    private GuiButtonToggle keep;
    private GuiSlider duration, stacksize;
    private int ticksToUpdate = 0, dirtyFields = 0;
    private boolean mouseDown = false;

    public SlateGui(SlateGuiContainer container) {
        super(container);
        this.container = container;
        this.commandRune = container.commandRune;
        displayContainer = new PartialContainer(container, 1);
        stack = container.getOutputStack().copy();
        xSize = guiWidth;
        ySize = guiHeight;
    }

    @SuppressWarnings("unchecked")
    private void setTab(Tab tab) {
        this.tab = tab;
        buttonList.clear();
        inventorySlots = (tab == Tab.OUTPUT) ? container : displayContainer;
        // Note: not updating mc.thePlayer.openContainer since that causes
        // the inventory to go stale.
        if (tab == Tab.CONFIG) {
            buttonList.add(keep);
            buttonList.add(duration);
            buttonList.add(stacksize);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        setTab(Tab.COMMANDS);
        Keyboard.enableRepeatEvents(true);
        commands = new GuiTextBox(Controls.COMMANDS.ordinal(),
                fontRendererObj, tabWidth + 8, 8, guiWidth - tabWidth - 16,
                guiHeight - 16);
        commands.setFocused(true);
        commands.allowFormatting = false;
        commands.setListener(this);
        name = new GuiTextBox(Controls.NAME.ordinal(), fontRendererObj,
                tabWidth + 8, 8, guiWidth - tabWidth - 16, 20);
        name.allowLineBreaks = false;
        name.setListener(this);
        lore = new GuiTextBox(Controls.LORE.ordinal(), fontRendererObj,
                tabWidth + 8, 32, guiWidth - tabWidth - 16, guiHeight - 40);
        lore.setListener(this);
        keep = new GuiButtonToggle(Controls.KEEP.ordinal(), guiLeft
                + tabWidth + 8, guiTop + 8, guiWidth - tabWidth - 16, 20,
                "Consume", "Keep");
        duration = new GuiSlider(Controls.DURATION.ordinal(), guiLeft
                + tabWidth + 8, guiTop + 32, guiWidth - tabWidth - 16, 20,
                "Duration", 0, 100);
        stacksize = new GuiSlider(Controls.STACK_SIZE.ordinal(), guiLeft
                + tabWidth + 8, guiTop + 56, guiWidth - tabWidth - 16, 20,
                "Stack size", 1, 64);
        readFields();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        writeFields(0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        if (tab == Tab.OUTPUT) {
            mc.getTextureManager().bindTexture(OUTPUT_TEXTURE);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth,
                    guiHeight);
        } else {
            int tabIndex = tab.ordinal();
            mc.getTextureManager().bindTexture(CONFIG_TEXTURE);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth,
                    guiHeight);
            drawTexturedModalRect(guiLeft, guiTop + tabHeight * tabIndex,
                    0, guiHeight + tabHeight * tabIndex, tabWidth + 8,
                    tabHeight);
        }
    }

    private void drawCenteredString(String text, int x, int y) {
        x -= fontRendererObj.getStringWidth(text) / 2;
        fontRendererObj.drawString(text, x + 1, y + 1, 0x888888);
        fontRendererObj.drawString(text, x, y, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredString("Commands", tabWidth / 2, 6);
        drawCenteredString("Name/lore", tabWidth / 2, 6 + tabHeight);
        drawCenteredString("Options", tabWidth / 2, 6 + tabHeight * 2);
        drawCenteredString("Result", tabWidth / 2, 6 + tabHeight * 3);
        if (tab == Tab.COMMANDS) {
            commands.drawTextBox();
        } else if (tab == Tab.DISPLAY) {
            name.drawTextBox();
            lore.drawTextBox();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException {
        mouseDown = true;
        int dx = mouseX - guiLeft, dy = mouseY - guiTop;
        if (dx < tabWidth) {
            int tabIndex = dy / tabHeight;
            if (dx >= 0 && dy >= 0 && tabIndex < Tab.values().length) {
                setTab(Tab.values()[tabIndex]);
                return;
            }
        }
        switch (tab) {
            case COMMANDS:
                commands.mouseClicked(mouseX - guiLeft, mouseY - guiTop,
                        mouseButton);
                break;
            case DISPLAY:
                name.mouseClicked(mouseX - guiLeft, mouseY - guiTop,
                        mouseButton);
                lore.mouseClicked(mouseX - guiLeft, mouseY - guiTop,
                        mouseButton);
                break;
            case CONFIG:
                break;
            case OUTPUT:
                break;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        mouseDown = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId,
                                    int clickedButton, int clickType) {
        if (slotId != 0)
            super.handleMouseClick(slot, slotId, clickedButton, clickType);
        if (slotId == 0 && (clickedButton == 0 || clickedButton == 1)
                && (clickType == 0 || clickType == 6)) {
            writeFields(1);
            container.craft(1);
            setTab(Tab.OUTPUT);
        }
        if (clickedButton == 0 && clickType == 1) {
            if (slotId == 0) {
                writeFields(2);
                container.craft(2);
                setTab(Tab.OUTPUT);
            } else {
                ItemStack mimicStack = slot.getStack();
                if (mimicStack != null) {
                    commandRune.setDisplay(stack, mimicStack);
                } else {
                    commandRune.setMetadata(stack, 0);
                }
                markDirty(Controls.MIMIC.ordinal());
                container.update(stack);
            }
        }
    }

    /**
     * We close the gui on Esc as in
     * {@link net.minecraft.client.gui.GuiScreen#keyTyped(char, int)
     * GuiScreen}. We don't want to call
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
        if (tab == Tab.COMMANDS) {
            commands.textboxKeyTyped(typedChar, keyCode);
        } else if (tab == Tab.DISPLAY) {
            name.textboxKeyTyped(typedChar, keyCode);
            lore.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (tab == Tab.COMMANDS) {
            commands.updateCursorCounter();
        } else if (tab == Tab.DISPLAY) {
            name.updateCursorCounter();
            lore.updateCursorCounter();
        }
        if (ticksToUpdate > 1) {
            --ticksToUpdate;
        } else if (ticksToUpdate == 1 && !mouseDown) {
            ticksToUpdate = 0;
            writeFields(0);
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
        // Consolidate updates to server by waiting for a pause in player
        // input.
        ticksToUpdate = 20;
        dirtyFields |= (1 << id);
    }

    // @formatter:off

    /**
     * The data flow for updating the fields is:
     * - (client) Wait for 20 ticks without input
     * - (client) Extract the contents of input fields
     * - (client) Send UpdateCommandSlateMessage to server
     * - (server) Modify the commandSlate and commandRune in output slot
     * - (server) Sync both itemstacks back to client
     * When choosing the mimic item, we also update the output slot directly
     * on the client to be more responsive (see handleMouseClick)
     */
    // @formatter:on
    private void writeFields(int craft) {
        if (dirtyFields == 0 && craft == 0) {
            return;
        }
        ticksToUpdate = 0;
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
                    case STACK_SIZE:
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
        CommandItemMod.proxy.sendToServer(new UpdateCommandSlateMessage(
                tag, craft));
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
