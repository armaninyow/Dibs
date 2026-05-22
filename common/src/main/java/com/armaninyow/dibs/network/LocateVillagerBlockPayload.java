package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record LocateVillagerBlockPayload(UUID villagerUuid) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<LocateVillagerBlockPayload> ID =
			new CustomPacketPayload.Type<>(Dibs.id("locate_villager_block"));

	public static final StreamCodec<RegistryFriendlyByteBuf, LocateVillagerBlockPayload> CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC, LocateVillagerBlockPayload::villagerUuid,
					LocateVillagerBlockPayload::new
			);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}