package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.ItemNbtHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
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

	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
			at = @At("HEAD"))
	private void captureUuidBeforePlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack stack = context.getStack();
		if (!WorkstationHelper.isWorkstationItem(stack) && !BedHelper.isBedItem(stack)) {
			return;
		}
		dibs_pendingVillagerUuid = ItemNbtHelper.getVillagerUuid(stack);
		dibs_pendingBlockPos = context.getBlockPos();
		dibs_pendingIsBed = BedHelper.isBedItem(stack);
	}

	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
			at = @At("RETURN"))
	private void onBlockPlaced(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		if (world.isClient()) {
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

		if (uuid == null || pos == null) {
			return;
		}

		if (cir.getReturnValue() == ActionResult.FAIL) {
			return;
		}

		ServerWorld serverWorld = (ServerWorld) world;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			return;
		}

		if (isBed) {
			// Find the HEAD (pillow) position of the newly placed bed
			BlockPos headPos = BedHelper.findHeadPos(serverWorld, pos);

			// Register the bed binding
			data.bindVillagerToBed(uuid, headPos);
			Dibs.LOGGER.info("Bound bed (head) at {} to villager {}", headPos, uuid);

			// Find the bound villager and force its HOME brain memory
			VillagerEntity villager = (VillagerEntity) serverWorld.getEntity(uuid);
			if (villager != null) {
				GlobalPos globalPos = GlobalPos.create(serverWorld.getRegistryKey(), headPos);
				villager.getBrain().remember(MemoryModuleType.HOME, globalPos);
				Dibs.LOGGER.info("Set HOME memory for villager {}", uuid);
			}
		} else {
			// Register the workstation binding
			data.bindVillagerToBlock(uuid, pos);
			Dibs.LOGGER.info("Bound block at {} to villager {}", pos, uuid);

			// Find the bound villager and force its job site brain memory
			VillagerEntity villager = (VillagerEntity) serverWorld.getEntity(uuid);
			if (villager != null) {
				GlobalPos globalPos = GlobalPos.create(serverWorld.getRegistryKey(), pos);
				villager.getBrain().remember(
						MemoryModuleType.JOB_SITE,
						globalPos
				);
				Dibs.LOGGER.info("Set job site memory for villager {}", uuid);
			}
		}
	}
}