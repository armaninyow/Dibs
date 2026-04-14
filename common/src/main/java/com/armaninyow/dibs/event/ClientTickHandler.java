package com.armaninyow.dibs.event;

import com.armaninyow.dibs.keybind.DibsKeybinds;
import com.armaninyow.dibs.network.LocateOwnerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ClientTickHandler {

	public static void onClientTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		// wasPressed() consumes the press and returns true once per keypress
		while (DibsKeybinds.LOCATE_OWNER_KEY.wasPressed()) {
			handleLocateOwnerKey(client);
		}
	}

	private static void handleLocateOwnerKey(MinecraftClient client) {
		HitResult hitResult = client.crosshairTarget;

		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult) hitResult;
			BlockPos blockPos = blockHitResult.getBlockPos();

			// Send packet to server to locate the owner
			ClientPlayNetworking.send(new LocateOwnerPayload(blockPos));
		}
	}
}