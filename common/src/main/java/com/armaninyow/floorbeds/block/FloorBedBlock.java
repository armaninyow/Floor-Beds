package com.armaninyow.floorbeds.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FloorBedBlock extends BedBlock {

	public static final MapCodec<FloorBedBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor),
			propertiesCodec()
		).apply(instance, FloorBedBlock::new)
	);

	public static final ThreadLocal<Boolean> SUPPRESS_SPAWN_SET = ThreadLocal.withInitial(() -> false);

	private static final VoxelShape FLAT_SHAPE = Block.box(0, 0, 0, 16, 4, 16);

	public FloorBedBlock(DyeColor color, BlockBehaviour.Properties properties) {
		super(color, properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FloorBedBlockEntity(pos, state);
	}

	@Override
	public MapCodec<BedBlock> codec() {
		return CODEC.xmap(b -> b, b -> (FloorBedBlock) b);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return FLAT_SHAPE;
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
		ItemStack tool = player.getMainHandItem();
		boolean isShears = tool.is(Items.SHEARS);

		var enchantmentRegistry = player.registryAccess()
			.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);

		float speedMultiplier;

		if (isShears) {
			speedMultiplier = 2.5f;
			var efficiencyOpt = enchantmentRegistry.get(Enchantments.EFFICIENCY);
			if (efficiencyOpt.isPresent()) {
				int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(efficiencyOpt.get(), tool);
				if (efficiencyLevel > 0) {
					speedMultiplier += (efficiencyLevel * efficiencyLevel) + 1;
				}
			}
		} else {
			speedMultiplier = 1.0f;
		}

		if (MobEffectUtil.hasDigSpeed(player)) {
			int hasteLevel = MobEffectUtil.getDigSpeedAmplification(player);
			speedMultiplier *= 1.0f + (0.2f * (hasteLevel + 1));
		}

		boolean hasAquaAffinity = false;
		var aquaAffinityOpt = enchantmentRegistry.get(Enchantments.AQUA_AFFINITY);
		if (aquaAffinityOpt.isPresent()) {
			hasAquaAffinity = EnchantmentHelper.getItemEnchantmentLevel(aquaAffinityOpt.get(), player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)) > 0;
		}
		if (player.isUnderWater() && !hasAquaAffinity) {
			speedMultiplier /= 5.0f;
		}
		if (!player.onGround()) {
			speedMultiplier /= 5.0f;
		}

		return speedMultiplier / 0.5f / 30.0f;
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide()) {
			BedPart part = state.getValue(PART);
			BlockPos footPos  = (part == BedPart.FOOT) ? pos : pos.relative(state.getValue(FACING).getOpposite());
			BlockPos otherPos = (part == BedPart.FOOT) ? pos.relative(state.getValue(FACING)) : pos;
			BlockState otherState = world.getBlockState(otherPos);

			if (state.getValue(OCCUPIED)
					|| (otherState.getBlock() instanceof FloorBedBlock && otherState.getValue(OCCUPIED))) {
				world.getEntitiesOfClass(
					Player.class,
					new AABB(footPos).inflate(2),
					Player::isSleeping
				).forEach(p -> p.stopSleeping());
			}

			if (otherState.getBlock() instanceof FloorBedBlock) {
				world.setBlock(otherPos, Blocks.AIR.defaultBlockState(),
					Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
			}

			if (!player.isCreative()) {
				Block.popResource(world, footPos, new ItemStack(state.getBlock()));
			}
		}
		return state;
	}

	@Override
	public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state,
	                          BlockEntity blockEntity, ItemStack tool) {
		// Intentionally empty.
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (state.getValue(PART) != BedPart.HEAD) {
			pos = pos.relative(state.getValue(FACING));
			state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof FloorBedBlock)) {
				return InteractionResult.CONSUME;
			}
		}

		if (state.getValue(OCCUPIED)) {
			player.sendOverlayMessage(Component.translatable("block.minecraft.bed.occupied"));
			return InteractionResult.SUCCESS;
		}

		SUPPRESS_SPAWN_SET.set(true);
		try {
			player.startSleepInBed(pos).ifLeft(reason -> {
				if (reason != null) {
					player.sendOverlayMessage(reason.message());
				}
			});
		} finally {
			SUPPRESS_SPAWN_SET.set(false);
		}

		return InteractionResult.SUCCESS;
	}
}