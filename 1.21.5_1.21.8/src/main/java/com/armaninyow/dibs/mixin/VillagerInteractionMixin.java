package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.event.PlayerInteractionHandler;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 1.21.5_1.21.8
@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

	@Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
	private void onVillagerInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		ItemStack heldItem = player.getStackInHand(hand);

		boolean isWorkstation = WorkstationHelper.isWorkstationItem(heldItem);
		boolean isBed = BedHelper.isBedItem(heldItem);
		if (!isWorkstation && !isBed) return;

		// profession() returns RegistryEntry with getKey()
		String professionPath = villager.getVillagerData().profession().getKey()
				.map(k -> k.getValue().getPath()).orElse("none");
		boolean isUnemployed = professionPath.equals("none");

		// Intercept if: holding a bed, unemployed, or employed with matching workstation item
		boolean matchesEmployedProfession = false;
		if (!isUnemployed && isWorkstation) {
			Block heldBlock = WorkstationHelper.getBlockFromItem(heldItem);
			matchesEmployedProfession = heldBlock != null
					&& WorkstationHelper.isMatchingWorkstation(heldBlock, professionPath);
		}

		boolean shouldIntercept = isBed || isUnemployed || matchesEmployedProfession;

		if (shouldIntercept) {
			ActionResult result = PlayerInteractionHandler.onUseEntity(
					player, ((EntityAccessor) (Object) this).getDibsWorld(), hand, villager, null);
			cir.setReturnValue(result);
		}
	}
}