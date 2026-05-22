package com.armaninyow.dibs.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Dibs! Configuration"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("General"))
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Binding Particle Duration (seconds)"))
								.description(OptionDescription.of(Component.literal("How long happy_villager particles appear after binding ritual")))
								.binding(
										2,
										() -> DibsConfig.INSTANCE.particleDurationSeconds,
										val -> DibsConfig.INSTANCE.particleDurationSeconds = val
								)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(1).max(60))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Glow Duration (seconds)"))
								.description(OptionDescription.of(Component.literal("How long the Glowing effect lasts when locating a villager with the keybind")))
								.binding(
										5,
										() -> DibsConfig.INSTANCE.glowDurationSeconds,
										val -> DibsConfig.INSTANCE.glowDurationSeconds = val
								)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(1).max(60))
								.build())
						.build())
				.save(DibsConfig::save)
				.build()
				.generateScreen(parent);
	}
}