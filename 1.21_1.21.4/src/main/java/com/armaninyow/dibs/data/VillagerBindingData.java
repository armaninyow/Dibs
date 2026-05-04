package com.armaninyow.dibs.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 1.21_1.21.4
public class VillagerBindingData extends PersistentState {
	private static final String DATA_NAME = "dibs_bindings";

	private final Map<UUID, BlockPos> villagerToBlock = new HashMap<>();
	private final Map<BlockPos, UUID> blockToVillager = new HashMap<>();
	private final Map<UUID, BlockPos> villagerToBed = new HashMap<>();
	private final Map<BlockPos, UUID> bedToVillager = new HashMap<>();

	public VillagerBindingData() { super(); }

	// Codec
	private record BindingEntry(UUID uuid, BlockPos pos) {
		static final Codec<BindingEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Uuids.STRING_CODEC.fieldOf("uuid").forGetter(BindingEntry::uuid),
				BlockPos.CODEC.fieldOf("pos").forGetter(BindingEntry::pos)
		).apply(instance, BindingEntry::new));
	}

	private static final Codec<VillagerBindingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BindingEntry.CODEC.listOf().fieldOf("workstations").forGetter(data ->
					data.villagerToBlock.entrySet().stream().map(e -> new BindingEntry(e.getKey(), e.getValue())).toList()),
			BindingEntry.CODEC.listOf().fieldOf("beds").forGetter(data ->
					data.villagerToBed.entrySet().stream().map(e -> new BindingEntry(e.getKey(), e.getValue())).toList())
	).apply(instance, (workstations, beds) -> {
		VillagerBindingData d = new VillagerBindingData();
		for (BindingEntry e : workstations) { d.villagerToBlock.put(e.uuid(), e.pos()); d.blockToVillager.put(e.pos(), e.uuid()); }
		for (BindingEntry e : beds) { d.villagerToBed.put(e.uuid(), e.pos()); d.bedToVillager.put(e.pos(), e.uuid()); }
		return d;
	}));

	// PersistentState requires writeNbt override
	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this).result().ifPresent(encoded -> {
			if (encoded instanceof NbtCompound c) nbt.copyFrom(c);
		});
		return nbt;
	}

	private static VillagerBindingData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		return CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt).result().orElseGet(VillagerBindingData::new);
	}

	// getOrCreate(Type<T>, String) — use PersistentState.Type
	private static final PersistentState.Type<VillagerBindingData> TYPE = new PersistentState.Type<>(
			VillagerBindingData::new, VillagerBindingData::fromNbt, null);

	public static VillagerBindingData get(World world) {
		if (!(world instanceof ServerWorld serverWorld)) return null;
		return serverWorld.getPersistentStateManager().getOrCreate(TYPE, DATA_NAME);
	}

	// Workstation binding
	public void bindVillagerToBlock(UUID uuid, BlockPos pos) {
		if (villagerToBlock.containsKey(uuid)) blockToVillager.remove(villagerToBlock.get(uuid));
		villagerToBlock.put(uuid, pos); blockToVillager.put(pos, uuid); markDirty();
	}
	public void unbindVillager(UUID uuid) {
		if (villagerToBlock.containsKey(uuid)) { blockToVillager.remove(villagerToBlock.remove(uuid)); markDirty(); }
	}
	public void unbindBlock(BlockPos pos) {
		if (blockToVillager.containsKey(pos)) { villagerToBlock.remove(blockToVillager.remove(pos)); markDirty(); }
	}
	public UUID getVillagerForBlock(BlockPos pos) { return blockToVillager.get(pos); }
	public BlockPos getBlockForVillager(UUID uuid) { return villagerToBlock.get(uuid); }
	public boolean isBlockBound(BlockPos pos) { return blockToVillager.containsKey(pos); }
	public boolean isVillagerBound(UUID uuid) { return villagerToBlock.containsKey(uuid); }

	// Bed binding
	public void bindVillagerToBed(UUID uuid, BlockPos headPos) {
		if (villagerToBed.containsKey(uuid)) bedToVillager.remove(villagerToBed.get(uuid));
		villagerToBed.put(uuid, headPos); bedToVillager.put(headPos, uuid); markDirty();
	}
	public void unbindBed(UUID uuid) {
		if (villagerToBed.containsKey(uuid)) { bedToVillager.remove(villagerToBed.remove(uuid)); markDirty(); }
	}
	public void unbindBedBlock(BlockPos headPos) {
		if (bedToVillager.containsKey(headPos)) { villagerToBed.remove(bedToVillager.remove(headPos)); markDirty(); }
	}
	public UUID getBedVillager(BlockPos headPos) { return bedToVillager.get(headPos); }
	public BlockPos getBedForVillager(UUID uuid) { return villagerToBed.get(uuid); }
	public boolean isBedBound(BlockPos headPos) { return bedToVillager.containsKey(headPos); }
	public boolean isVillagerBedBound(UUID uuid) { return villagerToBed.containsKey(uuid); }
}
