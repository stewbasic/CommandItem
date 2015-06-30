package com.stewbasic.command_item;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A helper class for pulling the text from the NBT tag of a book.
 * 
 * The text in an Array<String> tag with key "pages". Each entry represents one
 * page and can use either of two formats: - A string in double quotes, with
 * escape sequences \\, \", \n. - A JSON object in string format, with key
 * "text" (and other formatting keys)
 * 
 * Example:
/give @p minecraft:written_book 1 0 {title:,author:,pages:["foo","{text:\"foo\",color:\"green\",extra:[{text:\"bar\",color:\"blue\"}]}"]}
 * Also try pasting into a book:
§nMinecraft Formatting

§r§00 §11 §22 §33
§44 §55 §66 §77
§88 §99 §aa §bb
§cc §dd §ee §ff

§r§0k §kMinecraft
§rl §lMinecraft
§rm §mMinecraft
§rn §nMinecraft
§ro §oMinecraft
§rr §rMinecraft
 * References:
 * http://minecraft.gamepedia.com/Player.dat_format
 * http://minecraft.gamepedia.com/Formatting_codes
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/2219621-v1-0-6-json-book-generator-easily-create-colored
 * 
 * @author stewbasic
 * 
 */
public class BookReader {
	private static final String PAGES = "pages";

	public static String getPage(ItemStack stack, int page) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return null;
		}
		NBTTagList pages = nbt.getTagList(PAGES, NBT.TAG_STRING);
		if (pages.tagCount() > page) {
			return pages.getStringTagAt(page);
		} else {
			return null;
		}
	}

	/**
	 * TODO: Fill me. Handle quoted text and JSON.
	 * 
	 * @param stack
	 *            An ItemStack containing a book item
	 * @param page
	 *            The desired page number.
	 * @return
	 */
	public static List<String> getPageLines(ItemStack stack, int page) {
		String pageText = getPage(stack, page);
		if (pageText == null) {
			return null;
		}
		try {
			pageText = IChatComponent.Serializer.jsonToComponent(pageText)
					.getFormattedText();
		} catch (JsonParseException jsonparseexception) {
		}
		ArrayList<String> lines = new ArrayList<String>();
		for (String line : pageText.split("\n")) {
			lines.add(line);
		}
		return lines;
	}
}
