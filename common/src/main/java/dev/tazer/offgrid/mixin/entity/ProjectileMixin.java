package dev.tazer.offgrid.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayHitResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @WrapMethod(method = "onHitBlock")
    private void offgrid$hitOverlay(BlockHitResult result, Operation<Void> original) {
        Level level = ((Projectile) (Object) this).level();
        Overlay overlay = result instanceof OverlayHitResult hit ? Cells.find(level, hit.getBlockPos(), hit.getOverlayId()) : null;
        if (overlay == null) {
            original.call(result);
            return;
        }
        OverlayContext.run(level, result.getBlockPos(), overlay, () -> original.call(result));
    }
}
