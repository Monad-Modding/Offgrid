//? if <1.21 {
/*package dev.tazer.offgrid.fabric.compat.iris;

import net.irisshaders.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class IrisBlockContext {

    private IrisBlockContext() {
    }

    public static void begin(Object buffers, BlockState state, BlockPos host) {
        if (!(buffers instanceof ChunkBuildBuffersExt context)) return;
        context.iris$setLocalPos(host.getX() & 15, host.getY() & 15, host.getZ() & 15);
        context.iris$setMaterialId(state, (short) -1, (byte) state.getLightEmission());
    }

    public static void end(Object buffers) {
        if (buffers instanceof ChunkBuildBuffersExt context) {
            context.iris$resetBlockContext();
        }
    }
}
*///?}
