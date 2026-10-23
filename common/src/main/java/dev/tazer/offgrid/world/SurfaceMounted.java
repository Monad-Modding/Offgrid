package dev.tazer.offgrid.world;

import net.minecraft.world.phys.Vec3;

public interface SurfaceMounted {

    Vec3 offgrid$getSurfaceOffset();

    void offgrid$setSurfaceOffset(Vec3 offset);
}
