package com.armaninyow.dibs.util;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class VillagerPoiContext {

	private static final ThreadLocal<UUID> CURRENT_VILLAGER = new ThreadLocal<>();
	private static final ThreadLocal<ServerLevel> CURRENT_WORLD = new ThreadLocal<>();

	public static void set(UUID uuid, ServerLevel world) {
		CURRENT_VILLAGER.set(uuid);
		CURRENT_WORLD.set(world);
	}

	public static UUID get() {
		return CURRENT_VILLAGER.get();
	}

	public static ServerLevel getWorld() {
		return CURRENT_WORLD.get();
	}

	public static void clear() {
		CURRENT_VILLAGER.remove();
		CURRENT_WORLD.remove();
	}
}