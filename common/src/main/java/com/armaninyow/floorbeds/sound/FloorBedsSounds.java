package com.armaninyow.floorbeds.sound;

import com.armaninyow.floorbeds.FloorBeds;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class FloorBedsSounds {

	// The four .ogg files are registered as four separate SoundEvents.
	// sounds.json will map each event to all four .ogg files so Minecraft
	// picks one randomly per play call.
	public static final SoundEvent FLOOR_BED_BREAK   = register("block.floor_bed.break");
	public static final SoundEvent FLOOR_BED_PLACE   = register("block.floor_bed.place");
	public static final SoundEvent FLOOR_BED_HIT     = register("block.floor_bed.hit");
	public static final SoundEvent FLOOR_BED_FALL    = register("block.floor_bed.fall");
	public static final SoundEvent FLOOR_BED_STEP    = register("block.floor_bed.step");

	/**
	 * Custom SoundGroup that replaces the default wood sounds of vanilla beds
	 * with the soft cloth-like sounds supplied by the mod.
	 *
	 * Parameter order: volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound
	 */
	public static final BlockSoundGroup FLOOR_BED_SOUND_GROUP = new BlockSoundGroup(
		1.0f,
		1.0f,
		FLOOR_BED_BREAK,
		FLOOR_BED_STEP,
		FLOOR_BED_PLACE,
		FLOOR_BED_HIT,
		FLOOR_BED_FALL
	);

	private static SoundEvent register(String name) {
		Identifier id = Identifier.of(FloorBeds.MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void initialize() {
		// Triggers static field initialization
	}
}