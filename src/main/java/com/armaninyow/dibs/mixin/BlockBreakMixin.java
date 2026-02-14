package com.armaninyow.dibs.mixin;

import com.armaninyow.dibs.data.VillagerBindingData;
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

		// Check if this is a workstation block
		if (!WorkstationHelper.isWorkstationBlock(state.getBlock())) {
			return;
		}

		// Remove binding if it exists
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data != null) {
			data.unbindBlock(pos);
		}
	}
}