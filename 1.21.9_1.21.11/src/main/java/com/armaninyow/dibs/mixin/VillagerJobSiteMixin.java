package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.network.NetworkHandler;
import com.armaninyow.dibs.util.VillagerPoiContext;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

// 1.21.9_1.21.11
@Mixin(value = VillagerEntity.class, priority = 1000)
public class VillagerJobSiteMixin {

	private int dibs_jobTickCounter = 0;

	@SuppressWarnings("deprecation")
	@Inject(method = "setVillagerData", at = @At("HEAD"), cancellable = true)
	private void onSetVillagerData(VillagerData data, CallbackInfo ci) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		World w = ((EntityAccessor) (Object) this).getDibsWorld();
		if (!(w instanceof ServerWorld world)) return;


		// profession() returns RegistryEntry with getKey()
		boolean currentlyUnemployed = villager.getVillagerData().profession().getKey()
				.map(k -> k.getValue().getPath().equals("none")).orElse(true);
		boolean gettingEmployed = data.profession().getKey()
				.map(k -> !k.getValue().getPath().equals("none")).orElse(false);

		if (!currentlyUnemployed || !gettingEmployed) return;

		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isEmpty()) return;

		BlockPos jobSitePos = jobSiteOpt.get().pos();
		VillagerBindingData bindingData = VillagerBindingData.get(world);
		if (bindingData == null) return;

		UUID blockOwner = bindingData.getVillagerForBlock(jobSitePos);
		if (blockOwner != null && !blockOwner.equals(villager.getUuid())) {
			ci.cancel();
			villager.getBrain().forget(MemoryModuleType.JOB_SITE);
			villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
			world.getPointOfInterestStorage().releaseTicket(jobSitePos);
		}
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickHead(CallbackInfo ci) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		World w = ((EntityAccessor) (Object) this).getDibsWorld();
		if (!(w instanceof ServerWorld world)) return;

		VillagerPoiContext.set(villager.getUuid(), world);


		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		UUID myUuid = villager.getUuid();

		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isPresent()) {
			BlockPos jobSitePos = jobSiteOpt.get().pos();
			UUID blockOwner = data.getVillagerForBlock(jobSitePos);
			if (blockOwner != null && !blockOwner.equals(myUuid)) {
				villager.getBrain().forget(MemoryModuleType.JOB_SITE);
				villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
				world.getPointOfInterestStorage().releaseTicket(jobSitePos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		if (++dibs_jobTickCounter % 20 != 0) return;

		// Throttled: evict wrong claimant walking TOWARD a bound workstation
		Optional<GlobalPos> potentialOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		if (potentialOpt.isPresent()) {
			BlockPos potentialPos = potentialOpt.get().pos();
			UUID blockOwner = data.getVillagerForBlock(potentialPos);
			if (blockOwner != null && !blockOwner.equals(myUuid)) {
				villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
				world.getPointOfInterestStorage().releaseTicket(potentialPos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		if (jobSiteOpt.isEmpty()) {
			BlockPos boundPos = data.getBlockForVillager(myUuid);
			if (boundPos != null && world.isChunkLoaded(boundPos)) {
				villager.getBrain().remember(MemoryModuleType.JOB_SITE,
						GlobalPos.create(world.getRegistryKey(), boundPos));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("RETURN"))
	private void onTickReturn(CallbackInfo ci) {
		VillagerPoiContext.clear();
	}
}
