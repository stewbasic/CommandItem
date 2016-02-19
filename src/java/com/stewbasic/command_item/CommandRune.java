package com.stewbasic.command_item;

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
import net.minecraftforge.fml.relauncher.Side;

/**
 * Note that this item is not added to any creative inventory tab, and can only
 * be obtained using /give. This is analogous to the command block.
 */
public class CommandRune extends MimicItem {
    static final String name = "command_rune";
    private static final String DISP = "display";
    private static final String LORE = "Lore";
    private static final String NAME = "Name";
    private static final String TAG = "cmd";
    private static final String CMD = "cmd";
    private static final String CMDSTRING = "cmdstring";
    private static final String KEEP = "keep";
    private static final String DURATION = "duration";
    private static final String STACKSIZE = "stacksize";

    private final static NBTField[] copyTags = new NBTField[]{
            new NBTField(DISP, NBT.TAG_STRING),
            new NBTField(LORE, NBT.TAG_STRING),
            new NBTField(NAME, NBT.TAG_STRING),
            new NBTField(CMDSTRING, NBT.TAG_STRING),
            new NBTField(KEEP, NBT.TAG_BYTE),
            new NBTField(DURATION, NBT.TAG_INT),
            new NBTField(STACKSIZE, NBT.TAG_INT)};

    private static final Pattern durationOption = Pattern
            .compile("duration[ :=]*(\\d+)");
    private static final Pattern stackSizeOption = Pattern
            .compile("stacksize[ :=]*(\\d+)");

    public CommandRune() {
        super(1);
        setMaxStackSize(64);
        setUnlocalizedName(name);
    }

    // This constructor is intended to be used in unit tests.
    CommandRune(Side side) {
        super(1, side);
        setMaxStackSize(64);
        setUnlocalizedName(name);
    }

    public void setName(ItemStack stack, String name) {
        NBTTagCompound nbt = stack.getSubCompound(TAG, true);
        nbt.setString(NAME, name);
        updateDisplay(stack, NAME, true);
    }

    public String getName(ItemStack stack) {
        return getStringTag(stack, NAME);
    }

    public void setLore(ItemStack stack, String lore) {
        NBTTagCompound nbt = stack.getSubCompound(TAG, true);
        nbt.setString(LORE, lore);
        updateDisplay(stack, LORE, true);
    }

    public String getLore(ItemStack stack) {
        return getStringTag(stack, LORE);
    }

    /**
     * Stores the list of commands as a single newline-separated string.
     *
     * @param stack
     * @param commands
     */
    public void setCommandString(ItemStack stack, String commands) {
        NBTTagCompound nbt = stack.getSubCompound(TAG, true);
        nbt.setString(CMDSTRING, commands);
        updateCommands(nbt);
    }

    public String getCommandString(ItemStack stack) {
        return getStringTag(stack, CMDSTRING);
    }

    // Splits command string into commands.
    private void updateCommands(NBTTagCompound tag) {
        String commands = tag.getString(CMDSTRING);
        if (commands == null) {
            tag.removeTag(CMD);
        } else {
            NBTTagList nbtCommands = new NBTTagList();
            for (String line : commands.split("\n")) {
                if (!line.isEmpty()) nbtCommands.appendTag(new NBTTagString(line));
            }
            tag.setTag(CMD, nbtCommands);
        }
    }

    public void setOptions(ItemStack stack, String options) {
        for (String option : options.split("\n")) {
            if (KEEP.equals(option)) {
                setKeep(stack, true);
            }
            Matcher match = durationOption.matcher(option);
            if (match.matches()) {
                setDuration(stack, Integer.parseInt(match.group(1)));
            }
            match = stackSizeOption.matcher(option);
            if (match.matches()) {
                setStackSize(stack, Integer.parseInt(match.group(1)));
            }
        }
    }

    public void setDuration(ItemStack stack, int duration) {
        stack.getSubCompound(TAG, true).setInteger(DURATION, duration);
    }

    public void setStackSize(ItemStack stack, int stackSize) {
        stack.getSubCompound(TAG, true).setInteger(STACKSIZE, stackSize);
    }

    public void setKeep(ItemStack stack, boolean keep) {
        if (keep) {
            stack.getSubCompound(TAG, true).setBoolean(KEEP, true);
        } else {
            stack.getSubCompound(TAG, true).removeTag(KEEP);
        }
    }

    public boolean getKeep(ItemStack stack) {
        return keep(stack.getSubCompound(TAG, false));

    }

    protected String parse(String text, boolean stripFormatting) {
        return BookReader.parse(text, stripFormatting);
    }

    /**
     * Parse and copy display information from TAG.key to DISP.key.
     *
     * @param key             Either NAME or LORE
     * @param stripFormatting Whether to strip formatting codes. When called on a dedicated
     *                        server this must be true.
     */
    private void updateDisplay(ItemStack stack, String key,
                               boolean stripFormatting) {
        NBTTagCompound tag = stack.getSubCompound(TAG, false);
        if (tag == null || !tag.hasKey(key, NBT.TAG_STRING)) {
            return;
        }
        String parsedText = parse(tag.getString(key), stripFormatting);
        NBTTagCompound disp = stack.getSubCompound(DISP, true);
        if (key.equals(NAME)) {
            disp.setString(key, parsedText);
        } else {
            String[] lines = parsedText.split("\n");
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
        final IChatComponent name;
        final EntityPlayer player;
        final World world;

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
        for (int i = 0; i < cmds.tagCount(); ++i) {
            String cmd = cmds.getStringTagAt(i);
            icommandmanager.executeCommand(commandSender, cmd);
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

    @Override
    public void copyNBT(NBTTagCompound from, NBTTagCompound to) {
        super.copyNBT(from, to);
        copyNBTSubtag(from, to, TAG, copyTags);
        updateCommands(to.getCompoundTag(TAG));
    }

    private String getStringTag(ItemStack stack, String key) {
        NBTTagCompound nbt = stack.getSubCompound(TAG, false);
        return (nbt != null && nbt.hasKey(key, NBT.TAG_STRING)) ? nbt
                .getString(key) : null;
    }
}
