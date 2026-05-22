package com.armaninyow.dibs.event;

import com.armaninyow.dibs.keybind.DibsKeybinds;
import com.armaninyow.dibs.network.LocateOwnerPayload;
import com.armaninyow.dibs.network.LocateVillagerBlockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ClientTickHandler {

	public static void onClientTick(Minecraft client) {
		if (client.player == null || client.level == null) return;

		while (DibsKeybinds.LOCATE_OWNER_KEY.consumeClick()) {
			handleLocateOwnerKey(client);
		}
	}

	private static void handleLocateOwnerKey(Minecraft client) {
		HitResult hitResult = client.hitResult;
		if (hitResult == null) return;

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHit = (BlockHitResult) hitResult;
			BlockPos blockPos = blockHit.getBlockPos();
			ClientPlayNetworking.send(new LocateOwnerPayload(blockPos));

		} else if (hitResult.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityHit = (EntityHitResult) hitResult;
			Entity entity = entityHit.getEntity();
			if (entity instanceof Villager villager) {
				ClientPlayNetworking.send(new LocateVillagerBlockPayload(villager.getUUID()));
			}
		}
	}
}