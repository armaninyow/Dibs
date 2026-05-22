package com.armaninyow.dibs.mixin.client;

import com.armaninyow.dibs.data.ClientBindingCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin {

	@Shadow protected ClientLevel level;

	@Inject(
		method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private <T extends ParticleOptions> void onAddParticle(
			T particle, double x, double y, double z,
			double vx, double vy, double vz,
			CallbackInfoReturnable<Particle> cir) {

		if (particle.getType() != ParticleTypes.HAPPY_VILLAGER) return;
		if (level == null) return;

		for (Entity entity : level.entitiesForRendering()) {
			if (!(entity instanceof Villager villager)) continue;
			if (entity.distanceToSqr(x, y, z) > 4.0) continue;
			if (ClientBindingCache.isWrongClaimant(villager.getUUID())) {
				cir.setReturnValue(null);
				return;
			}
		}
	}
}