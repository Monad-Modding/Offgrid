//? if <1.21.4 {
package dev.tazer.offgrid.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayHitResult;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @WrapMethod(method = "pickBlock")
    private void offgrid$pickOverlay(Operation<Void> original) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.level == null || !(minecraft.hitResult instanceof OverlayHitResult hit)) {
            original.call();
            return;
        }
        Overlay overlay = Cells.find(minecraft.level, hit.getBlockPos(), hit.getOverlayId());
        if (overlay == null) {
            original.call();
            return;
        }
        overlay.materialize(minecraft.level, hit.getBlockPos());
        OverlayContext.run(minecraft.level, hit.getBlockPos(), overlay, original::call);
    }
}
//?}
