package com.stewbasic.command_item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

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
}
