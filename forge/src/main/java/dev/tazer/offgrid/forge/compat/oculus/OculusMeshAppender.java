package dev.tazer.offgrid.forge.compat.oculus;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.client.OverlayShaderTags;
import dev.tazer.offgrid.forge.client.ForgeOverlayGeometry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.compat.ccl.SinkingVertexBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.irisshaders.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.embeddedt.embeddium.api.MeshAppender;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Map;

public final class OculusMeshAppender extends OverlayShaderTags {

    private static final Vector3fc ORIGIN = new Vector3f();
    private static final ThreadLocal<Map<RenderType, SinkingVertexBuilder>> SINKS = ThreadLocal.withInitial(Reference2ReferenceOpenHashMap::new);

    private final ChunkBuildBuffers buffers;
    private final Map<RenderType, SinkingVertexBuilder> sinks = SINKS.get();

    @Nullable
    private OverlayGeometry.Entry entry;

    private OculusMeshAppender(ChunkBuildBuffers buffers) {
        this.buffers = buffers;
    }

    public static void render(MeshAppender.Context context) {
        BlockAndTintGetter region = context.blockRenderView();
        BlockPos origin = context.sectionOrigin().origin();
        List<OverlayGeometry.Entry> entries = OverlayGeometry.collect(region, origin);
        if (!((Object) context.sodiumBuildBuffers() instanceof ChunkBuildBuffersExt)) {
            ForgeOverlayGeometry.render(region, origin, entries, context.vertexConsumerProvider(), OverlayShaderTags.create());
            return;
        }
        OculusMeshAppender appender = new OculusMeshAppender(context.sodiumBuildBuffers());
        ForgeOverlayGeometry.render(region, origin, entries, appender::sink, appender);
    }

    private VertexConsumer sink(RenderType type) {
        return this.sinks.computeIfAbsent(type, key -> new SinkingVertexBuilder());
    }

    @Override
    public void enter(OverlayGeometry.Entry entry) {
        this.entry = entry;
        for (SinkingVertexBuilder sink : this.sinks.values()) {
            sink.reset();
        }
    }

    @Override
    public void exit() {
        OverlayGeometry.Entry current = this.entry;
        if (current == null) return;
        ChunkBuildBuffersExt context = (ChunkBuildBuffersExt) (Object) this.buffers;
        BlockPos host = current.host();
        context.iris$setLocalPos(host.getX() & 15, host.getY() & 15, host.getZ() & 15);
        context.iris$setMaterialId(current.state(), (short) -1, (byte) current.state().getLightEmission());
        for (Map.Entry<RenderType, SinkingVertexBuilder> sink : this.sinks.entrySet()) {
            Material material = DefaultMaterials.forRenderLayer(sink.getKey());
            sink.getValue().flush(this.buffers.get(material), material, ORIGIN);
            sink.getValue().reset();
        }
        context.iris$resetBlockContext();
        this.entry = null;
    }
}
