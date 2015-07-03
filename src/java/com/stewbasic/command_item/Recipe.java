package com.stewbasic.command_item;

import java.util.List;
import java.util.Set;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class Recipe implements IRecipe {
	private CommandSlate commandSlate;
	private Set<Item> books;
	private CommandRune commandRune;

	public Recipe(CommandSlate input, Set<Item> books, CommandRune output) {
		this.commandSlate = input;
		this.books = books;
		this.commandRune = output;
	}

	private static class StackAndLocation {
		ItemStack stack;
		int index;

		StackAndLocation(ItemStack stack, int index) {
			this.stack = stack;
			this.index = index;
		}
	}

	private static class Match {
		StackAndLocation input = null, book = null, tertiary = null;
	}

	private Match findMatch(InventoryCrafting inventory) {
		Match match = new Match();
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				Item item = stack.getItem();
				StackAndLocation location = new StackAndLocation(stack, i);
				if (item == commandSlate && match.input == null) {
					match.input = location;
				} else if (books.contains(item) && match.book == null) {
					match.book = location;
				} else if (match.tertiary == null) {
					match.tertiary = location;
				} else {
					return null;
				}
			}
		}
		if (match.input == null || match.book == null) {
			return null;
		}
		return match;
	}

	@Override
	public boolean matches(InventoryCrafting inventory, World worldIn) {
		return (findMatch(inventory) != null);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		Match match = findMatch(inventory);
		ItemStack result = new ItemStack(commandRune);
		if (match.book != null) {
			ItemStack book = match.book.stack;
			List<String> commands = BookReader.getUnformattedLines(book, 0);
			if (commands != null) {
				commandRune.setCommands(result, commands);
			}
			List<String> options = BookReader.getUnformattedLines(book, 1);
			if (options != null) {
				for (String option : options) {
					commandRune.setOption(result, option);
				}
			}
			String name = BookReader.getPage(book, 2);
			if (name != null) {
				commandRune.setName(result, name);
			}
			String lore = BookReader.getPage(book, 3);
			if (lore != null) {
				commandRune.setLore(result, lore);
			}
		}
		if (match.tertiary != null) {
			commandRune.setDisplay(result, match.tertiary.stack);
		}
		return result;
	}

	@Override
	public int getRecipeSize() {
		return 3;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(commandRune);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inventory) {
		Match match = findMatch(inventory);
		ItemStack[] remaining = new ItemStack[inventory.getSizeInventory()];
		if (match.book != null) {
			remaining[match.book.index] = match.book.stack;
		}
		if (match.tertiary != null) {
			// Even if we return a single tertiary item, it will go into the
			// player's inventory instead of the crafting table. So instead we
			// cheat by adding to the count of the stack on the table.
			++match.tertiary.stack.stackSize;
		}
		return remaining;
	}

}
