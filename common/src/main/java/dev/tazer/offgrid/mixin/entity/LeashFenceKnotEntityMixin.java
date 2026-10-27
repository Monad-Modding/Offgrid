package dev.tazer.offgrid.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin {

    @ModifyReturnValue(method = "survives", at = @At("RETURN"))
    private boolean offgrid$survivesOnOverlayFence(boolean original) {
        if (original) return true;
        LeashFenceKnotEntity self = (LeashFenceKnotEntity) (Object) this;
        for (Overlay overlay : Cells.overlays(self.level(), self.getPos())) {
            if (overlay.getState().is(BlockTags.FENCES)) return true;
        }
        return false;
    }
}
