package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerLevel.class)
public class ServerWorldStatusMixin {

	@Inject(method = "broadcastEntityEvent", at = @At("HEAD"), cancellable = true)
	private void onSendEntityStatus(Entity entity, byte status, CallbackInfo ci) {
		if (status != 14) return;
		if (!(entity instanceof Villager villager)) return;

		ServerLevel world = (ServerLevel) (Object) this;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		UUID myUuid = villager.getUUID();

		Optional<GlobalPos> jobSiteOpt = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
		if (jobSiteOpt.isPresent()) {
			BlockPos pos = jobSiteOpt.get().pos();
			UUID owner = data.getVillagerForBlock(pos);
			if (owner != null && !owner.equals(myUuid)) { ci.cancel(); return; }
		}

		Optional<GlobalPos> potentialOpt = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		if (potentialOpt.isPresent()) {
			BlockPos pos = potentialOpt.get().pos();
			UUID owner = data.getVillagerForBlock(pos);
			if (owner != null && !owner.equals(myUuid)) { ci.cancel(); return; }
		}

		Optional<GlobalPos> homeOpt = villager.getBrain().getMemory(MemoryModuleType.HOME);
		if (homeOpt.isPresent()) {
			BlockPos pos = homeOpt.get().pos();
			UUID bedOwner = data.getBedVillager(pos);
			if (bedOwner != null && !bedOwner.equals(myUuid)) { ci.cancel(); }
		}
	}
}