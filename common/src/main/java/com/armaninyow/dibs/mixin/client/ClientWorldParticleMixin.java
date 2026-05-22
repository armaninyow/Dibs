package com.armaninyow.dibs.mixin.client;

import com.armaninyow.dibs.data.ClientBindingCache;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public class ClientWorldParticleMixin {

	@Inject(method = "addParticlesAroundSelf", at = @At("HEAD"), cancellable = true)
	private void onProduceParticles(ParticleOptions particle, CallbackInfo ci) {
		if (particle.getType() != ParticleTypes.HAPPY_VILLAGER) return;
		if (!((Object) this instanceof Villager villager)) return;
		if (ClientBindingCache.isWrongClaimant(villager.getUUID())) {
			ci.cancel();
		}
	}
}