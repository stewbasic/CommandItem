package com.stewbasic.command_item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Note that this item is not added to any creative inventory tab, and can only
 * be obtained using /give. This is analogous to the command block.
 */
class CommandSlate extends Item {
    private final static String name = "command_slate";
    private final static String TAG = "config";

    final CommandRune commandRune;

    public CommandSlate(CommandRune commandRune) {
        super();
        setMaxStackSize(64);
        setUnlocalizedName(name);
        this.commandRune = commandRune;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world,
                                      EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            CommandItemMod.proxy.openGui(player, CommonProxy.GuiType.SLATE_GUI, world,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return stack;
    }

    public NBTTagCompound getConfigNBT(ItemStack stack) {
        return stack.getSubCompound(TAG, true);
    }

    public boolean hasConfigNBT(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        return (nbt != null) && nbt.hasKey(TAG, NBT.TAG_COMPOUND);
    }
}
