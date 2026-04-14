package com.armaninyow.dibs.mixin.client;

import com.armaninyow.dibs.data.ClientBindingCache;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

	@Shadow protected ClientWorld world;

	@Inject(
		method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private <T extends ParticleEffect> void onAddParticle(
			T particle, double x, double y, double z,
			double vx, double vy, double vz,
			CallbackInfoReturnable<Particle> cir) {

		if (particle.getType() != ParticleTypes.HAPPY_VILLAGER) return;
		if (world == null) return;

		for (Entity entity : world.getEntities()) {
			if (!(entity instanceof VillagerEntity villager)) continue;
			if (entity.squaredDistanceTo(x, y, z) > 4.0) continue;
			if (ClientBindingCache.isWrongClaimant(villager.getUuid())) {
				cir.setReturnValue(null);
				return;
			}
		}
	}
}