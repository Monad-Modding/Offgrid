//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sable")
@Mixin(LevelPlot.class)
public abstract class LevelPlotMixin {

    @WrapOperation(method = "onRemove", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/plot/LevelPlot;onRemoveChunkHolder(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void offgrid$untrackChunk(LevelPlot plot, LevelChunk chunk, Operation<Void> original) {
        OffgridEvents.onChunkUnload(chunk.getLevel(), chunk);
        original.call(plot, chunk);
    }
}
//?}
