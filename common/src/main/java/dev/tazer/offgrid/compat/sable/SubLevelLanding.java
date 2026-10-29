//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.compat.sable;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;

public final class SubLevelLanding {
    private static final Deque<SubLevelLanding> ACTIVE = new ArrayDeque<>();

    private final ServerLevel target;
    private final SubLevelAssemblyHelper.AssemblyTransform transform;
    private final Map<BlockPos, AssembledCell> cells;
    private final Collection<BlockPos> moved;
    private final Map<BlockPos, Direction.Axis> slabs;

    private boolean landed;

    public SubLevelLanding(SubLevelAssemblyHelper.AssemblyTransform transform, Map<BlockPos, AssembledCell> cells, Collection<BlockPos> moved, Map<BlockPos, Direction.Axis> slabs) {
        this.target = transform.getLevel();
        this.transform = transform;
        this.cells = cells;
        this.moved = moved;
        this.slabs = slabs;
    }

    public static void begin(SubLevelLanding landing) {
        ACTIVE.push(landing);
    }

    public static void end() {
        ACTIVE.pop();
    }

    public static void landActive() {
        SubLevelLanding landing = ACTIVE.peek();
        if (landing != null) {
            landing.land();
        }
    }

    public void land() {
        if (this.landed) return;
        this.landed = true;
        Rotation rotation = this.transform.getRotation();
        AssembledCell.landAll(this.target, this.cells, this.moved, this.transform::apply, state -> state.rotate(rotation), offset -> AssembledCell.rotate(offset, rotation));
        for (Map.Entry<BlockPos, Direction.Axis> slab : this.slabs.entrySet()) {
            MixedSlabs.rotate(this.target, slab.getKey(), slab.getValue(), rotation);
        }
    }
}
//?}
