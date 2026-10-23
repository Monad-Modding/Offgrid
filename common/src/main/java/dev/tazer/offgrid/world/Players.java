package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
//? if <1.20.5 {
/*import net.minecraft.world.phys.Vec3;
*///?}

public final class Players {

    private Players() {
    }

    public static double reach(Player player) {
        //? if >=1.20.5 {
        return player.blockInteractionRange();
        //?} else {
        /*return player.isCreative() ? 5.0 : 4.5;
        *///?}
    }

    public static boolean canReach(Player player, BlockPos pos, double buffer) {
        //? if >=1.20.5 {
        return player.canInteractWithBlock(pos, buffer);
        //?} else {
        /*double range = reach(player) + buffer;
        return player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) < range * range;
        *///?}
    }
}
