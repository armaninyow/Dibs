package com.armaninyow.dibs.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create()
					.setParentScreen(parent)
					.setTitle(Text.literal("Dibs! Configuration"));

			ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
			ConfigEntryBuilder entryBuilder = builder.entryBuilder();

			general.addEntry(entryBuilder.startIntField(
							Text.literal("Binding Particle Duration (seconds)"),
							DibsConfig.INSTANCE.particleDurationSeconds)
					.setDefaultValue(2)
					.setMin(1)
					.setMax(60)
					.setTooltip(Text.literal("How long happy_villager particles appear after binding ritual"))
					.setSaveConsumer(value -> DibsConfig.INSTANCE.particleDurationSeconds = value)
					.build());

			general.addEntry(entryBuilder.startIntField(
							Text.literal("Glow Duration (seconds)"),
							DibsConfig.INSTANCE.glowDurationSeconds)
					.setDefaultValue(5)
					.setMin(1)
					.setMax(60)
					.setTooltip(Text.literal("How long the Glowing effect lasts when locating a villager with the keybind"))
					.setSaveConsumer(value -> DibsConfig.INSTANCE.glowDurationSeconds = value)
					.build());

			builder.setSavingRunnable(DibsConfig::save);

			return builder.build();
		};
	}
}