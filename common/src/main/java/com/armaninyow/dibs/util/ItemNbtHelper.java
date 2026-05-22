package com.armaninyow.dibs.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemNbtHelper {
	private static final String VILLAGER_UUID_MOST  = "DibsVillagerUUIDMost";
	private static final String VILLAGER_UUID_LEAST = "DibsVillagerUUIDLeast";
	private static final String VILLAGER_NAME_KEY   = "DibsVillagerName";

	public static void tagItemWithVillager(ItemStack stack, Villager villager) {
		CompoundTag nbt = new CompoundTag();
		UUID uuid = villager.getUUID();
		nbt.putLong(VILLAGER_UUID_MOST,  uuid.getMostSignificantBits());
		nbt.putLong(VILLAGER_UUID_LEAST, uuid.getLeastSignificantBits());

		String professionName = villager.getVillagerData().profession().unwrapKey()
				.map(k -> k.identifier().getPath().equals("none") ? "villager" : k.identifier().getPath())
				.orElse("villager");
		nbt.putString(VILLAGER_NAME_KEY, professionName);

		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		updateItemLore(stack, professionName, uuid);
	}

	public static UUID getVillagerUuid(ItemStack stack) {
		CustomData component = stack.get(DataComponents.CUSTOM_DATA);
		if (component != null) {
			CompoundTag nbt = component.copyTag();
			if (nbt.contains(VILLAGER_UUID_MOST) && nbt.contains(VILLAGER_UUID_LEAST)) {
				long most  = ((LongTag) nbt.get(VILLAGER_UUID_MOST)).value();
				long least = ((LongTag) nbt.get(VILLAGER_UUID_LEAST)).value();
				return new UUID(most, least);
			}
		}
		return null;
	}

	public static boolean hasVillagerBinding(ItemStack stack) {
		return getVillagerUuid(stack) != null;
	}

	public static void removeVillagerBinding(ItemStack stack) {
		stack.remove(DataComponents.CUSTOM_DATA);
		stack.remove(DataComponents.LORE);
	}

	private static void updateItemLore(ItemStack stack, String professionName, UUID villagerUuid) {
		String shortUuid = villagerUuid.toString().substring(0, 8);
		Component loreText = Component.literal("Bound to: ")
				.withStyle(ChatFormatting.GRAY)
				.append(Component.literal(capitalize(professionName) + " #" + shortUuid)
						.withStyle(ChatFormatting.AQUA));
		List<Component> lore = new ArrayList<>();
		lore.add(loreText);
		stack.set(DataComponents.LORE, new ItemLore(lore));
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}