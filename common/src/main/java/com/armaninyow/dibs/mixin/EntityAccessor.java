package com.armaninyow.dibs.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	// Renamed to getDibsWorld() so it does not clash with the public
	// Entity.getWorld() method present in all targeted MC versions.
	// @Accessor("world") reads the private field directly — works in all versions.
	@Accessor("world")
	World getDibsWorld();
}
