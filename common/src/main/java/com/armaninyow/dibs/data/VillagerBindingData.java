package com.armaninyow.dibs.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerBindingData extends SavedData {
	private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath("dibs", "dibs_bindings");

	private final Map<UUID, BlockPos> villagerToBlock = new HashMap<>();
	private final Map<BlockPos, UUID> blockToVillager = new HashMap<>();
	private final Map<UUID, BlockPos> villagerToBed   = new HashMap<>();
	private final Map<BlockPos, UUID> bedToVillager   = new HashMap<>();

	public VillagerBindingData() {}

	private record BindingEntry(UUID uuid, BlockPos pos) {
		static final Codec<BindingEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(BindingEntry::uuid),
				BlockPos.CODEC.fieldOf("pos").forGetter(BindingEntry::pos)
		).apply(instance, BindingEntry::new));
	}

	private static final Codec<VillagerBindingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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

	private static final SavedDataType<VillagerBindingData> TYPE = new SavedDataType<>(
			DATA_ID,
			VillagerBindingData::new,
			VillagerBindingData.CODEC,
			null
	);

	public static VillagerBindingData get(Level world) {
		if (!(world instanceof ServerLevel serverLevel)) return null;
		return serverLevel.getDataStorage().computeIfAbsent(TYPE);
	}

	public void bindVillagerToBlock(UUID villagerUuid, BlockPos blockPos) {
		if (villagerToBlock.containsKey(villagerUuid)) blockToVillager.remove(villagerToBlock.get(villagerUuid));
		villagerToBlock.put(villagerUuid, blockPos);
		blockToVillager.put(blockPos, villagerUuid);
		setDirty();
	}

	public void unbindVillager(UUID villagerUuid) {
		if (villagerToBlock.containsKey(villagerUuid)) {
			blockToVillager.remove(villagerToBlock.remove(villagerUuid));
			setDirty();
		}
	}

	public void unbindBlock(BlockPos blockPos) {
		if (blockToVillager.containsKey(blockPos)) {
			villagerToBlock.remove(blockToVillager.remove(blockPos));
			setDirty();
		}
	}

	public UUID getVillagerForBlock(BlockPos blockPos)     { return blockToVillager.get(blockPos); }
	public BlockPos getBlockForVillager(UUID villagerUuid) { return villagerToBlock.get(villagerUuid); }
	public boolean isBlockBound(BlockPos blockPos)         { return blockToVillager.containsKey(blockPos); }
	public boolean isVillagerBound(UUID villagerUuid)      { return villagerToBlock.containsKey(villagerUuid); }

	public void bindVillagerToBed(UUID villagerUuid, BlockPos headPos) {
		if (villagerToBed.containsKey(villagerUuid)) bedToVillager.remove(villagerToBed.get(villagerUuid));
		villagerToBed.put(villagerUuid, headPos);
		bedToVillager.put(headPos, villagerUuid);
		setDirty();
	}

	public void unbindBed(UUID villagerUuid) {
		if (villagerToBed.containsKey(villagerUuid)) {
			bedToVillager.remove(villagerToBed.remove(villagerUuid));
			setDirty();
		}
	}

	public void unbindBedBlock(BlockPos headPos) {
		if (bedToVillager.containsKey(headPos)) {
			villagerToBed.remove(bedToVillager.remove(headPos));
			setDirty();
		}
	}

	public UUID getBedVillager(BlockPos headPos)          { return bedToVillager.get(headPos); }
	public BlockPos getBedForVillager(UUID villagerUuid)  { return villagerToBed.get(villagerUuid); }
	public boolean isBedBound(BlockPos headPos)           { return bedToVillager.containsKey(headPos); }
	public boolean isVillagerBedBound(UUID villagerUuid)  { return villagerToBed.containsKey(villagerUuid); }
}