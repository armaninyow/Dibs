package com.armaninyow.dibs.event;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.config.DibsConfig;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.ItemNbtHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlayerInteractionHandler {

	// Called from VillagerInteractionMixin (which cancels vanilla first, then calls this)
	// Also registered as UseEntityCallback as fallback
	public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (world.isClient()) {
			return ActionResult.PASS;
		}

		if (!(entity instanceof VillagerEntity villager)) {
			return ActionResult.PASS;
		}

		ItemStack heldItem = player.getStackInHand(hand);

		if (!WorkstationHelper.isWorkstationItem(heldItem)) {
			return ActionResult.PASS;
		}

		boolean isUnemployed = villager.getVillagerData().profession().getKey()
				.map(k -> k.getValue().getPath().equals("none"))
				.orElse(false);

		if (isUnemployed) {
			Dibs.LOGGER.info("Starting binding ritual for villager {}", villager.getUuid());
			performBinding(player, world, hand, villager, heldItem);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private static void performBinding(PlayerEntity player, World world, Hand hand, VillagerEntity villager, ItemStack heldItem) {
		ServerWorld serverWorld = (ServerWorld) world;
		VillagerBindingData data = VillagerBindingData.get(world);

		if (data == null) {
			Dibs.LOGGER.error("Failed to get VillagerBindingData");
			return;
		}

		// Create a fresh single item (no copied components) and tag it
		ItemStack boundItem = new ItemStack(heldItem.getItem(), 1);
		ItemNbtHelper.tagItemWithVillager(boundItem, villager);

		// Remove one from the stack in hand
		heldItem.decrement(1);
		if (heldItem.isEmpty()) {
			player.setStackInHand(hand, ItemStack.EMPTY);
		}

		// Return the tagged item to the player's inventory (or drop it)
		if (!player.getInventory().insertStack(boundItem)) {
			player.dropItem(boundItem, false);
		}

		// Clear any previous binding for this villager
		data.unbindVillager(villager.getUuid());

		// Spawn happy particles for configured duration
		int durationTicks = DibsConfig.INSTANCE.particleDurationSeconds * 20;
		spawnHappyParticles(serverWorld, villager, durationTicks);

		// Play yes sound
		serverWorld.playSound(
				null,
				villager.getBlockPos(),
				SoundEvents.ENTITY_VILLAGER_YES,
				SoundCategory.NEUTRAL,
				1.0F,
				1.0F
		);

		Dibs.LOGGER.info("Bound villager {} to workstation item", villager.getUuid());
	}

	private static void spawnHappyParticles(ServerWorld world, VillagerEntity villager, int durationTicks) {
		// Spawn every 4 ticks over the duration using daemon timers
		for (int i = 0; i < durationTicks; i += 4) {
			final long delayMs = i * 50L; // ticks to milliseconds
			new java.util.Timer(true).schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					if (villager.isAlive() && !villager.isRemoved()) {
						world.spawnParticles(
								ParticleTypes.HAPPY_VILLAGER,
								villager.getX(),
								villager.getY() + 1.0,
								villager.getZ(),
								5,
								0.5, 0.5, 0.5,
								0.0
						);
					}
				}
			}, delayMs);
		}
	}
}