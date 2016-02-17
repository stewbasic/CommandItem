package com.stewbasic.command_item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("UnusedDeclaration")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	private void register(Item item, String name) {
		register(item, 0, name);
	}

	private void register(Item item, int meta, String name) {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem()
				.getItemModelMesher();
		mesher.register(item, meta, new ModelResourceLocation(
				CommandItemMod.MODID + ":" + name, "inventory"));

	}

	@EventHandler
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		register(commandSlate, CommandSlate.name);
		register(commandRune, CommandRune.name);
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer
				: super.getPlayerEntity(ctx));
	}
	
	@Override
	public IThreadListener getThread(MessageContext ctx) {
		return Minecraft.getMinecraft();
	}
}
