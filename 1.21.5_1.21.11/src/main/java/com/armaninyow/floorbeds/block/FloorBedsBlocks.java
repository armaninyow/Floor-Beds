package com.armaninyow.floorbeds.block;

import com.armaninyow.floorbeds.FloorBeds;
import com.armaninyow.floorbeds.sound.FloorBedsSounds;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class FloorBedsBlocks {

	public static final FloorBedBlock WHITE_FLOOR_BED = register("white_floor_bed", DyeColor.WHITE);
	public static final FloorBedBlock ORANGE_FLOOR_BED = register("orange_floor_bed", DyeColor.ORANGE);
	public static final FloorBedBlock MAGENTA_FLOOR_BED = register("magenta_floor_bed", DyeColor.MAGENTA);
	public static final FloorBedBlock LIGHT_BLUE_FLOOR_BED = register("light_blue_floor_bed", DyeColor.LIGHT_BLUE);
	public static final FloorBedBlock YELLOW_FLOOR_BED = register("yellow_floor_bed", DyeColor.YELLOW);
	public static final FloorBedBlock LIME_FLOOR_BED = register("lime_floor_bed", DyeColor.LIME);
	public static final FloorBedBlock PINK_FLOOR_BED = register("pink_floor_bed", DyeColor.PINK);
	public static final FloorBedBlock GRAY_FLOOR_BED = register("gray_floor_bed", DyeColor.GRAY);
	public static final FloorBedBlock LIGHT_GRAY_FLOOR_BED = register("light_gray_floor_bed", DyeColor.LIGHT_GRAY);
	public static final FloorBedBlock CYAN_FLOOR_BED = register("cyan_floor_bed", DyeColor.CYAN);
	public static final FloorBedBlock PURPLE_FLOOR_BED = register("purple_floor_bed", DyeColor.PURPLE);
	public static final FloorBedBlock BLUE_FLOOR_BED = register("blue_floor_bed", DyeColor.BLUE);
	public static final FloorBedBlock BROWN_FLOOR_BED = register("brown_floor_bed", DyeColor.BROWN);
	public static final FloorBedBlock GREEN_FLOOR_BED = register("green_floor_bed", DyeColor.GREEN);
	public static final FloorBedBlock RED_FLOOR_BED = register("red_floor_bed", DyeColor.RED);
	public static final FloorBedBlock BLACK_FLOOR_BED = register("black_floor_bed", DyeColor.BLACK);

	private static FloorBedBlock register(String name, DyeColor color) {
		Identifier id = Identifier.of(FloorBeds.MOD_ID, name);
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

		FloorBedBlock block = new FloorBedBlock(
			color,
			AbstractBlock.Settings.copy(Blocks.WHITE_BED)
				.strength(0.5f)
				.sounds(FloorBedsSounds.FLOOR_BED_SOUND_GROUP)
				.registryKey(blockKey)
		);

		Registry.register(Registries.BLOCK, blockKey, block);
		Registry.register(
			Registries.ITEM,
			itemKey,
			new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey().registryKey(itemKey))
		);

		return block;
	}

	public static void initialize() {
		// Vanilla bed order in creative tabs:
		// White, Light Gray, Gray, Black, Brown, Red, Orange, Yellow,
		// Lime, Green, Cyan, Light Blue, Blue, Purple, Magenta, Pink

		// Add to Functional Blocks tab after the last vanilla bed (pink_bed)
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
			entries.addAfter(Blocks.PINK_BED,
				WHITE_FLOOR_BED,
				LIGHT_GRAY_FLOOR_BED,
				GRAY_FLOOR_BED,
				BLACK_FLOOR_BED,
				BROWN_FLOOR_BED,
				RED_FLOOR_BED,
				ORANGE_FLOOR_BED,
				YELLOW_FLOOR_BED,
				LIME_FLOOR_BED,
				GREEN_FLOOR_BED,
				CYAN_FLOOR_BED,
				LIGHT_BLUE_FLOOR_BED,
				BLUE_FLOOR_BED,
				PURPLE_FLOOR_BED,
				MAGENTA_FLOOR_BED,
				PINK_FLOOR_BED
			);
		});

		// Add to Colored Blocks tab after the last vanilla bed (pink_bed)
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(entries -> {
			entries.addAfter(Blocks.PINK_BED,
				WHITE_FLOOR_BED,
				LIGHT_GRAY_FLOOR_BED,
				GRAY_FLOOR_BED,
				BLACK_FLOOR_BED,
				BROWN_FLOOR_BED,
				RED_FLOOR_BED,
				ORANGE_FLOOR_BED,
				YELLOW_FLOOR_BED,
				LIME_FLOOR_BED,
				GREEN_FLOOR_BED,
				CYAN_FLOOR_BED,
				LIGHT_BLUE_FLOOR_BED,
				BLUE_FLOOR_BED,
				PURPLE_FLOOR_BED,
				MAGENTA_FLOOR_BED,
				PINK_FLOOR_BED
			);
		});
	}
}