package com.armaninyow.dibs.mixin.client;

import com.armaninyow.dibs.data.ClientBindingCache;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantEntity.class)
public class ClientWorldParticleMixin {

	@Inject(method = "produceParticles", at = @At("HEAD"), cancellable = true)
	private void onProduceParticles(ParticleEffect particle, CallbackInfo ci) {
		if (particle.getType() != ParticleTypes.HAPPY_VILLAGER) return;
		if (!((Object) this instanceof VillagerEntity villager)) return;
		if (ClientBindingCache.isWrongClaimant(villager.getUuid())) {
			ci.cancel();
		}
	}
}