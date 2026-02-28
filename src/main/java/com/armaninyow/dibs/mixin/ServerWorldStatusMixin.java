package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerWorld.class)
public class ServerWorldStatusMixin {

	// Status byte 14 = HAPPY_PARTICLES (villager found job site or bed)
	// Status byte 12 = SAD_PARTICLES
	// We block status 14 for villagers that don't own the block/bed they're claiming
	@Inject(method = "sendEntityStatus", at = @At("HEAD"), cancellable = true)
	private void onSendEntityStatus(Entity entity, byte status, CallbackInfo ci) {
		if (status != 14) return; // 14 = happy villager particles
		if (!(entity instanceof VillagerEntity villager)) return;

		ServerWorld world = (ServerWorld) (Object) this;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		UUID myUuid = villager.getUuid();

		// Check JOB_SITE memory (workstation claim)
		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isPresent()) {
			BlockPos pos = jobSiteOpt.get().pos();
			UUID owner = data.getVillagerForBlock(pos);
			if (owner != null && !owner.equals(myUuid)) {
				ci.cancel();
				return;
			}
		}

		// Check POTENTIAL_JOB_SITE memory (workstation approach)
		Optional<GlobalPos> potentialOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		if (potentialOpt.isPresent()) {
			BlockPos pos = potentialOpt.get().pos();
			UUID owner = data.getVillagerForBlock(pos);
			if (owner != null && !owner.equals(myUuid)) {
				ci.cancel();
				return;
			}
		}

		// Check HOME memory (bed claim)
		Optional<GlobalPos> homeOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.HOME);
		if (homeOpt.isPresent()) {
			BlockPos pos = homeOpt.get().pos();
			UUID bedOwner = data.getBedVillager(pos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) {
				ci.cancel();
			}
		}
	}
}