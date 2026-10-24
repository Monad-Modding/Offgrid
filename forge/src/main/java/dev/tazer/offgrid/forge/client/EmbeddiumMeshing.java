package dev.tazer.offgrid.forge.client;

import dev.tazer.offgrid.client.OverlayGeometry;
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
        event.addMeshAppender(context -> ForgeOverlayGeometry.render(context.blockRenderView(), context.sectionOrigin().origin(), context.vertexConsumerProvider()));
    }
}
