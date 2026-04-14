package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.network.NetworkHandler;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(VillagerEntity.class)
public class VillagerBedMixin {

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickBedHead(CallbackInfo ci) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		World w = ((EntityAccessor) (Object) this).getDibsWorld();

		if (!(w instanceof ServerWorld world)) {
			return;
		}

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			return;
		}

		UUID myUuid = villager.getUuid();

		// EVICTION — HOME memory
		Optional<GlobalPos> homeOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.HOME);
		if (homeOpt.isPresent()) {
			BlockPos homePos = homeOpt.get().pos();
			UUID bedOwner = data.getBedVillager(homePos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) {
				villager.getBrain().forget(MemoryModuleType.HOME);
				world.getPointOfInterestStorage().releaseTicket(homePos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		// EVICTION — NEAREST_BED memory
		Optional<BlockPos> nearestBedOpt = villager.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_BED);
		if (nearestBedOpt.isPresent()) {
			BlockPos bedPos = nearestBedOpt.get();
			UUID bedOwner = data.getBedVillager(bedPos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) {
				villager.getBrain().forget(MemoryModuleType.NEAREST_BED);
				world.getPointOfInterestStorage().releaseTicket(bedPos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		// GUIDANCE: runs every 20 ticks
		if (world.getTime() % 20 != 0) {
			return;
		}

		if (homeOpt.isEmpty()) {
			BlockPos boundBed = data.getBedForVillager(myUuid);
			if (boundBed != null && world.isChunkLoaded(boundBed)) {
				GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), boundBed);
				villager.getBrain().remember(MemoryModuleType.HOME, globalPos);
			}
		}
	}
}
