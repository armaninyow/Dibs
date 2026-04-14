package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record LocateOwnerPayload(BlockPos blockPos) implements CustomPayload {
	public static final CustomPayload.Id<LocateOwnerPayload> ID = 
			new CustomPayload.Id<>(Dibs.id("locate_owner"));
	
	public static final PacketCodec<RegistryByteBuf, LocateOwnerPayload> CODEC = 
			PacketCodec.tuple(
					BlockPos.PACKET_CODEC, LocateOwnerPayload::blockPos,
					LocateOwnerPayload::new
			);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}