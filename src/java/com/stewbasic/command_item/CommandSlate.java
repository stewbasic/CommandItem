package com.stewbasic.command_item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Note that this item is not added to any creative inventory tab, and can only
 * be obtained using /give. This is analogous to the command block.
 */
public class CommandSlate extends Item {
	static final String name = "command_slate";

	public CommandSlate() {
		super();
		setMaxStackSize(64);
		setUnlocalizedName(name);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world,
			EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			CommandItemMod.network.sendTo(new OpenGuiMessage(),
					(EntityPlayerMP) player);
		}
		return stack;
	}
}
