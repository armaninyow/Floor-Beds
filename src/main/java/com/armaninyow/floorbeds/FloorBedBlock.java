package com.armaninyow.floorbeds.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
	// Issue 5: flat outline per part, no bed frame legs
	// -------------------------------------------------------------------------
	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FLAT_SHAPE;
	}

	// -------------------------------------------------------------------------
	// Issue 3: custom breaking speed
	//   hardness 0.5, default = 15 ticks, shears = 6 ticks
	//   + efficiency enchantment bonus on shears
	//   + haste status effect for all tools
	//
	// Formula per tick: delta = speedMultiplier / hardness / 30
	// To hit N ticks: speedMultiplier = hardness * 30 / N
	//   default (any tool, speed 1.0): 0.5 * 30 / 15 = 1.0
	//   shears  (speed 2.5):           0.5 * 30 /  6 = 2.5
	// -------------------------------------------------------------------------
	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		ItemStack tool = player.getMainHandStack();
		boolean isShears = tool.isOf(Items.SHEARS);

		var enchantmentRegistry = player.getRegistryManager()
			.getOrThrow(RegistryKeys.ENCHANTMENT);

		float speedMultiplier;

		if (isShears) {
			// Base shears speed — targets 6 ticks
			speedMultiplier = 2.5f;

			// Efficiency enchantment: adds (level^2 + 1) to speed
			// getOptional returns Optional<RegistryEntry.Reference<T>> which is a RegistryEntry<T>
			var efficiencyOpt = enchantmentRegistry.getOptional(Enchantments.EFFICIENCY);
			if (efficiencyOpt.isPresent()) {
				int efficiencyLevel = EnchantmentHelper.getLevel(efficiencyOpt.get(), tool);
				if (efficiencyLevel > 0) {
					speedMultiplier += (efficiencyLevel * efficiencyLevel) + 1;
				}
			}
		} else {
			// Default — targets 15 ticks
			speedMultiplier = 1.0f;
		}

		// Haste status effect: multiplies speed by (1 + 0.2 * level)
		if (StatusEffectUtil.hasHaste(player)) {
			int hasteLevel = StatusEffectUtil.getHasteAmplifier(player);
			speedMultiplier *= 1.0f + (0.2f * (hasteLevel + 1));
		}

		// Underwater and airborne penalties (vanilla parity)
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
	// Drop suppression: no drops in creative mode
	// -------------------------------------------------------------------------
	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient() && !player.isCreative()) {
			// Only drop from foot part, same as loot table condition
			if (state.get(PART) == BedPart.FOOT) {
				Block.dropStack(world, pos, new ItemStack(state.getBlock()));
			}
		}
		return super.onBreak(world, pos, state, player);
	}

	// -------------------------------------------------------------------------
	// Issue 4: clicking the foot redirects to the head (same as vanilla)
	// The actual head/foot visual swap is fixed in the blockstate JSON.
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

		// Explode in unsupported dimensions (Nether / End)
		if (!BedBlock.isBedWorking(world)) {
			world.removeBlock(pos, false);
			BlockPos otherPos = pos.offset(state.get(FACING).getOpposite());
			if (world.getBlockState(otherPos).getBlock() instanceof FloorBedBlock) {
				world.removeBlock(otherPos, false);
			}
			world.createExplosion(
				null, null, null,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				5.0f, true,
				World.ExplosionSourceType.BLOCK
			);
			return ActionResult.SUCCESS;
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
					player.sendMessage(reason.getMessage(), true);
				}
			});
		} finally {
			SUPPRESS_SPAWN_SET.set(false);
		}

		return ActionResult.SUCCESS;
	}
}