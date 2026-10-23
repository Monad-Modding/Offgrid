package dev.tazer.offgrid.world;

import org.jetbrains.annotations.Nullable;

public interface OverlayBlockEntity {

    @Nullable
    Overlay offgrid$getOverlay();

    void offgrid$setOverlay(@Nullable Overlay overlay);
}
