/* Notes:
 * - unlocalizedName referred to in lang file, otherwise unexposed
 * - 2nd arg of register{Item|Block} must agree with .json name and ModelResourceLocation?
 * 
 */
package com.stewbasic.command_item;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = CommandItemMod.MODID, name = CommandItemMod.MODNAME, version = CommandItemMod.MODVERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.8,)")
public class CommandItemMod {
    public static final String MODNAME = "Command Item";
    // Must be kept in sync with mcmod.info
	public static final String MODID = "command_item";
    // Must be kept in sync with build.gradle
	public static final String MODVERSION = "0.1";
	// Hopefully javac will cull any unreachable code when this is false.
	public static final boolean DEBUG = false;

	private static enum Messages {
		UPDATE_COMMAND_SLATE
	}

	public static SimpleNetworkWrapper network;

	@Instance(value = CommandItemMod.MODID)
	public static CommandItemMod instance;

	@SidedProxy(clientSide = "com.stewbasic.command_item.ClientProxy", serverSide = "com.stewbasic.command_item.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		network = NetworkRegistry.INSTANCE
				.newSimpleChannel(CommandItemMod.MODID);
		network.registerMessage(UpdateCommandSlateMessage.Handler.class,
				UpdateCommandSlateMessage.class,
				Messages.UPDATE_COMMAND_SLATE.ordinal(), Side.SERVER);
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
}
