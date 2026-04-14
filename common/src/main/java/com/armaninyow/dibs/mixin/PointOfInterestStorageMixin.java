package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.VillagerPoiContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PointOfInterestStorage.class)
public class PointOfInterestStorageMixin {

	@Inject(
		method = "getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true,
		require = 0
	)
	private void filterBoundPoisSquare(
			Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
			BlockPos pos,
			int radius,
			PointOfInterestStorage.OccupationStatus occupationStatus,
			CallbackInfoReturnable<Stream<PointOfInterest>> cir) {
		Stream<PointOfInterest> filtered = filterPoiStream(cir.getReturnValue());
		if (filtered != null) cir.setReturnValue(filtered);
	}

	@Inject(
		method = "getInCircle(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true,
		require = 0
	)
	private void filterBoundPoisCircle(
			Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
			BlockPos pos,
			int radius,
			PointOfInterestStorage.OccupationStatus occupationStatus,
			CallbackInfoReturnable<Stream<PointOfInterest>> cir) {
		Stream<PointOfInterest> filtered = filterPoiStream(cir.getReturnValue());
		if (filtered != null) cir.setReturnValue(filtered);
	}

	private static Stream<PointOfInterest> filterPoiStream(Stream<PointOfInterest> original) {
		UUID scanningVillager = VillagerPoiContext.get();
		if (scanningVillager == null) return null;

		ServerWorld world = VillagerPoiContext.getWorld();
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
