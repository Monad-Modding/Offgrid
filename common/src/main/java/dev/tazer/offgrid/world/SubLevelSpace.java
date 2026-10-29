package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
//? if >=1.21 && <1.21.2 {
import dev.tazer.offgrid.compat.Integrations;
import dev.tazer.offgrid.compat.sable.SubLevels;
//?}

public final class SubLevelSpace {

    private SubLevelSpace() {
    }

    public static boolean isActive() {
        //? if >=1.21 && <1.21.2 {
        return Integrations.isLoaded(Integrations.SABLE);
        //?} else {
        /*return false;
        *///?}
    }

    public static Vec3 project(Level level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos);
        //? if >=1.21 && <1.21.2 {
        return isActive() ? SubLevels.project(level, centre) : centre;
        //?} else {
        /*return centre;
        *///?}
    }

    public static Vec3 toLocal(Level level, BlockPos pos, Vec3 point) {
        //? if >=1.21 && <1.21.2 {
        return isActive() ? SubLevels.toLocal(level, pos, point) : point;
        //?} else {
        /*return point;
        *///?}
    }

    public static double distanceSqr(Level level, BlockPos pos, Vec3 point) {
        Vec3 centre = Vec3.atCenterOf(pos);
        //? if >=1.21 && <1.21.2 {
        return isActive() ? SubLevels.distanceSqr(level, centre, point) : centre.distanceToSqr(point);
        //?} else {
        /*return centre.distanceToSqr(point);
        *///?}
    }
}
