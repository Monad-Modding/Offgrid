package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class OverlayHitResult extends BlockHitResult {

    private final int overlayId;

    public OverlayHitResult(Vec3 location, Direction direction, BlockPos host, int overlayId) {
        super(location, direction, host, false);
        this.overlayId = overlayId;
    }

    public int getOverlayId() {
        return this.overlayId;
    }
}
