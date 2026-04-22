package com.armaninyow.floorbeds.mixin;

import com.armaninyow.floorbeds.block.FloorBedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

	/**
	 * Cancels the setSpawnPoint call that vanilla fires inside trySleep when
	 * the player is sleeping on a FloorBedBlock.
	 *
	 * In 1.21.10 the signature changed to setSpawnPoint(Respawn, boolean).
	 * We suppress it by checking the thread-local flag that FloorBedBlock.onUse
	 * sets before calling trySleep.
	 */
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