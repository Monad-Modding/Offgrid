//? if >=26 {
/*package dev.tazer.offgrid.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.Overlays;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sodium")
@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin extends AbstractBlockRenderContext {

    @ModifyExpressionValue(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasOffsetFunction()Z"))
    private boolean offgrid$offsetBlocks(boolean original, @Local(argsOnly = true) BlockState state) {
        return original || Overlays.canOffset(state);
    }

    @ModifyExpressionValue(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 offgrid$offsetBlock(Vec3 original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true, ordinal = 0) BlockPos pos) {
        if (Offsets.isSuppressed()) return original;
        Vec3 offset = Offsets.of(this.level, pos, state);
        return offset == null ? original : original.add(offset);
    }
}
*///?} else if >=1.21 {
package dev.tazer.offgrid.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Overlays;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sodium")
@Mixin(BlockRenderer.class)
public class BlockRendererMixin {

    @ModifyExpressionValue(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasOffsetFunction()Z"))
    private boolean offgrid$offsetBlocks(boolean original, @Local(argsOnly = true) BlockState state) {
        return original || Overlays.canOffset(state);
    }
}
//?}
