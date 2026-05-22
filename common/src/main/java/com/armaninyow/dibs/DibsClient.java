package com.armaninyow.dibs;

import com.armaninyow.dibs.client.ParticleTrailRenderer;
import com.armaninyow.dibs.data.ClientBindingCache;
import com.armaninyow.dibs.event.ClientTickHandler;
import com.armaninyow.dibs.keybind.DibsKeybinds;
import com.armaninyow.dibs.network.NetworkHandler;
import com.armaninyow.dibs.network.VillagerBlockResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DibsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DibsKeybinds.register();

		ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);

		ClientPlayNetworking.registerGlobalReceiver(
				NetworkHandler.WrongClaimantPayload.ID,
				(payload, context) -> {
					if (payload.isWrong()) {
						ClientBindingCache.markWrongClaimant(payload.villagerUuid());
					} else {
						ClientBindingCache.clearWrongClaimant(payload.villagerUuid());
					}
				}
		);

		ClientPlayNetworking.registerGlobalReceiver(
				VillagerBlockResponsePayload.ID,
				(payload, context) -> {
					if (payload.workstationPos() != null) {
						ParticleTrailRenderer.drawTrailToBlock(payload.villagerUuid(), payload.workstationPos());
					}
					if (payload.bedPos() != null) {
						ParticleTrailRenderer.drawTrailToBlock(payload.villagerUuid(), payload.bedPos());
					}
				}
		);

		Dibs.LOGGER.info("Dibs client initialized!");
	}
}