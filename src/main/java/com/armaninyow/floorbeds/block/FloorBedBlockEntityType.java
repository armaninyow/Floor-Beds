package com.armaninyow.floorbeds.block;

import com.armaninyow.floorbeds.FloorBeds;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FloorBedBlockEntityType {

	public static BlockEntityType<FloorBedBlockEntity> FLOOR_BED;

	public static void initialize() {
		FLOOR_BED = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of(FloorBeds.MOD_ID, "floor_bed"),
			FabricBlockEntityTypeBuilder.create(
				FloorBedBlockEntity::new,
				FloorBedsBlocks.WHITE_FLOOR_BED,
				FloorBedsBlocks.ORANGE_FLOOR_BED,
				FloorBedsBlocks.MAGENTA_FLOOR_BED,
				FloorBedsBlocks.LIGHT_BLUE_FLOOR_BED,
				FloorBedsBlocks.YELLOW_FLOOR_BED,
				FloorBedsBlocks.LIME_FLOOR_BED,
				FloorBedsBlocks.PINK_FLOOR_BED,
				FloorBedsBlocks.GRAY_FLOOR_BED,
				FloorBedsBlocks.LIGHT_GRAY_FLOOR_BED,
				FloorBedsBlocks.CYAN_FLOOR_BED,
				FloorBedsBlocks.PURPLE_FLOOR_BED,
				FloorBedsBlocks.BLUE_FLOOR_BED,
				FloorBedsBlocks.BROWN_FLOOR_BED,
				FloorBedsBlocks.GREEN_FLOOR_BED,
				FloorBedsBlocks.RED_FLOOR_BED,
				FloorBedsBlocks.BLACK_FLOOR_BED
			).build()
		);
	}
}