package dev.tazer.offgrid.mixin.light;

import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Inject(method = "findBlockLightSources", at = @At("TAIL"))
    private void offgrid$overlayLightSources(BiConsumer<BlockPos, BlockState> output, CallbackInfo ci) {
        if (!((Object) this instanceof LevelChunk chunk)) return;
        OffgridChunk data = Services.PLATFORM.getChunkData(chunk);
        if (data == null) return;
        for (Long2ObjectMap.Entry<Cell> entry : data.getCells().long2ObjectEntrySet()) {
            int light = entry.getValue().light();
            if (light > 0) {
                output.accept(BlockPos.of(entry.getLongKey()), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, light));
            }
        }
    }
}
