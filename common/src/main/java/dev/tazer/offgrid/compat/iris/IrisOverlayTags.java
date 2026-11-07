//? if >=1.21 {
package dev.tazer.offgrid.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.client.OverlayShaderTags;
import dev.tazer.offgrid.mixin.compat.sodium.ChunkVertexConsumerAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class IrisOverlayTags extends OverlayShaderTags {

    private static final ThreadLocal<Object> BUFFERS = new ThreadLocal<>();

    private final List<VertexConsumer> touched = new ArrayList<>();

    @Nullable
    private BlockSensitiveBufferBuilder sharedContext;
    private boolean active;
    private int blockId;
    private byte emission;
    private int localX;
    private int localY;
    private int localZ;

    public static void setBuffers(@Nullable Object buffers) {
        BUFFERS.set(buffers);
    }

    @Override
    public void enter(OverlayGeometry.Entry entry) {
        Object2IntMap<BlockState> ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        this.active = ids != null;
        if (!this.active) return;
        BlockPos host = entry.host();
        this.blockId = ids.getOrDefault(entry.state(), -1);
        this.emission = (byte) entry.state().getLightEmission();
        this.localX = host.getX() & 15;
        this.localY = host.getY() & 15;
        this.localZ = host.getZ() & 15;
        if (BUFFERS.get() instanceof BlockSensitiveBufferBuilder context) {
            this.sharedContext = context;
            context.beginBlock(this.blockId, (byte) 0, this.emission, this.localX, this.localY, this.localZ);
        }
    }

    @Override
    public VertexConsumer tag(VertexConsumer consumer) {
        if (!this.active || this.sharedContext != null || this.touched.contains(consumer) || !(consumer instanceof ChunkVertexConsumerAccessor accessor)) return consumer;
        this.touched.add(consumer);
        if (accessor.offgrid$getModelBuilder() instanceof BlockSensitiveBufferBuilder context) {
            context.beginBlock(this.blockId, (byte) 0, this.emission, this.localX, this.localY, this.localZ);
        } else {
            Vertices.tag(accessor.offgrid$getVertices(), this.emission, this.blockId, this.localX, this.localY, this.localZ);
        }
        return consumer;
    }

    @Override
    public void exit() {
        if (this.sharedContext != null) {
            this.sharedContext.endBlock();
            this.sharedContext = null;
        }
        for (VertexConsumer consumer : this.touched) {
            ChunkVertexConsumerAccessor accessor = (ChunkVertexConsumerAccessor) consumer;
            if (accessor.offgrid$getModelBuilder() instanceof BlockSensitiveBufferBuilder context) {
                context.endBlock();
            } else {
                Vertices.tag(accessor.offgrid$getVertices(), (byte) 0, 0, 0, 0, 0);
            }
        }
        this.touched.clear();
        this.active = false;
    }

    private static final class Vertices {

        private static void tag(ChunkVertexEncoder.Vertex[] vertices, byte emission, int blockId, int x, int y, int z) {
            for (ChunkVertexEncoder.Vertex vertex : vertices) {
                if (vertex instanceof ChunkVertexExtension extension) {
                    extension.iris$setData(emission, (byte) 0, blockId, x, y, z);
                }
            }
        }
    }
}
//?}
