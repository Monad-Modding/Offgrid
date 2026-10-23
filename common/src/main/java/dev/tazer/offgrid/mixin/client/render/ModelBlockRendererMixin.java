//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.world.Offsets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    @ModifyExpressionValue(method = "tesselateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 offgrid$offsetBlock(Vec3 original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        if (Offsets.isSuppressed()) return original;
        Vec3 offset = Offsets.of(level, pos, state);
        return offset == null ? original : original.add(offset);
    }
}
*///?}
