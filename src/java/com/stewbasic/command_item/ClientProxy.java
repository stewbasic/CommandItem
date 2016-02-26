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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private final List<Item> itemsToRegister = new ArrayList<Item>();

    @EventHandler
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem()
                .getItemModelMesher();
        for (Item item : itemsToRegister) {
            mesher.register(item, 0, new ModelResourceLocation(
                    CommandItemMod.MODID + ":" + getName(item), "inventory"));

        }
        itemsToRegister.clear();
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

    @Override
    public void register(Item item) {
        super.register(item);
        itemsToRegister.add(item);
    }
}
