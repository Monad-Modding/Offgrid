//? if <1.21 {
/*package dev.tazer.offgrid.mixin.server;

import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "playerLoadedChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;trackChunk(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.AFTER))
    private void offgrid$sendOffgridChunk(ServerPlayer player, MutableObject<ClientboundLevelChunkWithLightPacket> packet, LevelChunk chunk, CallbackInfo ci) {
        OffgridEvents.onChunkSent(player, this.level, chunk);
    }
}
*///?}
