//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.compat.sable;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public final class SubLevelRenders {

    private SubLevelRenders() {
    }

    public static boolean isOnSubLevel(BlockPos host) {
        return SableCompanion.INSTANCE.getContainingClient(host) != null;
    }

    public static boolean enter(PoseStack poseStack, Vec3 camera, BlockPos host, Vec3 offset, float partialTick) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(host);
        if (subLevel == null) return false;
        Pose3dc pose = subLevel.renderPose(partialTick);
        Vector3dc position = pose.position();
        Vector3dc pivot = pose.rotationPoint();
        Vector3dc scale = pose.scale();
        poseStack.translate(position.x() - camera.x, position.y() - camera.y, position.z() - camera.z);
        poseStack.mulPose(new Quaternionf(pose.orientation()));
        poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());
        poseStack.translate(host.getX() + offset.x - pivot.x(), host.getY() + offset.y - pivot.y(), host.getZ() + offset.z - pivot.z());
        return true;
    }
}
//?}
