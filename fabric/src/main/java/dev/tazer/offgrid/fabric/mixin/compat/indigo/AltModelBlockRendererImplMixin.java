//? if >=26 {
/*package dev.tazer.offgrid.fabric.mixin.compat.indigo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.Offsets;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("fabric-renderer-indigo")
@Mixin(AltModelBlockRendererImpl.class)
public abstract class AltModelBlockRendererImplMixin {

    @Shadow
    private BlockState blockState;

    @ModifyExpressionValue(method = "tesselateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 offgrid$offsetBlock(Vec3 original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        if (Offsets.isSuppressed()) return original;
        Vec3 offset = Offsets.of(level, pos, state);
        return offset == null ? original : original.add(offset);
    }

    @WrapOperation(method = "shouldCullFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$cullAgainstBlocklogged(BlockAndTintGetter level, BlockPos neighbour, Operation<BlockState> original) {
        return Connections.forCulling(this.blockState, level, neighbour, original.call(level, neighbour));
    }
}
*///?}
