package com.stewbasic.command_item;

import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A helper class for pulling the text from the NBT tag of a book (written or
 * writable).
 */
class BookReader {
    // @formatter:off
    /* The book stores text in an Array<String> tag with key "pages". Each entry
     * represents one page. For writable_book the text is stored verbatim. For
	 * written_book the text can be either:
	 * - A "-enclosed string with escape sequences \\, \", \n
	 * - A JSON object in string format, with key "text" (and other formatting keys)
	 * Example:
/give @p minecraft:written_book 1 0 {title:,author:,pages:["seed","{text:foo,color:green,
extra:[{text:\"bar\\n\",color:blue},{text:zap,color:red}]}"]}
	 * Also try pasting into a writeable_book:
�nMinecraft Formatting

�r�00 �11 �22 �33
�44 �55 �66 �77
�88 �99 �aa �bb
�cc �dd �ee �ff

�r�0k �kMinecraft
�rl �lMinecraft
�rm �mMinecraft
�rn �nMinecraft
�ro �oMinecraft
�rr �rMinecraft
	 * References:
	 * http://minecraft.gamepedia.com/Player.dat_format
	 * http://minecraft.gamepedia.com/Formatting_codes
	 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/2219621-v1-0-6
	 * -json-book-generator-easily-create-colored
	 */
    // @formatter:on
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
     * Parses a string, which may be plain text, quoted or JSON, and produces a
     * ChatComponent. Based on
     * {@link net.minecraft.client.gui.GuiScreenBook#drawScreen(int, int, float)}.
     *
     * @param text The text to be parsed
     * @return A ChatComponent containing the text
     */
    public static IChatComponent parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            // The json parser also handles quoted text.
            IChatComponent component = IChatComponent.Serializer
                    .jsonToComponent(text);
            if (component != null) {
                return component;
            }
        } catch (JsonParseException e) {
            // Text may be plain, not JSON, so leave as is.
        }
        return new ChatComponentText(text);
    }

    /**
     * Parses a string, which may be plain text, quoted or JSON.
     *
     * @param text            The text to be parsed
     * @param stripFormatting Whether to strip formatting codes (eg colors). When called on
     *                        a dedicated server this must be true.
     * @return The parsed text.
     */
    public static String parse(String text, boolean stripFormatting) {
        IChatComponent component = parse(text);
        if (component == null) {
            return null;
        }
        if (stripFormatting) {
            return net.minecraft.util.EnumChatFormatting
                    .getTextWithoutFormattingCodes(component
                            .getUnformattedText());
        } else {
            return component.getFormattedText();
        }
    }

    public static String getUnformattedText(ItemStack stack, int page) {
        return parse(getPage(stack, page), true);
    }
}
