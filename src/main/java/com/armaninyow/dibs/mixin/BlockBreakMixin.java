package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockBreakMixin {

	@Inject(method = "onBreak", at = @At("HEAD"))
	private void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
		if (world.isClient()) {
			return;
		}

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			return;
		}

		// Handle workstation block break
		if (WorkstationHelper.isWorkstationBlock(state.getBlock())) {
			data.unbindBlock(pos);
			return;
		}

		// Handle bed block break — check both HEAD and FOOT positions
		// The binding is stored under the HEAD pos; when breaking the FOOT,
		// we need to find and clear the binding under the HEAD.
		if (BedHelper.isBedBlock(state.getBlock())) {
			if (BedHelper.isBedHead(state)) {
				// Breaking the head directly
				data.unbindBedBlock(pos);
			} else {
				// Breaking the foot — find the head neighbor and unbind it
				BlockPos headPos = BedHelper.findHeadPos(world, pos);
				data.unbindBedBlock(headPos);
			}
		}
	}
}