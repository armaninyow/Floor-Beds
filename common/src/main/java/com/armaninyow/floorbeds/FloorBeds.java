package com.armaninyow.floorbeds;

import com.armaninyow.floorbeds.block.FloorBedBlockEntityType;
import com.armaninyow.floorbeds.block.FloorBedsBlocks;
import com.armaninyow.floorbeds.sound.FloorBedsSounds;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloorBeds implements ModInitializer {
	public static final String MOD_ID = "floorbeds";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Sounds must be registered before blocks, because FloorBedsBlocks
		// references FloorBedsSounds.FLOOR_BED_SOUND_GROUP during block creation.
		FloorBedsSounds.initialize();
		FloorBedsBlocks.initialize();
		FloorBedBlockEntityType.initialize();

		LOGGER.info("Floor Beds loaded!");
	}
}