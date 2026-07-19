package com.armaninyow.dibs.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorkstationHelper {
	private static final Set<Block> WORKSTATION_BLOCKS = new HashSet<>();

	private static final Map<String, Block> PROFESSION_TO_BLOCK = new HashMap<>();

	static {
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

		PROFESSION_TO_BLOCK.put("armorer",      Blocks.BLAST_FURNACE);
		PROFESSION_TO_BLOCK.put("butcher",       Blocks.SMOKER);
		PROFESSION_TO_BLOCK.put("cleric",        Blocks.BREWING_STAND);
		PROFESSION_TO_BLOCK.put("cartographer",  Blocks.CARTOGRAPHY_TABLE);
		PROFESSION_TO_BLOCK.put("leatherworker", Blocks.CAULDRON);
		PROFESSION_TO_BLOCK.put("farmer",        Blocks.COMPOSTER);
		PROFESSION_TO_BLOCK.put("fisherman",     Blocks.BARREL);
		PROFESSION_TO_BLOCK.put("fletcher",      Blocks.FLETCHING_TABLE);
		PROFESSION_TO_BLOCK.put("weaponsmith",   Blocks.GRINDSTONE);
		PROFESSION_TO_BLOCK.put("librarian",     Blocks.LECTERN);
		PROFESSION_TO_BLOCK.put("shepherd",      Blocks.LOOM);
		PROFESSION_TO_BLOCK.put("toolsmith",     Blocks.SMITHING_TABLE);
		PROFESSION_TO_BLOCK.put("mason",         Blocks.STONECUTTER);
	}

	public static boolean isWorkstationBlock(Block block) {
		return WORKSTATION_BLOCKS.contains(block);
	}

	public static boolean isWorkstationItem(ItemStack stack) {
		if (stack.isEmpty()) return false;
		Block block = Block.byItem(stack.getItem());
		return isWorkstationBlock(block);
	}

	public static Block getBlockFromItem(ItemStack stack) {
		if (stack.isEmpty()) return null;
		return Block.byItem(stack.getItem());
	}

	public static boolean isMatchingWorkstation(Block block, String professionPath) {
		if (professionPath == null || professionPath.equals("none")) return false;
		if ("leatherworker".equals(professionPath)) {
			return block == Blocks.CAULDRON
					|| block == Blocks.WATER_CAULDRON
					|| block == Blocks.LAVA_CAULDRON
					|| block == Blocks.POWDER_SNOW_CAULDRON;
		}
		Block expected = PROFESSION_TO_BLOCK.get(professionPath);
		return expected != null && expected == block;
	}
}