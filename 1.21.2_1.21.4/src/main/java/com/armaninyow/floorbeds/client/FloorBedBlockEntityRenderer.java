package com.armaninyow.floorbeds.client;

import com.armaninyow.floorbeds.block.FloorBedBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

// 1.21.2_1.21.4
public class FloorBedBlockEntityRenderer implements BlockEntityRenderer<FloorBedBlockEntity> {

	public FloorBedBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(FloorBedBlockEntity entity, float tickDelta, MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		World world = entity.getWorld();
		if (world == null) return;

		var state = entity.getCachedState();
		var blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
		var model = blockRenderManager.getModel(state);

		matrices.push();
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
		blockRenderManager.getModelRenderer().render(
			world,
			model,
			state,
			entity.getPos(),
			matrices,
			consumer,
			false,
			world.getRandom(),
			state.getRenderingSeed(entity.getPos()),
			overlay
		);
		matrices.pop();
	}
}