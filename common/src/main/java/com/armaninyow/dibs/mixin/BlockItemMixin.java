package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.ItemNbtHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BlockItem.class)
public class BlockItemMixin {

	private UUID dibs_pendingVillagerUuid = null;
	private BlockPos dibs_pendingBlockPos = null;
	private boolean dibs_pendingIsBed = false;

	@Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
			at = @At("HEAD"))
	private void captureUuidBeforePlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = context.getItemInHand();
		if (!WorkstationHelper.isWorkstationItem(stack) && !BedHelper.isBedItem(stack)) return;
		dibs_pendingVillagerUuid = ItemNbtHelper.getVillagerUuid(stack);
		dibs_pendingBlockPos = context.getClickedPos();
		dibs_pendingIsBed = BedHelper.isBedItem(stack);
	}

	@Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
			at = @At("RETURN"))
	private void onBlockPlaced(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level world = context.getLevel();
		if (world.isClientSide()) {
			dibs_pendingVillagerUuid = null;
			dibs_pendingBlockPos = null;
			dibs_pendingIsBed = false;
			return;
		}

		UUID uuid = dibs_pendingVillagerUuid;
		BlockPos pos = dibs_pendingBlockPos;
		boolean isBed = dibs_pendingIsBed;
		dibs_pendingVillagerUuid = null;
		dibs_pendingBlockPos = null;
		dibs_pendingIsBed = false;

		if (uuid == null || pos == null) return;
		if (cir.getReturnValue() == InteractionResult.FAIL) return;

		ServerLevel serverLevel = (ServerLevel) world;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		if (isBed) {
			BlockPos headPos = BedHelper.findHeadPos(serverLevel, pos);
			data.bindVillagerToBed(uuid, headPos);
			Dibs.LOGGER.info("Bound bed (head) at {} to villager {}", headPos, uuid);

			Villager villager = (Villager) serverLevel.getEntity(uuid);
			if (villager != null) {
				GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), headPos);
				villager.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
				Dibs.LOGGER.info("Set HOME memory for villager {}", uuid);
			}
		} else {
			data.bindVillagerToBlock(uuid, pos);
			Dibs.LOGGER.info("Bound block at {} to villager {}", pos, uuid);

			Villager villager = (Villager) serverLevel.getEntity(uuid);
			if (villager != null) {
				GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), pos);
				villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, globalPos);
				Dibs.LOGGER.info("Set job site memory for villager {}", uuid);
			}
		}
	}
}