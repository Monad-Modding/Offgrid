//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.mixin.compat.sable;

import dev.ryanhcode.sable.sublevel.plot.SubLevelPlayerChunkSender;
import dev.tazer.offgrid.network.ChunkSync;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Cells;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Pseudo
@IfModPresent("sable")
@Mixin(SubLevelPlayerChunkSender.class)
public class SubLevelPlayerChunkSenderMixin {

    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void offgrid$sendCells(Consumer<Packet<? super ClientGamePacketListener>> consumer, LevelLightEngine light, LevelChunk chunk, CallbackInfo ci) {
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;
        ChunkSync packet = Cells.snapshot(level, chunk);
        if (packet != null) {
            consumer.accept(new ClientboundCustomPayloadPacket(OffgridNetwork.CHUNK.wrap(packet)));
        }
    }
}
//?}
