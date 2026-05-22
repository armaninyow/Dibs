package com.armaninyow.dibs.event;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.config.DibsConfig;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.ItemNbtHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class PlayerInteractionHandler {

	public static InteractionResult onUseEntity(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (world.isClientSide()) return InteractionResult.PASS;
		if (!(entity instanceof Villager villager)) return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);

		if (BedHelper.isBedItem(heldItem)) {
			Dibs.LOGGER.info("Starting bed binding ritual for villager {}", villager.getUUID());
			performBinding(player, world, hand, villager, heldItem, true);
			return InteractionResult.SUCCESS;
		}

		if (!WorkstationHelper.isWorkstationItem(heldItem)) return InteractionResult.PASS;

		String professionPath = villager.getVillagerData().profession().unwrapKey()
				.map(k -> k.identifier().getPath()).orElse("none");
		boolean isUnemployed = professionPath.equals("none");

		if (isUnemployed) {
			Dibs.LOGGER.info("Starting workstation binding ritual for villager {}", villager.getUUID());
			performBinding(player, world, hand, villager, heldItem, false);
			return InteractionResult.SUCCESS;
		}

		Block heldBlock = WorkstationHelper.getBlockFromItem(heldItem);
		if (heldBlock != null && WorkstationHelper.isMatchingWorkstation(heldBlock, professionPath)) {
			Dibs.LOGGER.info("Starting workstation binding ritual for employed villager {}", villager.getUUID());
			performBinding(player, world, hand, villager, heldItem, false);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private static void performBinding(Player player, Level world, InteractionHand hand, Villager villager, ItemStack heldItem, boolean isBed) {
		ServerLevel serverLevel = (ServerLevel) world;
		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) { Dibs.LOGGER.error("Failed to get VillagerBindingData"); return; }

		ItemStack boundItem = new ItemStack(heldItem.getItem(), 1);
		ItemNbtHelper.tagItemWithVillager(boundItem, villager);

		heldItem.shrink(1);
		if (heldItem.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
		if (!player.getInventory().add(boundItem)) player.drop(boundItem, false);

		if (isBed) data.unbindBed(villager.getUUID());
		else data.unbindVillager(villager.getUUID());

		int durationTicks = DibsConfig.INSTANCE.particleDurationSeconds * 20;
		spawnHappyParticles(serverLevel, villager, durationTicks);
		serverLevel.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
		Dibs.LOGGER.info("Bound villager {} to {} item", villager.getUUID(), isBed ? "bed" : "workstation");
	}

	private static void spawnHappyParticles(ServerLevel world, Villager villager, int durationTicks) {
		for (int i = 0; i < durationTicks; i += 4) {
			final long delayMs = i * 50L;
			new java.util.Timer(true).schedule(new java.util.TimerTask() {
				@Override public void run() {
					if (villager.isAlive() && !villager.isRemoved()) {
						world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
								villager.getX(), villager.getY() + 1.0, villager.getZ(),
								5, 0.5, 0.5, 0.5, 0.0);
					}
				}
			}, delayMs);
		}
	}
}