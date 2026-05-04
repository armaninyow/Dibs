package com.armaninyow.dibs.event;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.config.DibsConfig;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.ItemNbtHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// 1.21_1.21.4
public class PlayerInteractionHandler {

	public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (world.isClient()) return ActionResult.PASS;
		if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

		ItemStack heldItem = player.getStackInHand(hand);

		if (BedHelper.isBedItem(heldItem)) {
			Dibs.LOGGER.info("Starting bed binding ritual for villager {}", villager.getUuid());
			performBinding(player, world, hand, villager, heldItem, true);
			return ActionResult.SUCCESS;
		}

		if (!WorkstationHelper.isWorkstationItem(heldItem)) return ActionResult.PASS;

		Identifier profId = Registries.VILLAGER_PROFESSION.getId(villager.getVillagerData().getProfession());
		boolean isUnemployed = profId == null || profId.getPath().equals("none");

		if (isUnemployed) {
			Dibs.LOGGER.info("Starting workstation binding ritual for villager {}", villager.getUuid());
			performBinding(player, world, hand, villager, heldItem, false);
			return ActionResult.SUCCESS;
		}

		// Allow binding an already-employed villager if the held item matches their profession's job site
		String professionPath = profId.getPath();
		Block heldBlock = WorkstationHelper.getBlockFromItem(heldItem);
		if (heldBlock != null && WorkstationHelper.isMatchingWorkstation(heldBlock, professionPath)) {
			Dibs.LOGGER.info("Starting workstation binding ritual for employed villager {}", villager.getUuid());
			performBinding(player, world, hand, villager, heldItem, false);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private static void performBinding(PlayerEntity player, World world, Hand hand, VillagerEntity villager, ItemStack heldItem, boolean isBed) {
		ServerWorld serverWorld = (ServerWorld) world;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) { Dibs.LOGGER.error("Failed to get VillagerBindingData"); return; }

		ItemStack boundItem = new ItemStack(heldItem.getItem(), 1);
		ItemNbtHelper.tagItemWithVillager(boundItem, villager);

		heldItem.decrement(1);
		if (heldItem.isEmpty()) player.setStackInHand(hand, ItemStack.EMPTY);
		if (!player.getInventory().insertStack(boundItem)) player.dropItem(boundItem, false);

		if (isBed) data.unbindBed(villager.getUuid());
		else data.unbindVillager(villager.getUuid());

		int durationTicks = DibsConfig.INSTANCE.particleDurationSeconds * 20;
		spawnHappyParticles(serverWorld, villager, durationTicks);
		serverWorld.playSound(null, villager.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		Dibs.LOGGER.info("Bound villager {} to {} item", villager.getUuid(), isBed ? "bed" : "workstation");
	}

	private static void spawnHappyParticles(ServerWorld world, VillagerEntity villager, int durationTicks) {
		for (int i = 0; i < durationTicks; i += 4) {
			final long delayMs = i * 50L;
			new java.util.Timer(true).schedule(new java.util.TimerTask() {
				@Override public void run() {
					if (villager.isAlive() && !villager.isRemoved()) {
						world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
					}
				}
			}, delayMs);
		}
	}
}