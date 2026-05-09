package com.armaninyow.floorbeds;

import com.armaninyow.floorbeds.block.FloorBedBlockEntityType;
import com.armaninyow.floorbeds.client.FloorBedBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class FloorBedsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(
			FloorBedBlockEntityType.FLOOR_BED,
			FloorBedBlockEntityRenderer::new
		);
	}
}