package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Villager.class)
public class VillagerBedMixin {

	private int dibs_bedTickCounter = 0;

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickBedHead(CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		Level w = ((EntityAccessor) (Object) this).getDibsWorld();
		if (!(w instanceof ServerLevel world)) return;

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		UUID myUuid = villager.getUUID();

		Optional<GlobalPos> homeOpt = villager.getBrain().getMemory(MemoryModuleType.HOME);
		if (homeOpt.isPresent()) {
			BlockPos homePos = homeOpt.get().pos();
			UUID bedOwner = data.getBedVillager(homePos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) {
				villager.getBrain().eraseMemory(MemoryModuleType.HOME);
				world.getPoiManager().release(homePos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		Optional<BlockPos> nearestBedOpt = villager.getBrain().getMemory(MemoryModuleType.NEAREST_BED);
		if (nearestBedOpt.isPresent()) {
			BlockPos bedPos = nearestBedOpt.get();
			UUID bedOwner = data.getBedVillager(bedPos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) {
				villager.getBrain().eraseMemory(MemoryModuleType.NEAREST_BED);
				world.getPoiManager().release(bedPos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		if (++dibs_bedTickCounter % 20 != 0) return;

		if (homeOpt.isEmpty()) {
			BlockPos boundBed = data.getBedForVillager(myUuid);
			if (boundBed != null && world.isLoaded(boundBed)) {
				GlobalPos globalPos = GlobalPos.of(world.dimension(), boundBed);
				villager.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
			}
		}
	}
}