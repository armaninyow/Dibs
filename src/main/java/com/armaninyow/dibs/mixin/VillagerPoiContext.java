package com.armaninyow.dibs.util;

import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

/**
 * Thread-local context used to pass the currently-ticking villager UUID and
 * its world into PointOfInterestStorage so the POI scan can filter out
 * positions that are already bound to a different villager.
 *
 * Safe to use because the Minecraft server tick (and therefore all brain
 * tasks) runs on a single thread.
 */
public class VillagerPoiContext {

	private static final ThreadLocal<UUID> CURRENT_VILLAGER = new ThreadLocal<>();
	private static final ThreadLocal<ServerWorld> CURRENT_WORLD = new ThreadLocal<>();

	public static void set(UUID uuid, ServerWorld world) {
		CURRENT_VILLAGER.set(uuid);
		CURRENT_WORLD.set(world);
	}

	public static UUID get() {
		return CURRENT_VILLAGER.get();
	}

	public static ServerWorld getWorld() {
		return CURRENT_WORLD.get();
	}

	public static void clear() {
		CURRENT_VILLAGER.remove();
		CURRENT_WORLD.remove();
	}
}