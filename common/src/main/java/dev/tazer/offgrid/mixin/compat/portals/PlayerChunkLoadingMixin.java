//? if >=1.21 && <26 {
package dev.tazer.offgrid.mixin.compat.portals;

import dev.tazer.offgrid.platform.condition.IfAnyModPresent;
import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.PlayerChunkLoading;

@Pseudo
@IfAnyModPresent({"immersive_portals", "immersive_portals_core"})
@Mixin(value = PlayerChunkLoading.class, remap = false)
public abstract class PlayerChunkLoadingMixin {

    @Inject(method = "sendChunkPacket", at = @At("TAIL"))
    private static void offgrid$sendCells(ServerGamePacketListenerImpl listener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        OffgridEvents.onChunkSent(listener.player, level, chunk);
    }
}
//?}
