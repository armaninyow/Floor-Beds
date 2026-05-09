package com.armaninyow.floorbeds.mixin;

import com.armaninyow.floorbeds.block.FloorBedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.5_1.21.11
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

	@Inject(
		method = "setSpawnPoint",
		at = @At("HEAD"),
		cancellable = true
	)
	private void floorbeds$suppressSpawnSet(
		ServerPlayerEntity.Respawn respawn,
		boolean sendMessage,
		CallbackInfo ci
	) {
		if (FloorBedBlock.SUPPRESS_SPAWN_SET.get()) {
			ci.cancel();
		}
	}
}