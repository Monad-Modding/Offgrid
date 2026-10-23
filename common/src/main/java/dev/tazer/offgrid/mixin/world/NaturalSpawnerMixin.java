package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @ModifyReturnValue(method = "isValidEmptySpawnBlock", at = @At("RETURN"))
    private static boolean offgrid$overlaysBlockSpawns(boolean original, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid, EntityType<?> type) {
        if (!original) return false;
        for (Overlay overlay : Cells.overlays(level, pos)) {
            BlockState overlayState = overlay.getState();
            if (overlayState.isSignalSource() || overlayState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) || type.isBlockDangerous(overlayState)) return false;
        }
        return true;
    }
}
