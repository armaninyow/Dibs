package com.armaninyow.dibs.util;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;

public class BedHelper {

	public static boolean isBedItem(ItemStack stack) {
		if (stack.isEmpty()) return false;
		Item item = stack.getItem();
		if (!(item instanceof BlockItem blockItem)) return false;
		return blockItem.getBlock() instanceof BedBlock;
	}

	public static boolean isBedBlock(Block block) {
		return block instanceof BedBlock;
	}

	public static boolean isBedHead(BlockState state) {
		if (!(state.getBlock() instanceof BedBlock)) return false;
		return state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD;
	}

	public static BlockPos findHeadPos(LevelReader world, BlockPos placedAt) {
		BlockState state = world.getBlockState(placedAt);
		if (isBedHead(state)) return placedAt;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos neighbor = placedAt.relative(dir);
			BlockState neighborState = world.getBlockState(neighbor);
			if (isBedHead(neighborState)) return neighbor;
		}
		return placedAt;
	}
}