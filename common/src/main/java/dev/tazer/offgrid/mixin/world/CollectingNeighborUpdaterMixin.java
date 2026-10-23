package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.OverlayContext;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CollectingNeighborUpdater.class)
public abstract class CollectingNeighborUpdaterMixin {

    @WrapMethod(method = "runUpdates")
    private void offgrid$updateRealBlocks(Operation<Void> original) {
        OverlayContext.runOutside(original::call);
    }
}
