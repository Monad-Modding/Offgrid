package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Layers;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    //? if >=1.21.5 {
    /*@ModifyReturnValue(method = "setBlockState", at = @At("RETURN"))
    private @Nullable BlockState offgrid$validateOffgrid(@Nullable BlockState previous, BlockPos pos, BlockState state, int flags) {
        if (previous == null) return null;
        Level level = ((LevelChunk) (Object) this).getLevel();
        Layers.onReplaced(level, pos, previous, state);
        if (level instanceof ServerLevel serverLevel) {
            Overlays.onBlockChanged(serverLevel, pos, previous, state);
        }
        return previous;
    }
    *///?} else {
    @ModifyReturnValue(method = "setBlockState", at = @At("RETURN"))
    private @Nullable BlockState offgrid$validateOffgrid(@Nullable BlockState previous, BlockPos pos, BlockState state, boolean moving) {
        if (previous == null) return null;
        Level level = ((LevelChunk) (Object) this).getLevel();
        Layers.onReplaced(level, pos, previous, state);
        if (level instanceof ServerLevel serverLevel) {
            Overlays.onBlockChanged(serverLevel, pos, previous, state);
        }
        return previous;
    }
    //?}
}
