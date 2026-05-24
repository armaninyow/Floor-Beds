package com.armaninyow.floorbeds.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FloorBedBlockEntity extends BlockEntity {

	public FloorBedBlockEntity(BlockPos pos, BlockState state) {
		super(FloorBedBlockEntityType.FLOOR_BED, pos, state);
	}
}