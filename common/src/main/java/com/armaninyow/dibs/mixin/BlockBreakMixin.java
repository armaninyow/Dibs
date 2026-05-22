package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockBreakMixin {

	@Inject(method = "playerWillDestroy", at = @At("HEAD"))
	private void onBlockBroken(Level world, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
		if (world.isClientSide()) return;

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) return;

		if (WorkstationHelper.isWorkstationBlock(state.getBlock())) {
			data.unbindBlock(pos);
			return;
		}

		if (BedHelper.isBedBlock(state.getBlock())) {
			if (BedHelper.isBedHead(state)) {
				data.unbindBedBlock(pos);
			} else {
				BlockPos headPos = BedHelper.findHeadPos(world, pos);
				data.unbindBedBlock(headPos);
			}
		}
	}
}