package com.stewbasic.command_item;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This item can mimic the appearance of other items. It modifies the
 * damage/metadata of the {@link net.minecraft.item.ItemStack ItemStack}, so
 * subclasses should take care modifying these. More explicitly, subclasses
 * should pass to the MimicItem constructor the number of metadata values to
 * reserve, n. The subclass can assign models to values 0 to n-1 as usual. For a
 * given ItemStack it should either use a reserved metadata or call setDisplay,
 * but not both.
 */
public class MimicItem extends Item {
	// @formatter:off
	/* The appearance of the mimicked item is keyed by (item id, metadata). This
	 * item must assign a metadata value to each key used. We choose the next
	 * unused metadata and keep a map. Most of this logic is in getMetadata.
	 * This method is called by the client every frame for every visible item,
	 * so we don’t want to look up the key each time. Unfortunately there aren’t
	 * enough hooks to determine when the logic is needed:
	 * - onCreated covers crafting
	 * - updateItemStackNBT covers world load, but doesn’t expose the item stack
	 * - No hook when server pushes item to client
	 * - No hook for /give
	 * Our solution is add an NBT tag PROCESSED after running the logic _on
	 * client side only_. When an item is created or the server pushes to client
	 * (on inventory update), we see the tag is unset and check the key ->
	 * metadata map. There is a special case when the client also acts as server
	 * (SP or hosting LAN), which we detect using FML connection events.
	 */
	// @formatter:on

	private static class MimicKey {
		String id;
		int metadata;

		MimicKey(String id, int metadata) {
			this.id = id;
			this.metadata = metadata;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other.getClass() != MimicKey.class) {
				return false;
			}
			MimicKey otherKey = (MimicKey) other;
			return id.equals(otherKey.id) && metadata == otherKey.metadata;
		}

		@Override
		public int hashCode() {
			return id.hashCode() ^ metadata;
		}

		@Override
		public String toString() {
			return id + ":" + metadata;
		}
	}

	// Forge extends ItemModelMesher to ItemModelMesherForge. Unfortunately
	// neither class offers a way to retrieve model location from item, so we
	// hack it apart with reflection >_>.
	private Field locationsField = null;
	private String processedTag = null;
	private final static String TAG = "mimicItem", ID = "id", META = "meta",
			PROCESSED = "proc", PROCESSED_COMBINED = "procl";
	private Map<MimicKey, Integer> keyToMeta = null;
	private int lastMeta, reservedMeta;
	private Side side;

	public MimicItem(int reservedMeta) {
		// Always reserve meta = 0.
		this.reservedMeta = (reservedMeta < 1) ? 1 : reservedMeta;
		side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			// Set up client specific stuff.
			keyToMeta = new HashMap<MimicKey, Integer>();
			resetMetaMap();
			try {
				locationsField = ItemModelMesherForge.class
						.getDeclaredField("locations");
				locationsField.setAccessible(true);
			} catch (Exception e) {
				if (CommandItemMod.DEBUG) {
					System.out.println(e);
				}
			}
			FMLCommonHandler.instance().bus().register(this);
		}
	}

	public void setDisplay(ItemStack stack, ItemStack display) {
		String id;
		int metadata;
		Item displayItem = display.getItem();
		if (displayItem instanceof MimicItem) {
			NBTTagCompound rhs = display.getSubCompound(TAG, false);
			if (rhs != null && rhs.hasKey(ID, NBT.TAG_STRING)) {
				id = rhs.getString(ID);
				metadata = rhs.getInteger(META);
			} else {
				setDamage(stack, getDamage(display));
				return;
			}
		} else {
			id = Item.itemRegistry.getNameForObject(displayItem).toString();
			metadata = display.getMetadata();
		}
		NBTTagCompound nbt = stack.getSubCompound(TAG, true);
		nbt.setString(ID, id);
		nbt.setInteger(META, metadata);
	}

	@Override
	public int getMetadata(ItemStack stack) {
		int metadata = getDamage(stack);
		if (processedTag != null) {
			NBTTagCompound nbt = stack.getSubCompound(TAG, false);
			if (nbt != null && !nbt.hasKey(processedTag)) {
				if (nbt.hasKey(ID, NBT.TAG_STRING)) {
					MimicKey key = new MimicKey(nbt.getString(ID),
							nbt.getInteger(META));
					boolean needCopy = false;
					synchronized (this) {
						if (keyToMeta.containsKey(key)) {
							metadata = keyToMeta.get(key);
						} else {
							// Ignore the possibility of overflow here...
							metadata = ++lastMeta;
							if (CommandItemMod.DEBUG) {
								System.out.println(key + " -> " + metadata);
							}
							keyToMeta.put(key, metadata);
							needCopy = true;
						}
					}
					if (needCopy) {
						copyModel(key, stack.getItem(), metadata);
					}
					setDamage(stack, metadata);
				} else if (CommandItemMod.DEBUG) {
					System.out.println("No mimicItem, leaving metadata = "
							+ metadata);
				}
				nbt.setBoolean(processedTag, true);
			}
		}
		return metadata;
	}

	@Override
	public boolean updateItemStackNBT(NBTTagCompound nbt) {
		// Ensure that an ItemStack is marked unprocessed when it is loaded.
		if (nbt != null && nbt.hasKey(TAG, NBT.TAG_COMPOUND)) {
			NBTTagCompound tag = nbt.getCompoundTag(TAG);
			tag.removeTag(PROCESSED_COMBINED);
			// This should never be necessary, since PROCESSED is only used by
			// remote clients. Just in case...
			tag.removeTag(PROCESSED);
		}
		return super.updateItemStackNBT(nbt);
	}

	private void copyModel(MimicKey key, Item item, int metadata) {
		if (locationsField == null) {
			return;
		}
		Item keyItem = Item.getByNameOrId(key.id);
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem()
				.getItemModelMesher();
		try {
			Object o = locationsField.get(mesher);
			@SuppressWarnings("unchecked")
			IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>> locations = (IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>>) o;
			TIntObjectHashMap<ModelResourceLocation> metaMap = locations
					.get(keyItem);
			if (metaMap == null) {
				return;
			}
			ModelResourceLocation location = metaMap.get(key.metadata);
			if (location == null) {
				return;
			}
			mesher.register(item, metadata, location);
		} catch (Exception e) {
			if (CommandItemMod.DEBUG) {
				System.out.println(e);
			}
		}
	}

	// The next two events are used to distinguish the case of a combined
	// client+server (ie singleplayer or hosting LAN). We only register as a
	// listener when side==Side.CLIENT, but check this in case a subclass gets registered.
	@SubscribeEvent
	public void onServerConnect(ServerConnectionFromClientEvent event) {
		if (side == Side.SERVER) {
			return;
		}
		// Must be a combined client+server.
		if (CommandItemMod.DEBUG) {
			System.out.println("Combined client+server");
		}
		synchronized (this) {
			processedTag = PROCESSED_COMBINED;
		}
	}

	@SubscribeEvent
	public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
		if (side == Side.SERVER) {
			return;
		}
		// Reset everything.
		if (CommandItemMod.DEBUG) {
			System.out.println("Resetting");
		}
		synchronized (this) {
			resetMetaMap();
		}
	}

	private void resetMetaMap() {
		lastMeta = reservedMeta - 1;
		processedTag = PROCESSED;
		keyToMeta.clear();
	}
}
