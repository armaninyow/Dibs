package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.VillagerPoiContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PoiManager.class)
public class PointOfInterestStorageMixin {

	@Inject(
		method = "getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true,
		require = 0
	)
	private void filterBoundPoisSquare(
			Predicate<Holder<PoiType>> typePredicate,
			BlockPos pos,
			int radius,
			PoiManager.Occupancy occupationStatus,
			CallbackInfoReturnable<Stream<PoiRecord>> cir) {
		Stream<PoiRecord> filtered = filterPoiStream(cir.getReturnValue());
		if (filtered != null) cir.setReturnValue(filtered);
	}

	@Inject(
		method = "getInCircle(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true,
		require = 0
	)
	private void filterBoundPoisCircle(
			Predicate<Holder<PoiType>> typePredicate,
			BlockPos pos,
			int radius,
			PoiManager.Occupancy occupationStatus,
			CallbackInfoReturnable<Stream<PoiRecord>> cir) {
		Stream<PoiRecord> filtered = filterPoiStream(cir.getReturnValue());
		if (filtered != null) cir.setReturnValue(filtered);
	}

	private static Stream<PoiRecord> filterPoiStream(Stream<PoiRecord> original) {
		UUID scanningVillager = VillagerPoiContext.get();
		if (scanningVillager == null) return null;

		ServerLevel world = VillagerPoiContext.getWorld();
		if (world == null) return null;

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return null;

		return original.filter(poi -> {
			BlockPos poiPos = poi.getPos();
			UUID blockOwner = data.getVillagerForBlock(poiPos);
			if (blockOwner != null && !blockOwner.equals(scanningVillager)) return false;
			UUID bedOwner = data.getBedVillager(poiPos);
			if (bedOwner != null && !bedOwner.equals(scanningVillager)) return false;
			return true;
		});
	}
}