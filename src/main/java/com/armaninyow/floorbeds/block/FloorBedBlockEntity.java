package com.armaninyow.floorbeds.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class FloorBedBlockEntity extends BlockEntity {

	public FloorBedBlockEntity(BlockPos pos, BlockState state) {
		super(FloorBedBlockEntityType.FLOOR_BED, pos, state);
	}
}