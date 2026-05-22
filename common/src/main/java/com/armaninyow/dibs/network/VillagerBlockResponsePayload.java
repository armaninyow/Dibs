package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record VillagerBlockResponsePayload(
		UUID villagerUuid,
		@Nullable BlockPos workstationPos,
		@Nullable BlockPos bedPos
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<VillagerBlockResponsePayload> ID =
			new CustomPacketPayload.Type<>(Dibs.id("villager_block_response"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Optional<BlockPos>> OPTIONAL_BLOCK_POS =
			ByteBufCodecs.optional(BlockPos.STREAM_CODEC);

	public static final StreamCodec<RegistryFriendlyByteBuf, VillagerBlockResponsePayload> CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC, VillagerBlockResponsePayload::villagerUuid,
					OPTIONAL_BLOCK_POS, p -> Optional.ofNullable(p.workstationPos()),
					OPTIONAL_BLOCK_POS, p -> Optional.ofNullable(p.bedPos()),
					(uuid, ws, bed) -> new VillagerBlockResponsePayload(uuid, ws.orElse(null), bed.orElse(null))
			);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}