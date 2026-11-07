package dev.tazer.offgrid.forge.client;

import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.compat.Integrations;
import dev.tazer.offgrid.forge.compat.oculus.OculusMeshAppender;
import net.minecraft.core.BlockPos;
import org.embeddedt.embeddium.api.ChunkMeshEvent;

public final class EmbeddiumMeshing {

    private EmbeddiumMeshing() {
    }

    public static void register() {
        ChunkMeshEvent.BUS.addListener(EmbeddiumMeshing::onChunkMesh);
    }

    private static void onChunkMesh(ChunkMeshEvent event) {
        BlockPos origin = event.getSectionOrigin().origin();
        if (!OverlayGeometry.hasOverlays(event.getWorld(), origin)) return;
        if (Integrations.isLoaded(Integrations.OCULUS)) {
            event.addMeshAppender(OculusMeshAppender::render);
            return;
        }
        event.addMeshAppender(context -> ForgeOverlayGeometry.render(context.blockRenderView(), context.sectionOrigin().origin(), context.vertexConsumerProvider()));
    }
}
