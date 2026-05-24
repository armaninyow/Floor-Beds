package com.armaninyow.floorbeds.mixin;

import com.armaninyow.floorbeds.block.FloorBedBlock;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

	@Inject(
		method = "setRespawnPosition",
		at = @At("HEAD"),
		cancellable = true
	)
	private void floorbeds$suppressSpawnSet(
		ServerPlayer.RespawnConfig respawn,
		boolean sendMessage,
		CallbackInfo ci
	) {
		if (FloorBedBlock.SUPPRESS_SPAWN_SET.get()) {
			ci.cancel();
		}
	}
}