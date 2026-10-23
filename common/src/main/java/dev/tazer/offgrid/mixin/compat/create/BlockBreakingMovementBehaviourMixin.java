//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@IfModPresent("create")
@Mixin(BlockBreakingMovementBehaviour.class)
public abstract class BlockBreakingMovementBehaviourMixin {

    @Shadow(remap = false)
    public abstract boolean canBreak(Level level, BlockPos pos, BlockState state);

    @Inject(method = "visitNewPosition", at = @At("HEAD"), remap = false)
    private void offgrid$breakOverlaysInEmptyCell(MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (!(context.world instanceof ServerLevel level) || !Cells.hasOverlays(level, pos)) return;
        BlockState real = Cells.realState(level, pos);
        if (real.isAir() || real.canBeReplaced()) {
            this.offgrid$harvest(context, level, pos);
        }
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"), remap = false)
    private void offgrid$breakOverlaysWithHost(MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (context.world instanceof ServerLevel level && Cells.hasOverlays(level, pos)) {
            this.offgrid$harvest(context, level, pos);
        }
    }

    @Unique
    private void offgrid$harvest(MovementContext context, ServerLevel level, BlockPos pos) {
        Overlays.harvest(level, pos, state -> this.canBreak(level, pos, state), stack -> ((MovementBehaviour) (Object) this).dropItem(context, stack));
    }
}
//?}
