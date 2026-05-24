package com.armaninyow.floorbeds.block;

import com.armaninyow.floorbeds.FloorBeds;
import com.armaninyow.floorbeds.sound.FloorBedsSounds;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class FloorBedsBlocks {

	public static final FloorBedBlock WHITE_FLOOR_BED      = register("white_floor_bed",      DyeColor.WHITE);
	public static final FloorBedBlock ORANGE_FLOOR_BED     = register("orange_floor_bed",     DyeColor.ORANGE);
	public static final FloorBedBlock MAGENTA_FLOOR_BED    = register("magenta_floor_bed",    DyeColor.MAGENTA);
	public static final FloorBedBlock LIGHT_BLUE_FLOOR_BED = register("light_blue_floor_bed", DyeColor.LIGHT_BLUE);
	public static final FloorBedBlock YELLOW_FLOOR_BED     = register("yellow_floor_bed",     DyeColor.YELLOW);
	public static final FloorBedBlock LIME_FLOOR_BED       = register("lime_floor_bed",       DyeColor.LIME);
	public static final FloorBedBlock PINK_FLOOR_BED       = register("pink_floor_bed",       DyeColor.PINK);
	public static final FloorBedBlock GRAY_FLOOR_BED       = register("gray_floor_bed",       DyeColor.GRAY);
	public static final FloorBedBlock LIGHT_GRAY_FLOOR_BED = register("light_gray_floor_bed", DyeColor.LIGHT_GRAY);
	public static final FloorBedBlock CYAN_FLOOR_BED       = register("cyan_floor_bed",       DyeColor.CYAN);
	public static final FloorBedBlock PURPLE_FLOOR_BED     = register("purple_floor_bed",     DyeColor.PURPLE);
	public static final FloorBedBlock BLUE_FLOOR_BED       = register("blue_floor_bed",       DyeColor.BLUE);
	public static final FloorBedBlock BROWN_FLOOR_BED      = register("brown_floor_bed",      DyeColor.BROWN);
	public static final FloorBedBlock GREEN_FLOOR_BED      = register("green_floor_bed",      DyeColor.GREEN);
	public static final FloorBedBlock RED_FLOOR_BED        = register("red_floor_bed",        DyeColor.RED);
	public static final FloorBedBlock BLACK_FLOOR_BED      = register("black_floor_bed",      DyeColor.BLACK);

	private static final List<FloorBedBlock> ALL = List.of(
		WHITE_FLOOR_BED, LIGHT_GRAY_FLOOR_BED, GRAY_FLOOR_BED, BLACK_FLOOR_BED,
		BROWN_FLOOR_BED, RED_FLOOR_BED, ORANGE_FLOOR_BED, YELLOW_FLOOR_BED,
		LIME_FLOOR_BED, GREEN_FLOOR_BED, CYAN_FLOOR_BED, LIGHT_BLUE_FLOOR_BED,
		BLUE_FLOOR_BED, PURPLE_FLOOR_BED, MAGENTA_FLOOR_BED, PINK_FLOOR_BED
	);

	private static FloorBedBlock register(String name, DyeColor color) {
		Identifier id = Identifier.fromNamespaceAndPath(FloorBeds.MOD_ID, name);
		ResourceKey<net.minecraft.world.level.block.Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

		FloorBedBlock block = new FloorBedBlock(
			color,
			BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_BED)
				.strength(0.5f)
				.sound(FloorBedsSounds.FLOOR_BED_SOUND_GROUP)
				.setId(blockKey)
		);

		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
		Registry.register(
			BuiltInRegistries.ITEM,
			itemKey,
			new BlockItem(block, new Item.Properties().setId(itemKey))
		);

		return block;
	}

	private static List<ItemStack> allStacks() {
		return ALL.stream().map(ItemStack::new).toList();
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
			entries.insertAfter(Blocks.PINK_BED, allStacks())
		);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS).register(entries ->
			entries.insertAfter(Blocks.PINK_BED, allStacks())
		);
	}
}