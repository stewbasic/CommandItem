package com.stewbasic.command_item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Acts as the provided container from the server's point of view, but only
 * makes the first n slots visible and available on the client,
 */
public class PartialContainer extends Container {
	protected final Container container;
	protected final int n;

	public PartialContainer(Container container, int n) {
		this.container = container;
		this.n = Math.min(n, container.inventorySlots.size());
		for (int i = 0; i < n; ++i) {
			addSlotToContainer(container.getSlot(i));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return container.canInteractWith(player);
	}

	@Override
	public void putStackInSlot(int index, ItemStack stack) {
		container.putStackInSlot(index, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void putStacksInSlots(ItemStack[] stacks) {
		container.putStacksInSlots(stacks);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return container.transferStackInSlot(player, index);
	}
}
