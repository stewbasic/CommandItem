package com.stewbasic.command_item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

// The container used by SlateGui.
class SlateGuiContainer extends Container {
    final CommandSlate commandSlate;
    final CommandRune commandRune;
    final EntityPlayer player;

    final InventoryCraftResult output;

    // Constructs a MyContainer. Returns null if player is not holding a CommandSlate.
    static SlateGuiContainer Make(EntityPlayer player) {
        ItemStack heldStack = player.getHeldItem();
        if (heldStack == null || !(heldStack.getItem() instanceof CommandSlate)) {
            return null;
        }
        CommandSlate commandSlate = (CommandSlate) heldStack.getItem();
        return new SlateGuiContainer(player, heldStack, commandSlate);
    }

    private SlateGuiContainer(EntityPlayer player, ItemStack heldStack,
                              CommandSlate commandSlate) {
        this.player = player;
        this.commandSlate = commandSlate;
        commandRune = commandSlate.commandRune;
        ItemStack stack = new ItemStack(commandSlate.commandRune);
        output = new InventoryCraftResult();
        output.setInventorySlotContents(0, stack);
        addSlotToContainer(new Slot(this.output, 0, 31, 97) {
            @Override
            public boolean canBeHovered() {
                return true;
            }

            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return false;
            }
        });
        if (commandSlate.hasConfigNBT(heldStack)) {
            update(heldStack);
        }
        InventoryPlayer playerInventory = player.inventory;
        for (int i = 0; i < 36; ++i) {
            addSlotToContainer(new Slot(playerInventory, i, SlateGui.tabWidth + 8
                    + (i % 9) * 18, ((i < 9) ? SlateGui.hotbarTop : SlateGui.inventoryTop)
                    + (i / 9) * 18));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        // Shift-click on a slot. Handled in GuiScreenCommandItem so do
        // nothing here.
        return null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player == this.player;
    }

    public ItemStack getOutputStack() {
        ItemStack result = output.getStackInSlot(0);
        if (result == null) {
            // Shouldn't happen, but better not to crash.
            result = new ItemStack(commandRune);
            output.setInventorySlotContents(0, result);
        }
        return result;
    }

    public void update(ItemStack stack) {
        NBTTagCompound config = new NBTTagCompound();
        if (stack.getItem() == commandRune) {
            commandRune.copyNBT(stack.getTagCompound(), config);
        } else if (stack.getItem() == commandSlate) {
            commandRune.copyNBT(commandSlate.getConfigNBT(stack), config);
        }
        getOutputStack().setTagCompound(config);
    }

    public void craft(int craft) {
        ItemStack result = getOutputStack().copy();
        if (craft == 1) {
            result.stackSize = 1;
            InventoryPlayer inventory = player.inventory;
            ItemStack held = inventory.getItemStack();
            if (held != null) {
                if (held.getItem() == result.getItem()
                        && ItemStack.areItemStackTagsEqual(held, result)
                        && held.stackSize < held.getMaxStackSize()) {
                    ++held.stackSize;
                }
                return;
            }
            inventory.setItemStack(result);
        } else {
            result.stackSize = commandRune.getItemStackLimit(result);
            // Trigger the client initialization so result can combine with existing items. This
            // is only necessary for the singleplayer server case.
            if (CommandItemMod.proxy.side() == Side.CLIENT) result.getMetadata();
            mergeItemStack(result, 1, 37, true);
        }
    }
}
