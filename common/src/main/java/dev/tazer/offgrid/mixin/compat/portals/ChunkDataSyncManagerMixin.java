//? if <1.21 {
/*package dev.tazer.offgrid.mixin.compat.portals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.chunk_loading.ChunkDataSyncManager;
import qouteall.imm_ptl.core.ducks.IEThreadedAnvilChunkStorage;

@Pseudo
@IfModPresent("imm_ptl_core")
@Mixin(value = ChunkDataSyncManager.class, remap = false)
public abstract class ChunkDataSyncManagerMixin {

    @WrapOperation(method = "sendChunkDataPacketNow", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/ducks/IEThreadedAnvilChunkStorage;ip_updateEntityTrackersAfterSendingChunkPacket(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/server/level/ServerPlayer;)V", remap = true))
    private void offgrid$sendCells(IEThreadedAnvilChunkStorage storage, LevelChunk chunk, ServerPlayer player, Operation<Void> original) {
        original.call(storage, chunk, player);
        offgrid$sent(chunk, player);
    }

    @WrapOperation(method = "lambda$onChunkProvidedDeferred$1", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/ducks/IEThreadedAnvilChunkStorage;ip_updateEntityTrackersAfterSendingChunkPacket(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/server/level/ServerPlayer;)V", remap = true))
    private static void offgrid$sendDeferredCells(IEThreadedAnvilChunkStorage storage, LevelChunk chunk, ServerPlayer player, Operation<Void> original) {
        original.call(storage, chunk, player);
        offgrid$sent(chunk, player);
    }

    @Unique
    private static void offgrid$sent(LevelChunk chunk, ServerPlayer player) {
        if (chunk.getLevel() instanceof ServerLevel level) {
            OffgridEvents.onChunkSent(player, level, chunk);
        }
    }
}
*///?}
