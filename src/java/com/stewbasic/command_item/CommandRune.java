package com.stewbasic.command_item;

import java.util.List;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.gson.JsonParseException;

/**
 * Note that this item is not added to any creative inventory tab, and can only
 * be obtained using /give. This is analogous to the command block.
 */
public class CommandRune extends MimicItem {
	static final String name = "command_rune";
	static final String DISP = "display";
	static final String LORE = "Lore";
	static final String NAME = "Name";
	static final String TAG = "cmd";
	static final String CMD = "cmd";
	static final String KEEP = "keep";

	public CommandRune() {
		super(1);
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
		NBTTagList cmds = new NBTTagList();
		for (String line : commands) {
			cmds.appendTag(new NBTTagString(line));
		}
		nbt.setTag(CMD, cmds);
	}

	private static class CommandSender implements ICommandSender {
		IChatComponent name;
		EntityPlayer player;
		World world;

		CommandSender(ItemStack stack, EntityPlayer player, World world) {
			try {
				name = IChatComponent.Serializer.jsonToComponent(stack
						.getDisplayName());
			} catch (JsonParseException e) {
				name = new ChatComponentText(stack.getDisplayName());
			}
			this.player = player;
			this.world = world;
		}

		@Override
		public String getName() {
			return name.getUnformattedText();
		}

		@Override
		public IChatComponent getDisplayName() {
			return name;
		}

		@Override
		public void addChatMessage(IChatComponent message) {
		}

		@Override
		public boolean canUseCommand(int permLevel, String commandName) {
			return permLevel <= 2;
		}

		@Override
		public BlockPos getPosition() {
			return player.getPosition();
		}

		@Override
		public Vec3 getPositionVector() {
			return player.getPositionVector();
		}

		@Override
		public World getEntityWorld() {
			return world;
		}

		@Override
		public Entity getCommandSenderEntity() {
			return player;
		}

		@Override
		public boolean sendCommandFeedback() {
			return false;
		}

		@Override
		public void setCommandStat(Type type, int amount) {
		}
	}

	/**
	 * Based on
	 * {@link net.minecraft.command.server.CommandBlockLogic#trigger(World)}.
	 */

	private void runCommand(ItemStack stack, World world, EntityPlayer player) {
		MinecraftServer minecraftserver = MinecraftServer.getServer();
		if (world.isRemote || minecraftserver == null) {
			return;
		}
		ICommandManager icommandmanager = minecraftserver.getCommandManager();
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		NBTTagList cmds = nbt.getTagList(CMD, NBT.TAG_STRING);
		try {
			for (int i = 0; i < cmds.tagCount(); ++i) {
				String cmd = cmds.getStringTagAt(i);
				icommandmanager.executeCommand(new CommandSender(stack, player,
						world), cmd);
			}
		} catch (Exception e) {
			if (CommandItemMod.DEBUG) {
				System.out.println(e);
			}
		}
	}

	private static boolean keep(NBTTagCompound nbt) {
		return (nbt != null) && nbt.hasKey(KEEP);
	}

	// Note: In creative mode changes to stack.stackSize seem to be ignored.
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world,
			EntityPlayer player) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, false);
		if (!keep(nbt)) {
			--stack.stackSize;
		}
		runCommand(stack, world, player);
		return stack;
	}

	public void setOption(ItemStack stack, String option) {
		if (KEEP.equals(option)) {
			NBTTagCompound nbt = stack.getSubCompound(TAG, true);
			nbt.setBoolean(KEEP, true);
		}
	}
}
