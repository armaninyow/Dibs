package com.armaninyow.dibs.event;

import com.armaninyow.dibs.keybind.DibsKeybinds;
import com.armaninyow.dibs.network.LocateOwnerPayload;
import com.armaninyow.dibs.network.LocateVillagerBlockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ClientTickHandler {

	public static void onClientTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		while (DibsKeybinds.LOCATE_OWNER_KEY.wasPressed()) {
			handleLocateOwnerKey(client);
		}
	}

	private static void handleLocateOwnerKey(MinecraftClient client) {
		HitResult hitResult = client.crosshairTarget;
		if (hitResult == null) return;

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			// Existing behavior: press V on a block -> highlight bound villager
			BlockHitResult blockHitResult = (BlockHitResult) hitResult;
			BlockPos blockPos = blockHitResult.getBlockPos();
			ClientPlayNetworking.send(new LocateOwnerPayload(blockPos));

		} else if (hitResult.getType() == HitResult.Type.ENTITY) {
			// New behavior: press V on a villager -> draw particle trail to bound block(s)
			EntityHitResult entityHitResult = (EntityHitResult) hitResult;
			Entity entity = entityHitResult.getEntity();
			if (entity instanceof VillagerEntity villager) {
				ClientPlayNetworking.send(new LocateVillagerBlockPayload(villager.getUuid()));
			}
		}
	}
}