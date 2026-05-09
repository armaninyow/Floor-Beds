package com.armaninyow.floorbeds.mixin;

import com.armaninyow.floorbeds.block.FloorBedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.RegistryKey;

// 1.21_1.21.1
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

	@Inject(
		method = "setSpawnPoint",
		at = @At("HEAD"),
		cancellable = true
	)
	private void floorbeds$suppressSpawnSet(
		RegistryKey<World> dimension,
		BlockPos pos,
		float angle,
		boolean forced,
		boolean sendMessage,
		CallbackInfo ci
	) {
		if (FloorBedBlock.SUPPRESS_SPAWN_SET.get()) {
			ci.cancel();
		}
	}
}
