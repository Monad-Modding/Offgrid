package dev.tazer.offgrid.client;

import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayHitResult;
import dev.tazer.offgrid.world.OverlayShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class OverlayOutline {

    private OverlayOutline() {
    }

    @Nullable
    public static VoxelShape shape(BlockGetter level, BlockPos pos) {
        Overlay overlay = target(level, pos);
        return overlay == null ? null : OverlayShapes.of((Level) level, pos, overlay);
    }

    @Nullable
    public static Overlay target(BlockGetter level, BlockPos pos) {
        if (!(Minecraft.getInstance().hitResult instanceof OverlayHitResult hit) || !hit.getBlockPos().equals(pos) || !(level instanceof Level real)) return null;
        return Cells.find(real, pos, hit.getOverlayId());
    }
}
