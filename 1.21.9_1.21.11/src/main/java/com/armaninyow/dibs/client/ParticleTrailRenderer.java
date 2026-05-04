package com.armaninyow.dibs.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

// 1.21.9_1.21.11
public class ParticleTrailRenderer {

	private static final double STEP = 0.25;

	/**
	 * Draws a trail of END_ROD particles from the bound villager's position
	 * to the center-top of the bound block position.
	 * Uses client.particleManager directly since all ClientWorld.addParticle
	 * overloads with world-space coordinates are private in these versions.
	 */
	public static void drawTrailToBlock(UUID villagerUuid, BlockPos targetPos) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		if (world == null) return;

		VillagerEntity villager = findVillager(world, villagerUuid);
		if (villager == null) return;

		Vec3d start = new Vec3d(villager.getX(), villager.getY() + villager.getHeight() / 2.0, villager.getZ());
		Vec3d end = new Vec3d(
				targetPos.getX() + 0.5,
				targetPos.getY() + 1.0,
				targetPos.getZ() + 0.5
		);

		Vec3d delta = end.subtract(start);
		double length = delta.length();
		if (length == 0) return;

		Vec3d step = delta.normalize().multiply(STEP);
		int steps = (int) (length / STEP);

		for (int i = 0; i <= steps; i++) {
			double x = start.x + step.x * i;
			double y = start.y + step.y * i;
			double z = start.z + step.z * i;
			client.particleManager.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
		}
	}

	private static VillagerEntity findVillager(ClientWorld world, UUID uuid) {
		for (Entity entity : world.getEntities()) {
			if (entity instanceof VillagerEntity villager && villager.getUuid().equals(uuid)) {
				return villager;
			}
		}
		return null;
	}
}