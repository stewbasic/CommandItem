package com.stewbasic.command_item;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ItemModelMesherForge;

public class ModelCopier {
	// Forge extends ItemModelMesher to ItemModelMesherForge. Unfortunately
	// neither class offers a way to retrieve model location from item, so we
	// hack it apart with reflection >_>.
	private Field locationsField = null;

	public ModelCopier() {
		try {
			locationsField = ItemModelMesherForge.class
					.getDeclaredField("locations");
			locationsField.setAccessible(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void copy(ItemStack from, ItemStack to) {
		if (locationsField == null) {
			return;
		}
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem()
				.getItemModelMesher();
		try {
			Object o = locationsField.get(mesher);
			IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>> locations = (IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>>) o;
			TIntObjectHashMap<ModelResourceLocation> metaMap = locations
					.get(from.getItem());
			if (metaMap == null) {
				return;
			}
			ModelResourceLocation location = metaMap.get(from.getMetadata());
			if (location == null) {
				return;
			}
			mesher.register(to.getItem(), to.getMetadata(), location);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
