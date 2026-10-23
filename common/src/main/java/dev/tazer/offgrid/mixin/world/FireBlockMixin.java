package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.mixin.access.FireBlockAccessor;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Inject(method = "checkBurnOut", at = @At("HEAD"))
    private void offgrid$burnOverlays(CallbackInfo ci, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true, ordinal = 0) int chance, @Local(argsOnly = true) RandomSource random) {
        if (!(level instanceof ServerLevel server) || !Cells.hasOverlays(level, pos)) return;
        for (Overlay overlay : Cells.overlays(level, pos)) {
            int odds = ((FireBlockAccessor) this).offgrid$getBurnOdds(overlay.getState());
            if (odds > 0 && random.nextInt(chance) < odds) {
                Overlays.remove(server, pos, overlay, true);
            }
        }
    }
}
