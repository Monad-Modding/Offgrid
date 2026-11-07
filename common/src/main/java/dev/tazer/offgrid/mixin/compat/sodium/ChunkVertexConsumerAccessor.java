//? if >=1.21 {
package dev.tazer.offgrid.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkVertexConsumer;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@IfModPresent("sodium")
@Mixin(value = ChunkVertexConsumer.class, remap = false)
public interface ChunkVertexConsumerAccessor {

    @Accessor("modelBuilder")
    ChunkModelBuilder offgrid$getModelBuilder();

    @Accessor("vertices")
    ChunkVertexEncoder.Vertex[] offgrid$getVertices();
}
//?}
