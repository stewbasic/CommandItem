package com.stewbasic.command_item;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {
	ModelCopier modelCopier = new ModelCopier();

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

	@EventHandler
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		// Test. TODO: Remove
		modelCopier.copy(new ItemStack(Items.diamond), new ItemStack(
				commandRune));
	}
}
