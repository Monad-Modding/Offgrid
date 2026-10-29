//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SubLevels {

    private SubLevels() {
    }

    public static Vec3 project(Level level, Vec3 pos) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, pos);
    }

    public static Vec3 toLocal(Level level, BlockPos pos, Vec3 point) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, (Vec3i) pos);
        return subLevel == null ? point : subLevel.logicalPose().transformPositionInverse(point);
    }

    public static boolean contains(Level level, BlockPos pos) {
        return SableCompanion.INSTANCE.getContaining(level, (Vec3i) pos) != null;
    }

    public static double distanceSqr(Level level, Vec3 first, Vec3 second) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, first, second);
    }
}
//?}
