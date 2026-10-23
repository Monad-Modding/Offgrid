package dev.tazer.offgrid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
//? if >=1.21 && <1.21.2 {
import dev.tazer.offgrid.compat.sable.SubLevelRenders;
import dev.tazer.offgrid.world.SubLevelSpace;
//?}

public final class SubLevelView {

    private SubLevelView() {
    }

    public static boolean contains(BlockPos pos) {
        //? if >=1.21 && <1.21.2 {
        return SubLevelSpace.isActive() && SubLevelRenders.isOnSubLevel(pos);
        //?} else {
        /*return false;
        *///?}
    }

    public static void enter(PoseStack poseStack, Vec3 camera, BlockPos host, Vec3 offset, float partialTick) {
        //? if >=1.21 && <1.21.2 {
        if (SubLevelSpace.isActive() && SubLevelRenders.enter(poseStack, camera, host, offset, partialTick)) return;
        //?}
        poseStack.translate(host.getX() - camera.x + offset.x, host.getY() - camera.y + offset.y, host.getZ() - camera.z + offset.z);
    }
}
