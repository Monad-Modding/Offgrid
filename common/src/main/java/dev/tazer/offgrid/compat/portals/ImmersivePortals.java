package dev.tazer.offgrid.compat.portals;

import dev.tazer.offgrid.compat.Integrations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
//? if <26 {
import qouteall.imm_ptl.core.network.PacketRedirection;
//?}
//? if >=1.21 && <26 {
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
//?} else if <1.21 {
/*import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;
*///?}

public final class ImmersivePortals {

    private static Boolean loaded;

    private ImmersivePortals() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Integrations.isLoaded("imm_ptl_core") || Integrations.isLoaded("immersive_portals") || Integrations.isLoaded("immersive_portals_core");
        }
        return loaded;
    }

    public static List<ServerPlayer> viewers(ServerLevel level, BlockPos pos) {
        //? if >=1.21 && <26 {
        return ImmPtlChunkTracking.getPlayersViewingChunk(level.dimension(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())).toList();
        //?} else if <1.21 {
        /*return NewChunkTrackingGraph.getPlayersViewingChunk(level.dimension(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())).toList();
        *///?} else {
        /*return List.of();
        *///?}
    }

    public static void sending(ServerLevel level, ServerPlayer player, Runnable send) {
        //? if <26 {
        if (isLoaded() && player.level() != level) {
            PacketRedirection.withForceRedirect(level, send);
            return;
        }
        //?}
        send.run();
    }
}
