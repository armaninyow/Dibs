package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.config.DibsConfig;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.mixin.EntityAccessor;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class NetworkHandler {

	public record WrongClaimantPayload(UUID villagerUuid, boolean isWrong) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<WrongClaimantPayload> ID =
				new CustomPacketPayload.Type<>(Dibs.id("wrong_claimant"));

		public static final StreamCodec<RegistryFriendlyByteBuf, WrongClaimantPayload> CODEC =
				StreamCodec.composite(
						UUIDUtil.STREAM_CODEC, WrongClaimantPayload::villagerUuid,
						ByteBufCodecs.BOOL, WrongClaimantPayload::isWrong,
						WrongClaimantPayload::new
				);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }
	}

	public static void sendWrongClaimant(ServerLevel world, UUID villagerUuid, boolean isWrong) {
		WrongClaimantPayload payload = new WrongClaimantPayload(villagerUuid, isWrong);
		for (ServerPlayer player : world.players()) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void registerServerReceivers() {
		PayloadTypeRegistry.serverboundPlay().register(LocateOwnerPayload.ID, LocateOwnerPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(LocateVillagerBlockPayload.ID, LocateVillagerBlockPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(WrongClaimantPayload.ID, WrongClaimantPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(VillagerBlockResponsePayload.ID, VillagerBlockResponsePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(LocateOwnerPayload.ID, (payload, context) -> {
			ServerPlayer player = context.player();
			Level playerWorld = ((EntityAccessor) player).getDibsWorld();
			ServerLevel world = (ServerLevel) playerWorld;
			BlockPos blockPos = payload.blockPos();
			context.server().execute(() -> handleLocateOwner(player, world, blockPos));
		});

		ServerPlayNetworking.registerGlobalReceiver(LocateVillagerBlockPayload.ID, (payload, context) -> {
			ServerPlayer player = context.player();
			Level playerWorld = ((EntityAccessor) player).getDibsWorld();
			ServerLevel world = (ServerLevel) playerWorld;
			UUID villagerUuid = payload.villagerUuid();
			context.server().execute(() -> handleLocateVillagerBlock(player, world, villagerUuid));
		});
	}

	private static void handleLocateOwner(ServerPlayer player, ServerLevel world, BlockPos blockPos) {
		Dibs.LOGGER.info("Locate owner request at {}", blockPos);
		BlockState state = world.getBlockState(blockPos);
		Block block = state.getBlock();

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) { Dibs.LOGGER.info("No binding data found"); return; }

		UUID villagerUuid = null;
		if (WorkstationHelper.isWorkstationBlock(block)) {
			villagerUuid = data.getVillagerForBlock(blockPos);
		} else if (BedHelper.isBedBlock(block)) {
			BlockPos headPos = BedHelper.isBedHead(state) ? blockPos : BedHelper.findHeadPos(world, blockPos);
			villagerUuid = data.getBedVillager(headPos);
		} else {
			Dibs.LOGGER.info("Block is not a workstation or bed"); return;
		}

		if (villagerUuid == null) { Dibs.LOGGER.info("No villager bound to this block"); return; }

		Entity entity = world.getEntity(villagerUuid);
		if (!(entity instanceof Villager villager)) { Dibs.LOGGER.info("Villager entity not found"); return; }

		int durationTicks = DibsConfig.INSTANCE.glowDurationSeconds * 20;
		villager.addEffect(new MobEffectInstance(MobEffects.GLOWING, durationTicks, 0, false, false, true));
		Dibs.LOGGER.info("Applied glowing effect to villager {} for player {}", villagerUuid, player.getName().getString());
	}

	private static void handleLocateVillagerBlock(ServerPlayer player, ServerLevel world, UUID villagerUuid) {
		Dibs.LOGGER.info("Locate villager block request for villager {}", villagerUuid);

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) { Dibs.LOGGER.info("No binding data found"); return; }

		BlockPos workstationPos = data.getBlockForVillager(villagerUuid);
		BlockPos bedPos = data.getBedForVillager(villagerUuid);

		if (workstationPos == null && bedPos == null) {
			Dibs.LOGGER.info("No blocks bound to villager {}", villagerUuid);
			return;
		}

		ServerPlayNetworking.send(player, new VillagerBlockResponsePayload(villagerUuid, workstationPos, bedPos));
		Dibs.LOGGER.info("Sent block response to player {} for villager {}", player.getName().getString(), villagerUuid);
	}
}