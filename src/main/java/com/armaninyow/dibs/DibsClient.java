package com.armaninyow.dibs;

import com.armaninyow.dibs.data.ClientBindingCache;
import com.armaninyow.dibs.event.ClientTickHandler;
import com.armaninyow.dibs.keybind.DibsKeybinds;
import com.armaninyow.dibs.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DibsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register keybinds
		DibsKeybinds.register();

		// Register client tick handler for keybind processing
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);

		// Register client-side packet handler for wrong claimant suppression
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

		Dibs.LOGGER.info("Dibs client initialized!");
	}
}