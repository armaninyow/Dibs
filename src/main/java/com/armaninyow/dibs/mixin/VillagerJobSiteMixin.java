package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.network.NetworkHandler;
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

@Mixin(VillagerEntity.class)
public class VillagerJobSiteMixin {

	// Intercept when vanilla tries to assign a profession to a villager.
	// If the villager's job site is bound to a different villager, cancel the assignment.
	@SuppressWarnings("deprecation")
	@Inject(method = "setVillagerData", at = @At("HEAD"), cancellable = true)
	private void onSetVillagerData(VillagerData data, CallbackInfo ci) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		World w = ((EntityAccessor) villager).getWorld();

		if (!(w instanceof ServerWorld world)) {
			return;
		}

		// Only care about gaining a profession (changing from none to something)
		boolean currentlyUnemployed = villager.getVillagerData().profession().getKey()
				.map(k -> k.getValue().getPath().equals("none"))
				.orElse(true);
		boolean gettingEmployed = data.profession().getKey()
				.map(k -> !k.getValue().getPath().equals("none"))
				.orElse(false);

		if (!currentlyUnemployed || !gettingEmployed) {
			return;
		}

		// Check if this villager's claimed job site is bound to someone else
		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isEmpty()) {
			return;
		}

		BlockPos jobSitePos = jobSiteOpt.get().pos();
		VillagerBindingData bindingData = VillagerBindingData.get(world);
		if (bindingData == null) {
			return;
		}

		UUID blockOwner = bindingData.getVillagerForBlock(jobSitePos);
		if (blockOwner != null && !blockOwner.equals(villager.getUuid())) {
			// Cancel the profession assignment and fully evict
			ci.cancel();
			villager.getBrain().forget(MemoryModuleType.JOB_SITE);
			villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
			world.getPointOfInterestStorage().releaseTicket(jobSitePos);
		}
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		World w = ((EntityAccessor) villager).getWorld();

		if (!(w instanceof ServerWorld world)) {
			return;
		}

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			return;
		}

		UUID myUuid = villager.getUuid();

		// EVICTION: runs every tick — kick wrong villagers off bound blocks immediately
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

		// Also block villagers walking TOWARD a bound block they don't own
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

		// GUIDANCE: runs every 20 ticks — steer bound villager toward its block
		if (world.getTime() % 20 != 0) {
			return;
		}

		if (jobSiteOpt.isEmpty()) {
			BlockPos boundPos = data.getBlockForVillager(myUuid);
			if (boundPos != null && world.isChunkLoaded(boundPos)) {
				GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), boundPos);
				villager.getBrain().remember(MemoryModuleType.JOB_SITE, globalPos);
			}
		}
	}
}