package com.stewbasic.command_item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
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
	static final String DURATION = "duration";
	static final String STACKSIZE = "stacksize";

	private static final Pattern durationOption = Pattern
			.compile("duration[ :=]*(\\d+)");
	private static final Pattern stackSizeOption = Pattern
			.compile("stacksize[ :=]*(\\d+)");

	public CommandRune() {
		super(1);
		setMaxStackSize(64);
		setUnlocalizedName(name);
	}

	public void setName(ItemStack stack, String name) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		nbt.setString(NAME, name);
		updateDisplay(stack, NAME, true);
	}

	public void setLore(ItemStack stack, String lore) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		nbt.setString(LORE, lore);
		updateDisplay(stack, LORE, true);
	}

	public void setCommands(ItemStack stack, List<String> commands) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		NBTTagList cmds = new NBTTagList();
		for (String line : commands) {
			cmds.appendTag(new NBTTagString(line));
		}
		nbt.setTag(CMD, cmds);
	}

	public void setOption(ItemStack stack, String option) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		if (KEEP.equals(option)) {
			nbt.setBoolean(KEEP, true);
		}
		Matcher match = durationOption.matcher(option);
		if (match.matches()) {
			nbt.setInteger(DURATION, Integer.parseInt(match.group(1)));
		}
		match = stackSizeOption.matcher(option);
		if (match.matches()) {
			nbt.setInteger(STACKSIZE, Integer.parseInt(match.group(1)));
		}
	}

	/**
	 * Parse and copy display information from TAG.key to DISP.key.
	 * 
	 * @param key
	 *            Either NAME or LORE
	 * @param stripFormatting
	 *            Whether to strip formatting codes. When called on a dedicated
	 *            server this must be true.
	 */
	private void updateDisplay(ItemStack stack, String key,
			boolean stripFormatting) {
		NBTTagCompound tag = stack.getSubCompound(TAG, false);
		if (tag == null || !tag.hasKey(key, NBT.TAG_STRING)) {
			return;
		}
		String parsedText = BookReader.parse(tag.getString(key),
				stripFormatting);
		NBTTagCompound disp = stack.getSubCompound(DISP, true);
		if (key == NAME) {
			disp.setString(key, parsedText);
		} else {
			List<String> lines = BookReader.splitLines(parsedText);
			NBTTagList lore = new NBTTagList();
			for (String line : lines) {
				lore.appendTag(new NBTTagString(line));
			}
			disp.setTag(LORE, lore);
		}
	}

	@Override
	protected void onClientInit(ItemStack stack) {
		updateDisplay(stack, NAME, false);
		updateDisplay(stack, LORE, false);
	}

	private static class CommandSender implements ICommandSender {
		IChatComponent name;
		EntityPlayer player;
		World world;

		CommandSender(ItemStack stack, EntityPlayer player, World world) {
			NBTTagCompound tag = stack.getSubCompound(TAG, false);
			if (tag != null && tag.hasKey(NAME, NBT.TAG_STRING)) {
				name = BookReader.parse(tag.getString(NAME));
			} else {
				name = new ChatComponentText(CommandRune.name);
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
		CommandSender commandSender = new CommandSender(stack, player, world);
		try {
			for (int i = 0; i < cmds.tagCount(); ++i) {
				String cmd = cmds.getStringTagAt(i);
				icommandmanager.executeCommand(commandSender, cmd);
			}
		} catch (Exception e) {
			if (CommandItemMod.DEBUG) {
				System.out.println(e);
			}
		}
		if (!keep(nbt)) {
			--stack.stackSize;
		}
	}

	private static boolean keep(NBTTagCompound nbt) {
		return (nbt != null) && nbt.hasKey(KEEP);
	}

	private static int getDuration(NBTTagCompound nbt) {
		if (nbt != null && nbt.hasKey(DURATION, NBT.TAG_INT)) {
			return nbt.getInteger(DURATION);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return getDuration(stack.getSubCompound(TAG, false));
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, false);
		if (nbt != null && nbt.hasKey(STACKSIZE, NBT.TAG_INT)) {
			return nbt.getInteger(STACKSIZE);
		} else {
			return super.getItemStackLimit(stack);
		}
	}

	// Note: In creative mode changes to stack.stackSize seem to be ignored.
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world,
			EntityPlayer player) {
		NBTTagCompound nbt = stack.getSubCompound(TAG, false);
		int duration = getDuration(nbt);
		if (duration > 0) {
			player.setItemInUse(stack, duration);
		} else {
			runCommand(stack, world, player);
		}
		return stack;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world,
			EntityPlayer player) {
		runCommand(stack, world, player);
		return stack;
	}
}
