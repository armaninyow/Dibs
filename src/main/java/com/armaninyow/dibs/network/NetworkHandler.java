package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import com.armaninyow.dibs.config.DibsConfig;
import com.armaninyow.dibs.data.VillagerBindingData;
import com.armaninyow.dibs.mixin.EntityAccessor;
import com.armaninyow.dibs.util.BedHelper;
import com.armaninyow.dibs.util.WorkstationHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class NetworkHandler {

	// Packet to tell clients which villager UUID is a wrong claimant (suppress its particles)
	public record WrongClaimantPayload(UUID villagerUuid, boolean isWrong) implements CustomPayload {
		public static final CustomPayload.Id<WrongClaimantPayload> ID =
				new CustomPayload.Id<>(Dibs.id("wrong_claimant"));
		public static final PacketCodec<RegistryByteBuf, WrongClaimantPayload> CODEC =
				PacketCodec.tuple(
						PacketCodecs.BYTE_ARRAY.xmap(
								bytes -> new UUID(
										((long) bytes[0] << 56) | ((long)(bytes[1] & 0xFF) << 48) |
										((long)(bytes[2] & 0xFF) << 40) | ((long)(bytes[3] & 0xFF) << 32) |
										((long)(bytes[4] & 0xFF) << 24) | ((long)(bytes[5] & 0xFF) << 16) |
										((long)(bytes[6] & 0xFF) << 8) | ((long)(bytes[7] & 0xFF)),
										((long) bytes[8] << 56) | ((long)(bytes[9] & 0xFF) << 48) |
										((long)(bytes[10] & 0xFF) << 40) | ((long)(bytes[11] & 0xFF) << 32) |
										((long)(bytes[12] & 0xFF) << 24) | ((long)(bytes[13] & 0xFF) << 16) |
										((long)(bytes[14] & 0xFF) << 8) | ((long)(bytes[15] & 0xFF))
								),
								uuid -> {
									byte[] bytes = new byte[16];
									long msb = uuid.getMostSignificantBits();
									long lsb = uuid.getLeastSignificantBits();
									for (int i = 0; i < 8; i++) bytes[i] = (byte)(msb >> (56 - 8*i));
									for (int i = 0; i < 8; i++) bytes[8+i] = (byte)(lsb >> (56 - 8*i));
									return bytes;
								}
						),
						WrongClaimantPayload::villagerUuid,
						PacketCodecs.BOOLEAN,
						WrongClaimantPayload::isWrong,
						WrongClaimantPayload::new
				);

		@Override
		public Id<? extends CustomPayload> getId() { return ID; }
	}

	public static void sendWrongClaimant(ServerWorld world, UUID villagerUuid, boolean isWrong) {
		WrongClaimantPayload payload = new WrongClaimantPayload(villagerUuid, isWrong);
		for (ServerPlayerEntity player : PlayerLookup.world(world)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void registerServerReceivers() {
		// Register payload types
		PayloadTypeRegistry.playC2S().register(LocateOwnerPayload.ID, LocateOwnerPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(WrongClaimantPayload.ID, WrongClaimantPayload.CODEC);

		// Register server receiver
		ServerPlayNetworking.registerGlobalReceiver(LocateOwnerPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			World playerWorld = ((EntityAccessor) player).getWorld();
			ServerWorld world = (ServerWorld) playerWorld;
			BlockPos blockPos = payload.blockPos();

			context.server().execute(() -> {
				handleLocateOwner(player, world, blockPos);
			});
		});
	}

	private static void handleLocateOwner(ServerPlayerEntity player, ServerWorld world, BlockPos blockPos) {
		Dibs.LOGGER.info("Locate owner request at {}", blockPos);

		BlockState state = world.getBlockState(blockPos);
		Block block = state.getBlock();

		Dibs.LOGGER.info("Block at pos: {}", block);

		VillagerBindingData data = VillagerBindingData.get(world);
		if (data == null) {
			Dibs.LOGGER.info("No binding data found");
			return;
		}

		UUID villagerUuid = null;

		if (WorkstationHelper.isWorkstationBlock(block)) {
			villagerUuid = data.getVillagerForBlock(blockPos);
		} else if (BedHelper.isBedBlock(block)) {
			// The binding is stored under the HEAD position; handle both parts
			BlockPos headPos = BedHelper.isBedHead(state) ? blockPos : BedHelper.findHeadPos(world, blockPos);
			villagerUuid = data.getBedVillager(headPos);
		} else {
			Dibs.LOGGER.info("Block is not a workstation or bed");
			return;
		}

		Dibs.LOGGER.info("Villager UUID for block: {}", villagerUuid);

		if (villagerUuid == null) {
			Dibs.LOGGER.info("No villager bound to this block");
			return;
		}

		Entity entity = world.getEntity(villagerUuid);
		if (!(entity instanceof VillagerEntity villager)) {
			Dibs.LOGGER.info("Villager entity not found in world");
			return;
		}

		int durationTicks = DibsConfig.INSTANCE.glowDurationSeconds * 20;
		villager.addStatusEffect(new StatusEffectInstance(
				StatusEffects.GLOWING,
				durationTicks,
				0,
				false,
				false,
				true
		));

		Dibs.LOGGER.info("Applied glowing effect to villager {} for player {}",
				villagerUuid, player.getName().getString());
	}
}