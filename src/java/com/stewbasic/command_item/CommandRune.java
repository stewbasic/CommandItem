package com.stewbasic.command_item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

/**
 * Note that this item is not added to any creative inventory tab, and can only
 * be obtained using /give. This is analogous to the command block.
 * 
 * @author stewbasic
 * 
 */
public class CommandRune extends Item {
	static final String name = "command_rune";
	static final String DISP = "display";
	static final String LORE = "Lore";
	static final String NAME = "Name";
	static final String TAG = "cmd";
	static final String CMD = "cmd";

	public CommandRune() {
		super();
		setMaxStackSize(64);
		setUnlocalizedName(name);
	}

	public void setName(ItemStack stack, String name) {
		NBTTagCompound nbt = stack.getSubCompound(DISP, true);
		nbt.setString(NAME, name);
	}

	public void setDescription(ItemStack stack, List<String> description) {
		NBTTagCompound nbt = stack.getSubCompound(DISP, true);
		NBTTagList lore = new NBTTagList();
		for (String line : description) {
			lore.appendTag(new NBTTagString(line));
		}
		nbt.setTag(LORE, lore);
	}

	public void setCommands(ItemStack stack, List<String> commands) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		NBTTagList lore = new NBTTagList();
		for (String line : commands) {
			lore.appendTag(new NBTTagString(line));
		}
		nbt.setTag(CMD, lore);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn,
			EntityPlayer playerIn) {
		return itemStackIn;
	}
}
