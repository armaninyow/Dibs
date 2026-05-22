package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.event.PlayerInteractionHandler;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerInteractionMixin {

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void onVillagerInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		Villager villager = (Villager) (Object) this;
		ItemStack heldItem = player.getItemInHand(hand);

		boolean isWorkstation = WorkstationHelper.isWorkstationItem(heldItem);
		boolean isBed = BedHelper.isBedItem(heldItem);
		if (!isWorkstation && !isBed) return;

		String professionPath = villager.getVillagerData().profession().unwrapKey()
				.map(k -> k.identifier().getPath()).orElse("none");
		boolean isUnemployed = professionPath.equals("none");

		boolean matchesEmployedProfession = false;
		if (!isUnemployed && isWorkstation) {
			Block heldBlock = WorkstationHelper.getBlockFromItem(heldItem);
			matchesEmployedProfession = heldBlock != null
					&& WorkstationHelper.isMatchingWorkstation(heldBlock, professionPath);
		}

		boolean shouldIntercept = isBed || isUnemployed || matchesEmployedProfession;

		if (shouldIntercept) {
			InteractionResult result = PlayerInteractionHandler.onUseEntity(
					player, ((EntityAccessor) (Object) this).getDibsWorld(), hand, villager, null);
			cir.setReturnValue(result);
		}
	}
}