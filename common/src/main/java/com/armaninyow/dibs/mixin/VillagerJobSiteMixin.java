package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.network.NetworkHandler;
import com.armaninyow.dibs.util.VillagerPoiContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = Villager.class, priority = 1000)
public class VillagerJobSiteMixin {

	private int dibs_jobTickCounter = 0;

	@SuppressWarnings("deprecation")
	@Inject(method = "setVillagerData", at = @At("HEAD"), cancellable = true)
	private void onSetVillagerData(VillagerData data, CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		Level w = ((EntityAccessor) (Object) this).getDibsWorld();
		if (!(w instanceof ServerLevel world)) return;

		boolean currentlyUnemployed = villager.getVillagerData().profession().unwrapKey()
				.map(k -> k.identifier().getPath().equals("none")).orElse(true);
		boolean gettingEmployed = data.profession().unwrapKey()
				.map(k -> !k.identifier().getPath().equals("none")).orElse(false);

		if (!currentlyUnemployed || !gettingEmployed) return;

		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isEmpty()) return;

		BlockPos jobSitePos = jobSiteOpt.get().pos();
		VillagerBindingData bindingData = VillagerBindingData.get(world);
		if (bindingData == null) return;

		UUID blockOwner = bindingData.getVillagerForBlock(jobSitePos);
		if (blockOwner != null && !blockOwner.equals(villager.getUUID())) {
			ci.cancel();
			villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
			villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
			world.getPoiManager().release(jobSitePos);
		}
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickHead(CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		Level w = ((EntityAccessor) (Object) this).getDibsWorld();
		if (!(w instanceof ServerLevel world)) return;

		VillagerPoiContext.set(villager.getUUID(), world);

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		UUID myUuid = villager.getUUID();

		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isPresent()) {
			BlockPos jobSitePos = jobSiteOpt.get().pos();
			UUID blockOwner = data.getVillagerForBlock(jobSitePos);
			if (blockOwner != null && !blockOwner.equals(myUuid)) {
				villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
				villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
				world.getPoiManager().release(jobSitePos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		if (++dibs_jobTickCounter % 20 != 0) return;

		Optional<GlobalPos> potentialOpt = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		if (potentialOpt.isPresent()) {
			BlockPos potentialPos = potentialOpt.get().pos();
			UUID blockOwner = data.getVillagerForBlock(potentialPos);
			if (blockOwner != null && !blockOwner.equals(myUuid)) {
				villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
				world.getPoiManager().release(potentialPos);
				NetworkHandler.sendWrongClaimant(world, myUuid, true);
				return;
			}
		}

		if (jobSiteOpt.isEmpty()) {
			BlockPos boundPos = data.getBlockForVillager(myUuid);
			if (boundPos != null && world.isLoaded(boundPos)) {
				villager.getBrain().setMemory(MemoryModuleType.JOB_SITE,
						GlobalPos.of(world.dimension(), boundPos));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At("RETURN"))
	private void onTickReturn(CallbackInfo ci) {
		VillagerPoiContext.clear();
	}
}