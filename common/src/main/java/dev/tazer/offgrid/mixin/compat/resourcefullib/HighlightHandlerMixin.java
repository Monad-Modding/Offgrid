//? if <26.3 {
package dev.tazer.offgrid.mixin.compat.resourcefullib;

import com.teamresourceful.resourcefullib.client.highlights.HighlightHandler;
import dev.tazer.offgrid.client.OverlayOutline;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Blocklogging;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.Scope;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26 {
/*import com.teamresourceful.resourcefullib.client.highlights.HighlightRenderState;
import dev.tazer.offgrid.world.Offsets;
import org.spongepowered.asm.mixin.Unique;
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
//?}

@Pseudo
@IfModPresent("resourcefullib")
@Mixin(value = HighlightHandler.class, priority = 900)
public abstract class HighlightHandlerMixin {

    //? if >=26 {
    /*@Inject(method = "extractState", at = @At("HEAD"), cancellable = true, remap = false)
    private static void offgrid$offgridHighlight(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<HighlightRenderState> cir) {
        if (Scope.get(Scope.HIGHLIGHTING)) return;
        Overlay overlay = OverlayOutline.target(level, pos);
        if (overlay != null) {
            if (Blocklogging.at(level, pos, overlay) != null) {
                cir.setReturnValue(null);
                return;
            }
            HighlightRenderState highlight = Scope.with(Scope.HIGHLIGHTING, Boolean.TRUE, () -> OverlayContext.call(level, pos, overlay, () -> HighlightHandler.extractState(level, pos, overlay.getState())));
            cir.setReturnValue(highlight == null ? null : offgrid$shifted(highlight, overlay.getOffset()));
            return;
        }
        Vec3 offset = Offsets.of(level, pos, state);
        if (offset == null) return;
        HighlightRenderState highlight = Scope.with(Scope.HIGHLIGHTING, Boolean.TRUE, () -> HighlightHandler.extractState(level, pos, state));
        cir.setReturnValue(highlight == null ? null : offgrid$shifted(highlight, offset));
    }

    @Unique
    private static HighlightRenderState offgrid$shifted(HighlightRenderState highlight, Vec3 offset) {
        Vec3 shifted = highlight.offset().add(offset);
        if (highlight instanceof HighlightRenderState.Dynamic dynamic) return new HighlightRenderState.Dynamic(dynamic.highlight(), shifted);
        return new HighlightRenderState.Cached(((HighlightRenderState.Cached) highlight).data(), shifted);
    }
    *///?} else {
    @Inject(method = "onBlockHighlight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void offgrid$overlayHighlight(Vec3 cameraPos, Entity cameraEntity, PoseStack stack, BlockPos pos, BlockState state, VertexConsumer consumer, CallbackInfoReturnable<Boolean> cir) {
        if (Scope.get(Scope.HIGHLIGHTING)) return;
        Level level = cameraEntity.level();
        Overlay overlay = OverlayOutline.target(level, pos);
        if (overlay == null) return;
        if (Blocklogging.at(level, pos, overlay) != null) {
            cir.setReturnValue(false);
            return;
        }
        Vec3 offset = overlay.getOffset();
        stack.pushPose();
        stack.translate(offset.x, offset.y, offset.z);
        boolean drawn = Scope.with(Scope.HIGHLIGHTING, Boolean.TRUE, () -> OverlayContext.call(level, pos, overlay, () -> HighlightHandler.onBlockHighlight(cameraPos, cameraEntity, stack, pos, overlay.getState(), consumer)));
        stack.popPose();
        cir.setReturnValue(drawn);
    }
    //?}
}
//?}
