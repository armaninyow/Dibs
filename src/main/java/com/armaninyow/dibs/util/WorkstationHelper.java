package com.armaninyow.dibs.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class WorkstationHelper {
	private static final Set<Block> WORKSTATION_BLOCKS = new HashSet<>();

	static {
		// Add all vanilla workstation blocks
		WORKSTATION_BLOCKS.add(Blocks.BLAST_FURNACE);
		WORKSTATION_BLOCKS.add(Blocks.SMOKER);
		WORKSTATION_BLOCKS.add(Blocks.BREWING_STAND);
		WORKSTATION_BLOCKS.add(Blocks.CARTOGRAPHY_TABLE);
		WORKSTATION_BLOCKS.add(Blocks.CAULDRON);
		WORKSTATION_BLOCKS.add(Blocks.WATER_CAULDRON);
		WORKSTATION_BLOCKS.add(Blocks.LAVA_CAULDRON);
		WORKSTATION_BLOCKS.add(Blocks.POWDER_SNOW_CAULDRON);
		WORKSTATION_BLOCKS.add(Blocks.COMPOSTER);
		WORKSTATION_BLOCKS.add(Blocks.BARREL);
		WORKSTATION_BLOCKS.add(Blocks.FLETCHING_TABLE);
		WORKSTATION_BLOCKS.add(Blocks.GRINDSTONE);
		WORKSTATION_BLOCKS.add(Blocks.LECTERN);
		WORKSTATION_BLOCKS.add(Blocks.LOOM);
		WORKSTATION_BLOCKS.add(Blocks.SMITHING_TABLE);
		WORKSTATION_BLOCKS.add(Blocks.STONECUTTER);
	}

	public static boolean isWorkstationBlock(Block block) {
		return WORKSTATION_BLOCKS.contains(block);
	}

	public static boolean isWorkstationItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		
		Item item = stack.getItem();
		Block block = Block.getBlockFromItem(item);
		return isWorkstationBlock(block);
	}

	public static Block getBlockFromItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		return Block.getBlockFromItem(stack.getItem());
	}
}