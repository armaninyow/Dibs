package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LocateOwnerPayload(BlockPos blockPos) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<LocateOwnerPayload> ID =
			new CustomPacketPayload.Type<>(Dibs.id("locate_owner"));

	public static final StreamCodec<RegistryFriendlyByteBuf, LocateOwnerPayload> CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC, LocateOwnerPayload::blockPos,
					LocateOwnerPayload::new
			);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}