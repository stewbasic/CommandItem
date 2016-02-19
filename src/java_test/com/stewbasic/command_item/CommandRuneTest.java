package com.stewbasic.command_item;

import junit.framework.TestCase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class CommandRuneTest extends TestCase {
    private CommandRune commandRune;
    private ItemStack stack;

    @Override
    protected void setUp() {
        commandRune = spy(new CommandRune(Side.CLIENT));
        stack = new ItemStack(commandRune);
        resetMock();
    }

    private void resetMock() {
        reset(commandRune);
        doNothing().when(commandRune).copyModel(any(MimicItem.MimicKey.class),
                any(Item.class), anyInt());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return "formatted<" + args[0] + ">";
            }
        }).when(commandRune).parse(anyString(), anyBoolean());
    }

    public void testDisplay() {
        commandRune.setName(stack, "foo");
        commandRune.setLore(stack, "bar");
        assertEquals("foo", commandRune.getName(stack));
        assertEquals("bar", commandRune.getLore(stack));

        NBTTagCompound nbt = stack.getSubCompound("cmd", false);
        assertEquals("foo", nbt.getString("Name"));
        assertEquals("bar", nbt.getString("Lore"));
    }

    public void testCommand() {
        commandRune.setCommandString(stack, "say x\n\ngive @p y");
        assertEquals("say x\n\ngive @p y", commandRune.getCommandString(stack));

        NBTTagCompound nbt = stack.getSubCompound("cmd", false);
        assertEquals("say x\n\ngive @p y", nbt.getString("cmdstring"));
        NBTTagList cmds = nbt.getTagList("cmd", Constants.NBT.TAG_STRING);
        assertEquals(2, cmds.tagCount());
        assertEquals("say x", cmds.getStringTagAt(0));
        assertEquals("give @p y", cmds.getStringTagAt(1));
    }

    public void testOptions() {
        commandRune.setOptions(stack, "keep\n\nduration=10\nstacksize: 5\nblah");
        assertTrue(commandRune.getKeep(stack));
        NBTTagCompound nbt = stack.getSubCompound("cmd", false);
        assertTrue(nbt.getBoolean("keep"));
        assertEquals(10, nbt.getInteger("duration"));
        assertEquals(5, nbt.getInteger("stacksize"));

        commandRune.setKeep(stack, false);
        commandRune.setDuration(stack, 100);
        commandRune.setStackSize(stack, 32);
        assertFalse(commandRune.getKeep(stack));
        nbt = stack.getSubCompound("cmd", false);
        assertFalse(nbt.getBoolean("keep"));
        assertEquals(100, nbt.getInteger("duration"));
        assertEquals(32, nbt.getInteger("stacksize"));
    }

    public void testClientInit() {
        commandRune.setName(stack, "foo");
        commandRune.setLore(stack, "bar\nzap");
        commandRune.onClientInit(stack);

        verify(commandRune).parse("foo", false);
        verify(commandRune).parse("bar\nzap", false);

        NBTTagCompound nbt = stack.getSubCompound("display", false);
        assertEquals("formatted<foo>", nbt.getString("Name"));
        NBTTagList lore = nbt.getTagList("Lore", Constants.NBT.TAG_STRING);
        assertEquals(2, lore.tagCount());
        assertEquals("formatted<bar", lore.getStringTagAt(0));
        assertEquals("zap>", lore.getStringTagAt(1));
    }
}
