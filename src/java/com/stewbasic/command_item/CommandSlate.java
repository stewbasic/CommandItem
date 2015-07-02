package com.stewbasic.command_item;

import net.minecraft.item.Item;

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
}
