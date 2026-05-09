package com.armaninyow.floorbeds.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

// 1.21.11
public class FloorBedBlock extends BedBlock {

	public static final MapCodec<FloorBedBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor),
			createSettingsCodec()
		).apply(instance, FloorBedBlock::new)
	);

	public static final ThreadLocal<Boolean> SUPPRESS_SPAWN_SET = ThreadLocal.withInitial(() -> false);

	private static final VoxelShape FLAT_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

	public FloorBedBlock(DyeColor color, AbstractBlock.Settings settings) {
		super(color, settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FloorBedBlockEntity(pos, state);
	}

	@Override
	public MapCodec<BedBlock> getCodec() {
		return CODEC.xmap(b -> b, b -> (FloorBedBlock) b);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FLAT_SHAPE;
	}

	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		ItemStack tool = player.getMainHandStack();
		boolean isShears = tool.isOf(Items.SHEARS);

		var enchantmentRegistry = player.getRegistryManager()
			.getOrThrow(RegistryKeys.ENCHANTMENT);

		float speedMultiplier;

		if (isShears) {
			speedMultiplier = 2.5f;
			var efficiencyOpt = enchantmentRegistry.getOptional(Enchantments.EFFICIENCY);
			if (efficiencyOpt.isPresent()) {
				int efficiencyLevel = EnchantmentHelper.getLevel(efficiencyOpt.get(), tool);
				if (efficiencyLevel > 0) {
					speedMultiplier += (efficiencyLevel * efficiencyLevel) + 1;
				}
			}
		} else {
			speedMultiplier = 1.0f;
		}

		if (StatusEffectUtil.hasHaste(player)) {
			int hasteLevel = StatusEffectUtil.getHasteAmplifier(player);
			speedMultiplier *= 1.0f + (0.2f * (hasteLevel + 1));
		}

		boolean hasAquaAffinity = false;
		var aquaAffinityOpt = enchantmentRegistry.getOptional(Enchantments.AQUA_AFFINITY);
		if (aquaAffinityOpt.isPresent()) {
			hasAquaAffinity = EnchantmentHelper.getEquipmentLevel(aquaAffinityOpt.get(), player) > 0;
		}
		if (player.isSubmergedInWater() && !hasAquaAffinity) {
			speedMultiplier /= 5.0f;
		}
		if (!player.isOnGround()) {
			speedMultiplier /= 5.0f;
		}

		return speedMultiplier / 0.5f / 30.0f;
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient()) {
			BedPart part = state.get(PART);
			BlockPos footPos  = (part == BedPart.FOOT) ? pos : pos.offset(state.get(FACING).getOpposite());
			BlockPos otherPos = (part == BedPart.FOOT) ? pos.offset(state.get(FACING)) : pos;
			BlockState otherState = world.getBlockState(otherPos);

			if (state.get(OCCUPIED)
					|| (otherState.getBlock() instanceof FloorBedBlock && otherState.get(OCCUPIED))) {
				world.getEntitiesByClass(
					PlayerEntity.class,
					new Box(footPos).expand(2),
					PlayerEntity::isSleeping
				).forEach(p -> p.wakeUp(true, true));
			}

			if (otherState.getBlock() instanceof FloorBedBlock) {
				world.setBlockState(otherPos, Blocks.AIR.getDefaultState(),
					Block.NOTIFY_ALL | Block.SKIP_DROPS);
			}

			if (!player.isCreative()) {
				Block.dropStack(world, footPos, new ItemStack(state.getBlock()));
			}
		}
		return state;
	}

	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
	                        BlockEntity blockEntity, ItemStack tool) {
		// Intentionally empty.
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		if (state.get(PART) != BedPart.HEAD) {
			pos = pos.offset(state.get(FACING));
			state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof FloorBedBlock)) {
				return ActionResult.CONSUME;
			}
		}

		if (state.get(OCCUPIED)) {
			player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
			return ActionResult.SUCCESS;
		}

		SUPPRESS_SPAWN_SET.set(true);
		try {
			player.trySleep(pos).ifLeft(reason -> {
				if (reason != null) {
					player.sendMessage(reason.message(), true);
				}
			});
		} finally {
			SUPPRESS_SPAWN_SET.set(false);
		}

		return ActionResult.SUCCESS;
	}
}