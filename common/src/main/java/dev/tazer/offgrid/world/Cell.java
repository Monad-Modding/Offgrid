package dev.tazer.offgrid.world;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record Cell(@Nullable Vec3 offset, List<Overlay> overlays, int light) {

    public static final Cell EMPTY = new Cell(null, List.of(), 0);

    public static Cell of(@Nullable Vec3 offset, List<Overlay> overlays) {
        if (offset == null && overlays.isEmpty()) return EMPTY;
        int light = 0;
        for (Overlay overlay : overlays) {
            light = Math.max(light, overlay.getState().getLightEmission());
        }
        return new Cell(offset, List.copyOf(overlays), light);
    }

    public boolean isEmpty() {
        return this.offset == null && this.overlays.isEmpty();
    }

    public boolean hasOverlays() {
        return !this.overlays.isEmpty();
    }

    @Nullable
    public Overlay find(int id) {
        for (Overlay overlay : this.overlays) {
            if (overlay.getId() == id) return overlay;
        }
        return null;
    }

    public Cell withOffset(@Nullable Vec3 offset) {
        return of(offset, this.overlays);
    }

    public Cell with(Overlay overlay) {
        List<Overlay> next = new ArrayList<>(this.overlays.size() + 1);
        next.addAll(this.overlays);
        next.add(overlay);
        return of(this.offset, next);
    }

    public Cell without(Overlay overlay) {
        List<Overlay> next = new ArrayList<>(this.overlays.size());
        for (Overlay existing : this.overlays) {
            if (existing != overlay) {
                next.add(existing);
            }
        }
        return of(this.offset, next);
    }

    public Cell relit() {
        return of(this.offset, this.overlays);
    }
}
