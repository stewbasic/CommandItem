/* Notes:
 * - unlocalizedName referred to in lang file, otherwise unexposed
 * - 2nd arg of register{Item|Block} must agree with .json name and ModelResourceLocation?
 * 
 */
package com.stewbasic.command_item;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = CommandItemMod.MODID, name = CommandItemMod.MODNAME,
        version = CommandItemMod.MODVERSION, acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.8,)")
public class CommandItemMod {
    public final static String MODNAME = "Command Item";
    // Must be kept in sync with mcmod.info
    public final static String MODID = "command_item";
    // Must be kept in sync with build.gradle
    public final static String MODVERSION = "0.1";
    // Hopefully javac will cull any unreachable code when this is false.
    public final static boolean DEBUG = false;

    @SidedProxy(clientSide = "com.stewbasic.command_item.ClientProxy",
            serverSide = "com.stewbasic.command_item.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(this, event);
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
