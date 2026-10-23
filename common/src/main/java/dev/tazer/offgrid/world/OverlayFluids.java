package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class OverlayFluids {

    private OverlayFluids() {
    }

    public static FluidState around(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable BlockPos changed, @Nullable FluidState changedFluid) {
        VoxelShape shape = OverlayShapes.of(level, host, state, offset, null);
        AABB bounds = shape.isEmpty() ? new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).move(offset.x, offset.y, offset.z) : shape.bounds();
        FluidState found = Fluids.EMPTY.defaultFluidState();
        BlockPos min = BlockPos.containing(host.getX() + bounds.minX + Support.EPSILON, host.getY() + bounds.minY + Support.EPSILON, host.getZ() + bounds.minZ + Support.EPSILON);
        BlockPos max = BlockPos.containing(host.getX() + bounds.maxX - Support.EPSILON, host.getY() + bounds.maxY - Support.EPSILON, host.getZ() + bounds.maxZ - Support.EPSILON);
        for (BlockPos cell : BlockPos.betweenClosed(min, max)) {
            FluidState here;
            if (changed != null && changedFluid != null && cell.equals(changed)) {
                here = changedFluid;
            } else if (level.hasChunkAt(cell)) {
                here = Cells.realState(level, cell).getFluidState();
            } else {
                continue;
            }
            if (!here.isEmpty() && (found.isEmpty() || !here.is(FluidTags.WATER))) {
                found = here;
            }
        }
        return found;
    }

    public static boolean holds(BlockState state, FluidState fluid) {
        return fluid.isEmpty() || state.getFluidState().getType().isSame(fluid.getType());
    }

    public static void settle(ServerLevel level, BlockPos host, Overlay overlay, FluidState fluid) {
        BlockState state = overlay.getState();
        if (holds(state, fluid)) return;
        OverlayContext.run(level, host, overlay, () -> {
            //? if >=1.20.2 {
            boolean contains = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, host, state, fluid.getType());
            //?} else {
            /*boolean contains = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(level, host, state, fluid.getType());
            *///?}
            if (contains) {
                ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, host, state, fluid);
            } else {
                level.destroyBlock(host, true);
            }
        });
    }
}
