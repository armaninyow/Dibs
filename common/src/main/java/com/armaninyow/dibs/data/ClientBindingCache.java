package com.armaninyow.dibs.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBindingCache {

	private static final long EXPIRY_MS = 2000;
	private static final Map<UUID, Long> wrongClaimants = new ConcurrentHashMap<>();

	public static void markWrongClaimant(UUID villagerUuid) {
		wrongClaimants.put(villagerUuid, System.currentTimeMillis() + EXPIRY_MS);
	}

	public static void clearWrongClaimant(UUID villagerUuid) {
		wrongClaimants.remove(villagerUuid);
	}

	public static boolean isWrongClaimant(UUID villagerUuid) {
		Long expiry = wrongClaimants.get(villagerUuid);
		if (expiry == null) return false;
		if (System.currentTimeMillis() > expiry) {
			wrongClaimants.remove(villagerUuid);
			return false;
		}
		return true;
	}

	public static void clear() {
		wrongClaimants.clear();
	}
}