package com.stewbasic.command_item;

import junit.framework.TestCase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemCoal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import static org.mockito.Mockito.*;

public class MimicItemTest extends TestCase {
    private Item coalItem, boatItem;
    private MimicItem mimicItem;
    private ItemStack coal, boat, stack;

    @Override
    protected void setUp() {
        coalItem = new ItemCoal();
        boatItem = new ItemBoat();
        coal = new ItemStack(coalItem, 1, 5);
        boat = new ItemStack(boatItem, 1, 7);
        mimicItem = spy(new MimicItem(3, Side.CLIENT));
        stack = new ItemStack(mimicItem);
        resetMock();
    }

    private void resetMock() {
        reset(mimicItem);
        doNothing().when(mimicItem).copyModel(any(MimicItem.MimicKey.class),
                any(Item.class), anyInt());
        doReturn("coal").when(mimicItem).getItemName(coalItem);
        doReturn("boat").when(mimicItem).getItemName(boatItem);
    }

    public void testSetDisplayAndMetadata() {
        mimicItem.setDisplay(stack, coal);
        NBTTagCompound nbt = stack.getSubCompound("mimicItem", false);
        assertEquals("coal", nbt.getString("id"));
        assertEquals(5, nbt.getInteger("meta"));

        ItemStack stack2 = new ItemStack(mimicItem);
        mimicItem.setDisplay(stack2, stack);
        nbt = stack2.getSubCompound("mimicItem", false);
        assertEquals("coal", nbt.getString("id"));
        assertEquals(5, nbt.getInteger("meta"));

        mimicItem.setMetadata(stack, 1);
        nbt = stack.getSubCompound("mimicItem", false);
        assertFalse(nbt.hasKey("id"));
        assertEquals(1, nbt.getInteger("meta"));

        mimicItem.setDisplay(stack2, stack);
        nbt = stack2.getSubCompound("mimicItem", false);
        assertFalse(nbt.hasKey("id"));
        assertEquals(1, nbt.getInteger("meta"));
    }

    public void testGetMetadata() {
        // Check that client init happens only once despite calling getMetadata twice.
        mimicItem.setDisplay(stack, coal);
        int metadata = mimicItem.getMetadata(stack);
        assertEquals(metadata, mimicItem.getMetadata(stack));
        verify(mimicItem).onClientInit(stack);
        verify(mimicItem).copyModel(eq(new MimicItem.MimicKey("coal", 5)), any(MimicItem.class),
                eq(metadata));

        mimicItem.setDisplay(stack, boat);
        metadata = mimicItem.getMetadata(stack);
        verify(mimicItem, times(2)).onClientInit(stack);
        verify(mimicItem).copyModel(eq(new MimicItem.MimicKey("boat", 7)), any(MimicItem.class),
                eq(metadata));

        mimicItem.setMetadata(stack, 2);
        assertEquals(2, mimicItem.getMetadata(stack));
        verify(mimicItem, times(3)).onClientInit(stack);
    }

    public void testCopyNBT() {
        mimicItem.setDisplay(stack, coal);
        NBTTagCompound nbt = new NBTTagCompound();
        mimicItem.copyNBT(stack.getTagCompound(), nbt);
        stack.getSubCompound("mimicItem", false);
        assertEquals("coal", nbt.getCompoundTag("mimicItem").getString("id"));
        assertEquals(5, nbt.getCompoundTag("mimicItem").getInteger("meta"));
    }

    public void testSinglePlayer() {
        mimicItem.setDisplay(stack, coal);
        int metadata = mimicItem.getMetadata(stack);
        verify(mimicItem).onClientInit(stack);
        verify(mimicItem).copyModel(eq(new MimicItem.MimicKey("coal", 5)), any(MimicItem.class),
                eq(metadata));
        NBTTagCompound nbt = stack.getSubCompound("mimicItem", false);
        assertTrue(nbt.hasKey("processed"));
        assertFalse(nbt.hasKey("processedClient"));

        // Disconnect from server and start single player. Models are reset.
        mimicItem.onClientDisconnect(null);
        mimicItem.onServerConnect(null);

        mimicItem.setDisplay(stack, coal);
        metadata = mimicItem.getMetadata(stack);
        verify(mimicItem, times(2)).onClientInit(stack);
        verify(mimicItem, times(2)).copyModel(eq(new MimicItem.MimicKey("coal", 5)),
                any(MimicItem.class),
                eq(metadata));
        nbt = stack.getSubCompound("mimicItem", false);
        assertFalse(nbt.hasKey("processed"));
        assertTrue(nbt.hasKey("processedClient"));
    }
}
