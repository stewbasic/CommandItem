package com.stewbasic.command_item;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateCommandSlateMessage implements IMessage, Runnable {
	private MessageContext ctx = null;
	private NBTTagCompound tag = null;

	public UpdateCommandSlateMessage() {
	}

	public UpdateCommandSlateMessage(NBTTagCompound tag) {
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			tag = (new PacketBuffer(buf)).readNBTTagCompoundFromBuffer();
		} catch (IOException e) {
			tag = null;
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		(new PacketBuffer(buf)).writeNBTTagCompoundToBuffer(tag);
	}

	@Override
	public void run() {
		if (tag != null) {
			if (CommandItemMod.DEBUG) {
				System.out.println(tag);
			}
			CommandSlate commandSlate = CommandItemMod.proxy.commandSlate;
			CommandRune commandRune = CommandItemMod.proxy.commandRune;
			EntityPlayer player = CommandItemMod.proxy.getPlayerEntity(ctx);
			ItemStack stack = player.getHeldItem();
			if (stack.getItem() == commandSlate) {
				commandRune.copyNBT(tag, commandSlate.getConfigNBT(stack));
			}
		}
	}

	public static class Handler implements
			IMessageHandler<UpdateCommandSlateMessage, IMessage> {
		@Override
		public IMessage onMessage(UpdateCommandSlateMessage message,
				MessageContext ctx) {
			message.ctx = ctx;
			CommandItemMod.proxy.getThread(ctx).addScheduledTask(message);
			return null;
		}
	}
}
