package com.stewbasic.command_item;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OpenGuiMessage implements IMessage, Runnable {
	public static int discriminator = 0;
	private MessageContext ctx = null;

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

	@SideOnly(Side.CLIENT)
	private void openGui() {
		EntityPlayer player = CommandItemMod.proxy.getPlayerEntity(ctx);
		Minecraft.getMinecraft().displayGuiScreen(
				new GuiScreenCommandItem(player));
	}

	@Override
	public void run() {
		openGui();
	}

	public static class Handler implements
			IMessageHandler<OpenGuiMessage, IMessage> {
		@Override
		public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
			message.ctx = ctx;
			CommandItemMod.proxy.getThread(ctx).addScheduledTask(message);
			return null;
		}
	}
}
