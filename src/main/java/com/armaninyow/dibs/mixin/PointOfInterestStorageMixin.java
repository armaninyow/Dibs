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

	/**
	 * Filter POI scan results so that a villager never "sees" a BlockPos that
	 * is already bound to a different villager.  Called for both job-site
	 * searches (workstations) and bed searches.
	 *
	 * Context is set by VillagerJobSiteMixin at the start of VillagerEntity.tick()
	 * and cleared at the end, so this filter is only active while a specific
	 * villager's brain is ticking.
	 */
	@Inject(
		method = "getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true
	)
	private void filterBoundPoisSquare(
			Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
			BlockPos pos,
			int radius,
			PointOfInterestStorage.OccupationStatus occupationStatus,
			CallbackInfoReturnable<Stream<PointOfInterest>> cir) {

		cir.setReturnValue(filterPoiStream(cir.getReturnValue()));
	}

	@Inject(
		method = "getInCircle(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;",
		at = @At("RETURN"),
		cancellable = true
	)
	private void filterBoundPoisCircle(
			Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
			BlockPos pos,
			int radius,
			PointOfInterestStorage.OccupationStatus occupationStatus,
			CallbackInfoReturnable<Stream<PointOfInterest>> cir) {

		cir.setReturnValue(filterPoiStream(cir.getReturnValue()));
	}

	private static Stream<PointOfInterest> filterPoiStream(Stream<PointOfInterest> original) {
		UUID scanningVillager = VillagerPoiContext.get();
		if (scanningVillager == null) {
			return original;
		}

		ServerWorld world = VillagerPoiContext.getWorld();
		if (world == null) {
			return original;
		}

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			return original;
		}

		return original.filter(poi -> {
			BlockPos poiPos = poi.getPos();

			// Block if this workstation is bound to a different villager
			UUID blockOwner = data.getVillagerForBlock(poiPos);
			if (blockOwner != null && !blockOwner.equals(scanningVillager)) {
				return false;
			}

			// Block if this bed is bound to a different villager
			UUID bedOwner = data.getBedVillager(poiPos);
			if (bedOwner != null && !bedOwner.equals(scanningVillager)) {
				return false;
			}

			return true;
		});
	}
}