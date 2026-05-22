package com.armaninyow.dibs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ParticleTrailRenderer {

	private static final double STEP = 0.25;

	public static void drawTrailToBlock(UUID villagerUuid, BlockPos targetPos) {
		Minecraft client = Minecraft.getInstance();
		ClientLevel world = client.level;
		if (world == null) return;

		Villager villager = findVillager(world, villagerUuid);
		if (villager == null) return;

		Vec3 start = new Vec3(villager.getX(), villager.getY() + villager.getBbHeight() / 2.0, villager.getZ());
		Vec3 end   = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5);

		Vec3 delta = end.subtract(start);
		double length = delta.length();
		if (length == 0) return;

		Vec3 step = delta.normalize().scale(STEP);
		int steps = (int) (length / STEP);

		for (int i = 0; i <= steps; i++) {
			double x = start.x + step.x * i;
			double y = start.y + step.y * i;
			double z = start.z + step.z * i;
			client.particleEngine.createParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
		}
	}

	private static Villager findVillager(ClientLevel world, UUID uuid) {
		for (Entity entity : world.entitiesForRendering()) {
			if (entity instanceof Villager villager && villager.getUUID().equals(uuid)) {
				return villager;
			}
		}
		return null;
	}
}