package com.armaninyow.dibs.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 1.21.9_1.21.11
public class VillagerBindingData extends PersistentState {
	private static final String DATA_NAME = "dibs_bindings";

	// Workstation maps: Villager UUID <-> Bound workstation BlockPos
	private final Map<UUID, BlockPos> villagerToBlock = new HashMap<>();
	private final Map<BlockPos, UUID> blockToVillager = new HashMap<>();

	// Bed maps: Villager UUID <-> Bound bed (HEAD) BlockPos
	private final Map<UUID, BlockPos> villagerToBed = new HashMap<>();
	private final Map<BlockPos, UUID> bedToVillager = new HashMap<>();

	public VillagerBindingData() {
		super();
	}

	// -------------------------------------------------------------------------
	// Codec
	// -------------------------------------------------------------------------

	// A single binding entry: UUID <-> BlockPos
	private record BindingEntry(UUID uuid, BlockPos pos) {
		static final Codec<BindingEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Uuids.STRING_CODEC.fieldOf("uuid").forGetter(BindingEntry::uuid),
				BlockPos.CODEC.fieldOf("pos").forGetter(BindingEntry::pos)
		).apply(instance, BindingEntry::new));
	}

	// Full codec for VillagerBindingData
	static final Codec<VillagerBindingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BindingEntry.CODEC.listOf().fieldOf("workstations").forGetter(data ->
					data.villagerToBlock.entrySet().stream()
							.map(e -> new BindingEntry(e.getKey(), e.getValue()))
							.toList()
			),
			BindingEntry.CODEC.listOf().fieldOf("beds").forGetter(data ->
					data.villagerToBed.entrySet().stream()
							.map(e -> new BindingEntry(e.getKey(), e.getValue()))
							.toList()
			)
	).apply(instance, (workstations, beds) -> {
		VillagerBindingData d = new VillagerBindingData();
		for (BindingEntry e : workstations) {
			d.villagerToBlock.put(e.uuid(), e.pos());
			d.blockToVillager.put(e.pos(), e.uuid());
		}
		for (BindingEntry e : beds) {
			d.villagerToBed.put(e.uuid(), e.pos());
			d.bedToVillager.put(e.pos(), e.uuid());
		}
		return d;
	}));

	public static VillagerBindingData get(World world) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return null;
		}

		PersistentStateType<VillagerBindingData> type = new PersistentStateType<>(
				DATA_NAME,
				VillagerBindingData::new,
				VillagerBindingData.CODEC,
				null
		);

		return serverWorld.getPersistentStateManager().getOrCreate(type);
	}

	// -------------------------------------------------------------------------
	// Workstation binding
	// -------------------------------------------------------------------------

	public void bindVillagerToBlock(UUID villagerUuid, BlockPos blockPos) {
		// Remove old binding if exists
		if (villagerToBlock.containsKey(villagerUuid)) {
			BlockPos oldPos = villagerToBlock.get(villagerUuid);
			blockToVillager.remove(oldPos);
		}

		// Create new binding
		villagerToBlock.put(villagerUuid, blockPos);
		blockToVillager.put(blockPos, villagerUuid);
		markDirty();
	}

	public void unbindVillager(UUID villagerUuid) {
		if (villagerToBlock.containsKey(villagerUuid)) {
			BlockPos pos = villagerToBlock.remove(villagerUuid);
			blockToVillager.remove(pos);
			markDirty();
		}
	}

	public void unbindBlock(BlockPos blockPos) {
		if (blockToVillager.containsKey(blockPos)) {
			UUID villagerUuid = blockToVillager.remove(blockPos);
			villagerToBlock.remove(villagerUuid);
			markDirty();
		}
	}

	public UUID getVillagerForBlock(BlockPos blockPos) {
		return blockToVillager.get(blockPos);
	}

	public BlockPos getBlockForVillager(UUID villagerUuid) {
		return villagerToBlock.get(villagerUuid);
	}

	public boolean isBlockBound(BlockPos blockPos) {
		return blockToVillager.containsKey(blockPos);
	}

	public boolean isVillagerBound(UUID villagerUuid) {
		return villagerToBlock.containsKey(villagerUuid);
	}

	// -------------------------------------------------------------------------
	// Bed binding
	// -------------------------------------------------------------------------

	public void bindVillagerToBed(UUID villagerUuid, BlockPos headPos) {
		// Remove old bed binding if exists
		if (villagerToBed.containsKey(villagerUuid)) {
			BlockPos oldPos = villagerToBed.get(villagerUuid);
			bedToVillager.remove(oldPos);
		}

		// Create new binding
		villagerToBed.put(villagerUuid, headPos);
		bedToVillager.put(headPos, villagerUuid);
		markDirty();
	}

	public void unbindBed(UUID villagerUuid) {
		if (villagerToBed.containsKey(villagerUuid)) {
			BlockPos pos = villagerToBed.remove(villagerUuid);
			bedToVillager.remove(pos);
			markDirty();
		}
	}

	public void unbindBedBlock(BlockPos headPos) {
		if (bedToVillager.containsKey(headPos)) {
			UUID villagerUuid = bedToVillager.remove(headPos);
			villagerToBed.remove(villagerUuid);
			markDirty();
		}
	}

	public UUID getBedVillager(BlockPos headPos) {
		return bedToVillager.get(headPos);
	}

	public BlockPos getBedForVillager(UUID villagerUuid) {
		return villagerToBed.get(villagerUuid);
	}

	public boolean isBedBound(BlockPos headPos) {
		return bedToVillager.containsKey(headPos);
	}

	public boolean isVillagerBedBound(UUID villagerUuid) {
		return villagerToBed.containsKey(villagerUuid);
	}
}