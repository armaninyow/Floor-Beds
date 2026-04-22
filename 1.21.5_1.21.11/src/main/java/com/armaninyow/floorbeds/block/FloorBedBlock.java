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

public class FloorBedBlock extends BedBlock {

	public static final MapCodec<FloorBedBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor),
			createSettingsCodec()
		).apply(instance, FloorBedBlock::new)
	);

	/**
	 * Thread-local flag read by the mixin on ServerPlayerEntity.
	 * When true, suppresses any spawn point change during trySleep.
	 */
	public static final ThreadLocal<Boolean> SUPPRESS_SPAWN_SET = ThreadLocal.withInitial(() -> false);

	// Flat 16x4x16 shape for each part — no bed frame legs
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

	// -------------------------------------------------------------------------
	// Flat outline per part, no bed frame legs
	// -------------------------------------------------------------------------
	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FLAT_SHAPE;
	}

	// -------------------------------------------------------------------------
	// Custom breaking speed
	//   hardness 0.5, default = 15 ticks, shears = 6 ticks
	//   + efficiency enchantment bonus on shears
	//   + haste status effect for all tools
	// -------------------------------------------------------------------------
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

	// -------------------------------------------------------------------------
	// Drop fix
	//
	// The previous approach called super.onBreak (BedBlock), which internally
	// calls world.removeBlock() on the other half. removeBlock() triggers onBreak
	// on that neighbour — recursively, on the same thread — making drop
	// behaviour unpredictable regardless of any thread-local suppression flags.
	//
	// Fix: we fully replace onBreak. We do everything BedBlock.onBreak does
	// (wake sleepers, remove other half) but use setBlockState with SKIP_DROPS
	// instead of removeBlock(), which does NOT trigger onBreak on the neighbour.
	// We then drop exactly one item at the foot position in survival.
	//
	// We return `state` directly at the end — that is all Block.onBreak does
	// besides delegating the drop (which afterBreak handles, and we've made
	// afterBreak a no-op). Break particles and sounds are handled by the game
	// engine independently of what onBreak returns.
	// -------------------------------------------------------------------------
	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient()) {
			BedPart part = state.get(PART);

			// Resolve foot and other-half positions regardless of which half was broken
			BlockPos footPos  = (part == BedPart.FOOT) ? pos : pos.offset(state.get(FACING).getOpposite());
			BlockPos otherPos = (part == BedPart.FOOT) ? pos.offset(state.get(FACING)) : pos;
			BlockState otherState = world.getBlockState(otherPos);

			// Wake any sleeping player near the bed (mirrors vanilla behaviour)
			if (state.get(OCCUPIED)
					|| (otherState.getBlock() instanceof FloorBedBlock && otherState.get(OCCUPIED))) {
				world.getEntitiesByClass(
					PlayerEntity.class,
					new Box(footPos).expand(2),
					PlayerEntity::isSleeping
				).forEach(p -> p.wakeUp(true, true));
			}

			// Remove the other half silently — SKIP_DROPS + setBlockState means
			// onBreak is NOT called on the neighbour, so no recursion
			if (otherState.getBlock() instanceof FloorBedBlock) {
				world.setBlockState(otherPos, Blocks.AIR.getDefaultState(),
					Block.NOTIFY_ALL | Block.SKIP_DROPS);
			}

			// Drop exactly one item in survival, always at the foot position
			if (!player.isCreative()) {
				Block.dropStack(world, footPos, new ItemStack(state.getBlock()));
			}
		}

		// Return state directly — this is all Block.onBreak does that we care
		// about. We skip calling super (BedBlock.onBreak) intentionally.
		return state;
	}

	/**
	 * Suppress the loot-table drop path entirely — onBreak above already
	 * handled the item drop, and we have no loot table for floor beds.
	 */
	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
	                        BlockEntity blockEntity, ItemStack tool) {
		// Intentionally empty.
	}

	// -------------------------------------------------------------------------
	// Sleep: clicking the foot redirects to the head (same as vanilla).
	// -------------------------------------------------------------------------
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		// If the player clicks the foot, jump to the head block
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

		// Suppress spawn point change, then sleep
		SUPPRESS_SPAWN_SET.set(true);
		try {
			player.trySleep(pos).ifLeft(reason -> {
				if (reason != null) {
					// reason.message() returns the already-built translated Text.
					// reason.toString() was printing the raw record representation.
					player.sendMessage(reason.message(), true);
				}
			});
		} finally {
			SUPPRESS_SPAWN_SET.set(false);
		}

		return ActionResult.SUCCESS;
	}
}
