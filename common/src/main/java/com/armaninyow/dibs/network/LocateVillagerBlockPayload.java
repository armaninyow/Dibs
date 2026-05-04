package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record LocateVillagerBlockPayload(UUID villagerUuid) implements CustomPayload {
	public static final CustomPayload.Id<LocateVillagerBlockPayload> ID =
			new CustomPayload.Id<>(Dibs.id("locate_villager_block"));

	public static final PacketCodec<RegistryByteBuf, LocateVillagerBlockPayload> CODEC =
			PacketCodec.of(
					(value, buf) -> {
						buf.writeLong(value.villagerUuid().getMostSignificantBits());
						buf.writeLong(value.villagerUuid().getLeastSignificantBits());
					},
					buf -> new LocateVillagerBlockPayload(new UUID(buf.readLong(), buf.readLong()))
			);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}