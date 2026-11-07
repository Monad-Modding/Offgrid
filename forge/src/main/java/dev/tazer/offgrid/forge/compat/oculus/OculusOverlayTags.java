package dev.tazer.offgrid.forge.compat.oculus;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.client.OverlayShaderTags;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class OculusOverlayTags extends OverlayShaderTags {

    private final List<BlockSensitiveBufferBuilder> touched = new ArrayList<>();

    private boolean active;
    private short blockId;
    private int localX;
    private int localY;
    private int localZ;

    @Override
    public void enter(OverlayGeometry.Entry entry) {
        Object2IntMap<BlockState> ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        this.active = ids != null;
        if (!this.active) return;
        BlockPos host = entry.host();
        this.blockId = (short) ids.getOrDefault(entry.state(), -1);
        this.localX = host.getX() & 15;
        this.localY = host.getY() & 15;
        this.localZ = host.getZ() & 15;
    }

    @Override
    public VertexConsumer tag(VertexConsumer consumer) {
        if (this.active && consumer instanceof BlockSensitiveBufferBuilder builder && !this.touched.contains(builder)) {
            builder.beginBlock(this.blockId, (short) -1, this.localX, this.localY, this.localZ);
            this.touched.add(builder);
        }
        return consumer;
    }

    @Override
    public void exit() {
        for (BlockSensitiveBufferBuilder builder : this.touched) {
            builder.endBlock();
        }
        this.touched.clear();
        this.active = false;
    }
}
