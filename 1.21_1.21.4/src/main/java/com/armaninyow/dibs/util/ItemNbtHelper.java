package com.armaninyow.dibs.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLong;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 1.21_1.21.4
public class ItemNbtHelper {
	private static final String VILLAGER_UUID_MOST = "DibsVillagerUUIDMost";
	private static final String VILLAGER_UUID_LEAST = "DibsVillagerUUIDLeast";
	private static final String VILLAGER_NAME_KEY = "DibsVillagerName";

	public static void tagItemWithVillager(ItemStack stack, VillagerEntity villager) {
		NbtCompound nbt = new NbtCompound();
		UUID uuid = villager.getUuid();
		nbt.putLong(VILLAGER_UUID_MOST, uuid.getMostSignificantBits());
		nbt.putLong(VILLAGER_UUID_LEAST, uuid.getLeastSignificantBits());

		// getProfession() returns VillagerProfession directly — use registry lookup
		Identifier profId = Registries.VILLAGER_PROFESSION.getId(villager.getVillagerData().getProfession());
		String professionName = (profId == null || profId.getPath().equals("none")) ? "villager" : profId.getPath();
		nbt.putString(VILLAGER_NAME_KEY, professionName);

		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		updateItemLore(stack, professionName, uuid);
	}

	public static UUID getVillagerUuid(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component != null) {
			NbtCompound nbt = component.copyNbt();
			if (nbt.contains(VILLAGER_UUID_MOST) && nbt.contains(VILLAGER_UUID_LEAST)) {
				long most = ((NbtLong) nbt.get(VILLAGER_UUID_MOST)).longValue();
				long least = ((NbtLong) nbt.get(VILLAGER_UUID_LEAST)).longValue();
				return new UUID(most, least);
			}
		}
		return null;
	}

	public static boolean hasVillagerBinding(ItemStack stack) { return getVillagerUuid(stack) != null; }

	public static void removeVillagerBinding(ItemStack stack) {
		stack.remove(DataComponentTypes.CUSTOM_DATA);
		stack.remove(DataComponentTypes.LORE);
	}

	private static void updateItemLore(ItemStack stack, String professionName, UUID villagerUuid) {
		String shortUuid = villagerUuid.toString().substring(0, 8);
		Text loreText = Text.literal("Bound to: ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(capitalize(professionName) + " #" + shortUuid).formatted(Formatting.AQUA));
		List<Text> lore = new ArrayList<>();
		lore.add(loreText);
		stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
