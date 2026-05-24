package com.armaninyow.floorbeds.sound;

import com.armaninyow.floorbeds.FloorBeds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public class FloorBedsSounds {

	public static final SoundEvent FLOOR_BED_BREAK = register("block.floor_bed.break");
	public static final SoundEvent FLOOR_BED_PLACE = register("block.floor_bed.place");
	public static final SoundEvent FLOOR_BED_HIT   = register("block.floor_bed.hit");
	public static final SoundEvent FLOOR_BED_FALL  = register("block.floor_bed.fall");
	public static final SoundEvent FLOOR_BED_STEP  = register("block.floor_bed.step");

	public static final SoundType FLOOR_BED_SOUND_GROUP = new SoundType(
		1.0f,
		1.0f,
		FLOOR_BED_BREAK,
		FLOOR_BED_STEP,
		FLOOR_BED_PLACE,
		FLOOR_BED_HIT,
		FLOOR_BED_FALL
	);

	private static SoundEvent register(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(FloorBeds.MOD_ID, name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void initialize() {
		// Triggers static field initialization
	}
}