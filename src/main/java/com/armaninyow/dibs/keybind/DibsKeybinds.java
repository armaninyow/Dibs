package com.armaninyow.dibs.keybind;

import com.armaninyow.dibs.Dibs;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DibsKeybinds {
	public static KeyBinding LOCATE_OWNER_KEY;

	public static void register() {
		LOCATE_OWNER_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.dibs.locate_owner",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				new KeyBinding.Category(Dibs.id("keybinds"))
		));
	}
}