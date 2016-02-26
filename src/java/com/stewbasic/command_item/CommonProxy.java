package com.stewbasic.command_item;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;

public class CommonProxy implements IGuiHandler {
    // To keep packet numbers consistent, only append to these lists.
    private static enum MessageType {
        UPDATE_COMMAND_SLATE
    }

    static enum GuiType {
        SLATE_GUI
    }

    private CommandItemMod mod;

    private SimpleNetworkWrapper network;

    public CommonProxy() {
    }

    @EventHandler
    public void preInit(CommandItemMod mod, FMLPreInitializationEvent event) {
        this.mod = mod;
        registerNetworkHandlers();
        CommandRune commandRune = new CommandRune();
        register(commandRune);
        CommandSlate commandSlate = new CommandSlate(commandRune);
        register(commandSlate);
        HashSet<Item> books = new HashSet<Item>();
        books.add(Items.writable_book);
        books.add(Items.written_book);
        GameRegistry.addRecipe(new Recipe(commandSlate, books, commandRune));
        RecipeSorter.register("command_item:shapeless", Recipe.class,
                SHAPELESS, "");
        GameRegistry.addShapelessRecipe(new ItemStack(Items.diamond),
                Blocks.dirt, Blocks.dirt);
    }

    private void registerNetworkHandlers() {
        NetworkRegistry networkRegistry = NetworkRegistry.INSTANCE;
        network = networkRegistry
                .newSimpleChannel(CommandItemMod.MODID);
        network.registerMessage(UpdateCommandSlateMessage.Handler.class,
                UpdateCommandSlateMessage.class,
                MessageType.UPDATE_COMMAND_SLATE.ordinal(),
                Side.SERVER);
        networkRegistry.registerGuiHandler(mod, this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }

    public IThreadListener getThread(MessageContext ctx) {
        return (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
    }

    public void sendToServer(IMessage message) {
        network.sendToServer(message);
    }

    public void openGui(EntityPlayer player, GuiType type, World world, int posX, int posY,
                        int posZ) {
        player.openGui(mod, type.ordinal(), world, posX, posY, posZ);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y,
                                      int z) {
        if (ID == GuiType.SLATE_GUI.ordinal()) {
            return SlateGuiContainer.Make(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y,
                                      int z) {
        if (ID == GuiType.SLATE_GUI.ordinal()) {
            SlateGuiContainer container = SlateGuiContainer.Make(player);
            return container == null ? null : new SlateGui(container);
        }
        return null;
    }


    public void register(Item item) {
        GameRegistry.registerItem(item, getName(item));
    }

    protected static String getName(Item item) {
        return item.getUnlocalizedName().substring(5);
    }
}
