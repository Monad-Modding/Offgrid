//? if <26 {
package dev.tazer.offgrid.neoforge.mixin.compat.gallery;

import com.teamabnormals.gallery.core.other.GalleryEvents;
import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@IfModPresent("gallery")
@Mixin(GalleryEvents.class)
public abstract class GalleryEventsMixin {

    @Inject(method = "rightClickWithPainting(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickBlock;)V", at = @At("HEAD"), cancellable = true)
    private static void offgrid$placeOnFloorsAndCeilings(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        Direction face = event.getFace();
        if (face != null && face.getAxis().isVertical() && OffgridSettings.of(event.getLevel()).anyFacePaintings()) {
            ci.cancel();
        }
    }
}
//?}
