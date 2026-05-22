package com.armaninyow.dibs.keybind;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class DibsKeybinds {
	public static KeyMapping LOCATE_OWNER_KEY;

	public static void register() {
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("dibs", "keybinds")
		);
		LOCATE_OWNER_KEY = new KeyMapping(
				"key.dibs.locate_owner",
				GLFW.GLFW_KEY_V,
				category
		);
	}
}