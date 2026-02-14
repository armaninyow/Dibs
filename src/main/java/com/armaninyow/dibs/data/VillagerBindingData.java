package com.armaninyow.dibs.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class VillagerBindingData extends PersistentState {
	private static final String DATA_NAME = "dibs_bindings";
	
	// Map: Villager UUID -> Bound Block Position
	private final Map<UUID, BlockPos> villagerToBlock = new HashMap<>();
	
	// Map: Block Position -> Villager UUID
	private final Map<BlockPos, UUID> blockToVillager = new HashMap<>();

	public VillagerBindingData() {
		super();
	}

	public static VillagerBindingData get(World world) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return null;
		}
		
		// Use the Codec constructor - Codec.unit creates a codec that always returns the same value
		Codec<VillagerBindingData> codec = Codec.unit(VillagerBindingData::new);
		
		PersistentStateType<VillagerBindingData> type = new PersistentStateType<VillagerBindingData>(
			DATA_NAME,
			VillagerBindingData::new,
			codec,
			null
		);
		
		return serverWorld.getPersistentStateManager().getOrCreate(type);
	}

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

	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		NbtCompound bindings = new NbtCompound();
		
		for (Map.Entry<UUID, BlockPos> entry : villagerToBlock.entrySet()) {
			BlockPos pos = entry.getValue();
			NbtCompound posNbt = new NbtCompound();
			posNbt.putInt("x", pos.getX());
			posNbt.putInt("y", pos.getY());
			posNbt.putInt("z", pos.getZ());
			bindings.put(entry.getKey().toString(), posNbt);
		}
		
		nbt.put("bindings", bindings);
		return nbt;
	}

	public static VillagerBindingData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		VillagerBindingData data = new VillagerBindingData();
		
		if (nbt.contains("bindings")) {
			NbtCompound bindings = (NbtCompound) nbt.get("bindings");
			for (String key : bindings.getKeys()) {
				try {
					UUID villagerUuid = UUID.fromString(key);
					NbtCompound posNbt = (NbtCompound) bindings.get(key);
					int x = ((NbtInt) posNbt.get("x")).intValue();
					int y = ((NbtInt) posNbt.get("y")).intValue();
					int z = ((NbtInt) posNbt.get("z")).intValue();
					BlockPos pos = new BlockPos(x, y, z);
					data.villagerToBlock.put(villagerUuid, pos);
					data.blockToVillager.put(pos, villagerUuid);
				} catch (Exception e) {
					// Skip invalid UUID or data
				}
			}
		}
		
		return data;
	}
}