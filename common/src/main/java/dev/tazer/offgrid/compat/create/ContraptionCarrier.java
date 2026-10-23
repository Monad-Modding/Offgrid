//? if <26 {
package dev.tazer.offgrid.compat.create;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ContraptionCarrier {

    @Nullable
    CarriedCells offgrid$getCarried();

    Vec3 offgrid$toGlobal(Vec3 local);

    Vec3 offgrid$toLocal(Vec3 global);

    @Nullable
    BlockHitResult offgrid$getLastHit();

    void offgrid$setLastHit(@Nullable BlockHitResult hit);
}
//?}
