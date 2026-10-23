//? if <26 {
package dev.tazer.offgrid.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CarriedOverlayHit extends BlockHitResult {

    private final int index;

    public CarriedOverlayHit(Vec3 location, Direction direction, BlockPos pos, int index) {
        super(location, direction, pos, false);
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }
}
//?}
