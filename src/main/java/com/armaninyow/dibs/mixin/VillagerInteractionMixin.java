package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.event.PlayerInteractionHandler;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

	@Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
	private void onVillagerInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		ItemStack heldItem = player.getStackInHand(hand);

		boolean isWorkstation = WorkstationHelper.isWorkstationItem(heldItem);
		boolean isBed = BedHelper.isBedItem(heldItem);

		// Only intercept when holding a workstation or bed item
		if (!isWorkstation && !isBed) {
			return;
		}

		boolean isUnemployed = villager.getVillagerData().profession().getKey()
				.map(k -> k.getValue().getPath().equals("none"))
				.orElse(false);

		// Bed binding works on any villager (not just unemployed)
		// Workstation binding only works on unemployed villagers
		boolean shouldIntercept = isBed || isUnemployed;

		if (shouldIntercept) {
			// Cancel vanilla FIRST (prevents head-shake and "no" sound regardless of stack size)
			// Then run our binding logic directly
			ActionResult result = PlayerInteractionHandler.onUseEntity(
					player, ((EntityAccessor) villager).getWorld(), hand, villager, null);
			cir.setReturnValue(result);
		}
	}
}