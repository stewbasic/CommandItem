package com.stewbasic.command_item;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateCommandSlateMessage implements IMessage, Runnable {
    private MessageContext ctx = null;
    private NBTTagCompound tag = null;
    private int craft = 0;

    // Message must be default constructible to be passed to SimpleNetworkWrapper.registerMessage.
    @SuppressWarnings("UnusedDeclaration")
    public UpdateCommandSlateMessage() {
    }

    public UpdateCommandSlateMessage(NBTTagCompound tag, int craft) {
        this.tag = tag;
        this.craft = craft;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            tag = (new PacketBuffer(buf)).readNBTTagCompoundFromBuffer();
            craft = buf.readInt();
        } catch (IOException e) {
            tag = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        (new PacketBuffer(buf)).writeNBTTagCompoundToBuffer(tag);
        buf.writeInt(craft);
    }

    @Override
    public void run() {
        if (tag != null) {
            EntityPlayer player = CommandItemMod.proxy.getPlayerEntity(ctx);
            ItemStack stack = player.getHeldItem();
            if (stack.getItem() instanceof CommandSlate) {
                CommandSlate commandSlate = (CommandSlate) stack.getItem();
                CommandRune commandRune = commandSlate.commandRune;
                commandRune.copyNBT(tag, commandSlate.getConfigNBT(stack));
                Container container = player.openContainer;
                if (container instanceof SlateGuiContainer) {
                    SlateGuiContainer slateGuiContainer = (SlateGuiContainer) container;
                    slateGuiContainer.update(stack);
                    if (craft > 0) {
                        slateGuiContainer.craft(craft);
                    }
                }
            }
        }
    }

    public static class Handler implements
            IMessageHandler<UpdateCommandSlateMessage, IMessage> {
        @Override
        public IMessage onMessage(UpdateCommandSlateMessage message,
                                  MessageContext ctx) {
            message.ctx = ctx;
            CommandItemMod.proxy.getThread(ctx).addScheduledTask(message);
            return null;
        }
    }
}
