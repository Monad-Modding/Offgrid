package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.OffgridTags;
import dev.tazer.offgrid.compat.portals.ImmersivePortals;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.SurfaceOffsetSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import net.minecraft.world.entity.decoration.painting.Painting;
*///?} else {
import net.minecraft.world.entity.decoration.Painting;
//?}

import java.util.List;
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
        if (entity instanceof HangingEntity hanging) {
            Scope.with(Scope.MOUNTING, Boolean.TRUE, () -> mountHanging(hanging, mounted, context));
            return;
        }
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
    public static boolean isMounting() {
        return Scope.get(Scope.MOUNTING);
    }

    @Nullable
    public static Boolean survivesOverride(HangingEntity entity) {
        if (!(entity instanceof SurfaceMounted mounted) || !OffgridSettings.of(entity.level()).surfaceAttaching()) return null;
        UseOnContext placing = placingContext();
        if (placing != null && !isMounting() && entity.getPos().equals(placing.getClickedPos().relative(placing.getClickedFace()))) return Scope.with(Scope.MOUNTING, Boolean.TRUE, () -> mountHanging(entity, mounted, placing));
        return survives(entity);
    }

    private static boolean mountHanging(HangingEntity hanging, SurfaceMounted mounted, UseOnContext context) {
        mounted.offgrid$setSurfaceOffset(Vec3.ZERO);
        List<Vec3> offsets = Surfaces.hangingOffsetsFor(context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getClickLocation(), hanging.getBoundingBox(), hanging instanceof Painting);
        for (Vec3 offset : offsets) {
            mounted.offgrid$setSurfaceOffset(offset);
            if (survives(hanging)) return true;
        }
        mounted.offgrid$setSurfaceOffset(Vec3.ZERO);
        return false;
    }

    public static boolean survives(HangingEntity entity) {
        Level level = entity.level();
        if (!level.noCollision(entity)) return false;
        Direction facing = entity.getDirection();
        AABB box = entity.getBoundingBox();
        if (entity instanceof Painting ? !Surfaces.covers(level, facing, box) : !Surfaces.touches(level, facing, box)) return false;
        return level.getEntities(entity, box, other -> other instanceof HangingEntity).isEmpty();
    }

    public static void sendTo(ServerPlayer player, Entity entity) {
        if (!(entity instanceof SurfaceMounted mounted)) return;
        Vec3 offset = mounted.offgrid$getSurfaceOffset();
        if (Overlays.isZero(offset)) return;
        SurfaceOffsetSync packet = new SurfaceOffsetSync(entity.getId(), offset);
        if (entity.level() instanceof ServerLevel entityLevel) {
            ImmersivePortals.sending(entityLevel, player, () -> OffgridNetwork.sendToPlayer(player, OffgridNetwork.SURFACE, packet));
        } else {
            OffgridNetwork.sendToPlayer(player, OffgridNetwork.SURFACE, packet);
        }
    }
}
