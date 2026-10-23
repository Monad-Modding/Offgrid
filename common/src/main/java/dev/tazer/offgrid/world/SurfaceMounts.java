package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.OffgridTags;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.SurfaceOffsetSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class SurfaceMounts {

    public static final String KEY = "offgrid:surface_offset";

    private SurfaceMounts() {
    }

    public static <T> T placing(UseOnContext context, Supplier<T> use) {
        return Scope.with(Scope.PLACING, context, use);
    }

    @Nullable
    public static UseOnContext placingContext() {
        return Scope.get(Scope.PLACING);
    }

    public static void mountPlaced(Entity entity) {
        UseOnContext context = Scope.get(Scope.PLACING);
        if (context == null || !(entity instanceof SurfaceMounted mounted) || !entity.getType().builtInRegistryHolder().is(OffgridTags.SURFACE_ATTACHING)) return;
        if (!Overlays.isZero(mounted.offgrid$getSurfaceOffset())) return;
        mount(entity, context);
    }

    public static void mount(Entity entity, UseOnContext context) {
        if (!(entity instanceof SurfaceMounted mounted) || !OffgridSettings.of(context.getLevel()).surfaceAttaching()) return;
        boolean vanillaFits = !(entity instanceof HangingEntity hanging) || hanging.survives();
        mounted.offgrid$setSurfaceOffset(Surfaces.mountOffsetFor(context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getClickLocation(), entity.getBoundingBox().getCenter(), vanillaFits));
    }

    //? if <1.21 {
    /*public static void shift(Entity entity) {
        if (!(entity instanceof SurfaceMounted mounted) || entity.getDirection() == null) return;
        Vec3 offset = mounted.offgrid$getSurfaceOffset();
        if (offset == null || Overlays.isZero(offset)) return;
        entity.setBoundingBox(entity.getBoundingBox().move(offset));
        entity.setPosRaw(entity.getX() + offset.x, entity.getY() + offset.y, entity.getZ() + offset.z);
    }

    *///?}
    public static boolean isShifted(Entity entity) {
        return entity instanceof SurfaceMounted mounted && !Overlays.isZero(mounted.offgrid$getSurfaceOffset());
    }

    public static boolean survives(HangingEntity entity) {
        Level level = entity.level();
        if (!level.noCollision(entity)) return false;
        Direction facing = entity.getDirection();
        Direction.Axis axis = facing.getAxis();
        AABB box = entity.getBoundingBox();
        Vec3 centre = box.getCenter();
        double back = facing.getAxisDirection().getStep() > 0 ? box.min(axis) : box.max(axis);
        Vec3 point = switch (axis) {
            case X -> new Vec3(back, centre.y, centre.z);
            case Y -> new Vec3(centre.x, back, centre.z);
            case Z -> new Vec3(centre.x, centre.y, back);
        };
        if (!Surfaces.supports(level, point, facing)) return false;
        return level.getEntities(entity, box, other -> other instanceof HangingEntity).isEmpty();
    }

    public static void sendTo(ServerPlayer player, Entity entity) {
        if (!(entity instanceof SurfaceMounted mounted)) return;
        Vec3 offset = mounted.offgrid$getSurfaceOffset();
        if (!Overlays.isZero(offset)) {
            OffgridNetwork.sendToPlayer(player, OffgridNetwork.SURFACE, new SurfaceOffsetSync(entity.getId(), offset));
        }
    }
}
