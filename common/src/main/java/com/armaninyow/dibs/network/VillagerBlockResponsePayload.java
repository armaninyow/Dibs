package com.armaninyow.dibs.network;

import com.armaninyow.dibs.Dibs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record VillagerBlockResponsePayload(
		UUID villagerUuid,
		@Nullable BlockPos workstationPos,
		@Nullable BlockPos bedPos
) implements CustomPayload {
	public static final CustomPayload.Id<VillagerBlockResponsePayload> ID =
			new CustomPayload.Id<>(Dibs.id("villager_block_response"));

	public static final PacketCodec<RegistryByteBuf, VillagerBlockResponsePayload> CODEC =
			PacketCodec.of(
					(value, buf) -> {
						buf.writeLong(value.villagerUuid().getMostSignificantBits());
						buf.writeLong(value.villagerUuid().getLeastSignificantBits());
						writeNullableBlockPos(buf, value.workstationPos());
						writeNullableBlockPos(buf, value.bedPos());
					},
					buf -> new VillagerBlockResponsePayload(
							new UUID(buf.readLong(), buf.readLong()),
							readNullableBlockPos(buf),
							readNullableBlockPos(buf)
					)
			);

	private static void writeNullableBlockPos(RegistryByteBuf buf, @Nullable BlockPos pos) {
		buf.writeBoolean(pos != null);
		if (pos != null) {
			buf.writeLong(pos.asLong());
		}
	}

	@Nullable
	private static BlockPos readNullableBlockPos(RegistryByteBuf buf) {
		if (buf.readBoolean()) {
			return BlockPos.fromLong(buf.readLong());
		}
		return null;
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}