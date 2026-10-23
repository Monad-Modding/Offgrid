//? if <26 {
package dev.tazer.offgrid.compat.create;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface OverlayPrinter {

    @Nullable
    Boolean offgrid$shouldPrintOverlay(Level level);
}
//?}
