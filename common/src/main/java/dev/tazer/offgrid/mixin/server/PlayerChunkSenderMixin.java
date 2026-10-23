//? if >=1.21 {
package dev.tazer.offgrid.mixin.server;

import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void offgrid$sendOffgridChunk(ServerGamePacketListenerImpl listener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        OffgridEvents.onChunkSent(listener.player, level, chunk);
    }
}
//?}
