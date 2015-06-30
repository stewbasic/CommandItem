package com.stewbasic.command_item;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

import java.util.HashSet;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

public class CommonProxy {
	public static CommandSlate commandSlate;
	public static CommandRune commandRune;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		commandSlate = new CommandSlate();
		commandRune = new CommandRune();
		HashSet<Item> books = new HashSet<Item>();
		books.add(Items.writable_book);
		books.add(Items.written_book);
		GameRegistry.registerItem(commandSlate, CommandSlate.name);
		GameRegistry.registerItem(commandRune, CommandRune.name);
		GameRegistry.addRecipe(new Recipe(commandSlate, books, commandRune));
		RecipeSorter.register("command_item:shapeless", Recipe.class,
				SHAPELESS, "");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
