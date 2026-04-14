package com.armaninyow.dibs.util;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class BedHelper {

	public static boolean isBedItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		if (!(item instanceof BlockItem blockItem)) {
			return false;
		}
		return blockItem.getBlock() instanceof BedBlock;
	}

	public static boolean isBedBlock(Block block) {
		return block instanceof BedBlock;
	}

	/**
	 * Beds are two blocks; villagers remember the HEAD (pillow) position.
	 * Returns true if this blockstate is the HEAD part of a bed.
	 */
	public static boolean isBedHead(BlockState state) {
		if (!(state.getBlock() instanceof BedBlock)) {
			return false;
		}
		return state.get(Properties.BED_PART) == BedPart.HEAD;
	}

	/**
	 * When a bed item is placed, the context blockpos is the HEAD position
	 * (where the player is looking/placing). However, placement can result in
	 * either part being at that position. We need to find the HEAD block after
	 * placement to record the correct binding position.
	 *
	 * Given the position the player placed the bed item at, find the HEAD
	 * position in the world (searches the placed pos and its neighbors).
	 */
	public static BlockPos findHeadPos(WorldView world, BlockPos placedAt) {
		BlockState state = world.getBlockState(placedAt);
		if (isBedHead(state)) {
			return placedAt;
		}
		// Check the four horizontal neighbors
		for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
			BlockPos neighbor = placedAt.offset(dir);
			BlockState neighborState = world.getBlockState(neighbor);
			if (isBedHead(neighborState)) {
				return neighbor;
			}
		}
		// Fallback — return the placed position even if we can't confirm
		return placedAt;
	}
}