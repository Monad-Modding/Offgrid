//? if <26 {
package dev.tazer.offgrid.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface OverlayPrinter {

    void offgrid$setBusy(boolean busy);

    @Nullable
    Vec3 offgrid$offsetAt(BlockPos pos);

    boolean offgrid$takePrintedOverlay();
}
//?}
