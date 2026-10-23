package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//? if >=26 {
/*import net.minecraft.world.entity.decoration.painting.Painting;
*///?} else {
import net.minecraft.world.entity.decoration.Painting;
//?}

public final class FlatPaintings {

    public static final String FACE_KEY = "offgrid:face";
    public static final String TOP_KEY = "offgrid:top";
    private static final double WALL_GAP = 0.46875;
    private static final double DEPTH = 0.0625;

    private FlatPaintings() {
    }

    @Nullable
    public static Direction placingTop() {
        UseOnContext context = SurfaceMounts.placingContext();
        if (context == null) return null;
        Direction facing = context.getHorizontalDirection();
        return context.getClickedFace() == Direction.DOWN ? facing.getOpposite() : facing;
    }

    public static boolean isFlatCandidate(Entity entity) {
        return entity instanceof Painting;
    }

    public static boolean isFlat(Entity entity) {
        if (!(entity instanceof Painting)) return false;
        Direction direction = entity.getDirection();
        return direction != null && direction.getAxis().isVertical();
    }

    public static Direction top(Entity entity) {
        return Direction.fromYRot(entity.getYRot());
    }

    public static Direction right(Direction face, Direction top) {
        return face == Direction.UP ? top.getCounterClockWise() : top.getClockWise();
    }

    public static int width(Painting painting) {
        //? if >=1.21 {
        return painting.getVariant().value().width();
        //?} else {
        /*return Math.max(1, painting.getVariant().value().getWidth() / 16);
        *///?}
    }

    public static int height(Painting painting) {
        //? if >=1.21 {
        return painting.getVariant().value().height();
        //?} else {
        /*return Math.max(1, painting.getVariant().value().getHeight() / 16);
        *///?}
    }

    public static AABB box(BlockPos pos, Direction face, Direction top, int width, int height) {
        Direction right = right(face, top);
        double widthShift = width % 2 == 0 ? 0.5 : 0.0;
        double heightShift = height % 2 == 0 ? 0.5 : 0.0;
        double x = pos.getX() + 0.5 - face.getStepX() * WALL_GAP - right.getStepX() * widthShift + top.getStepX() * heightShift;
        double y = pos.getY() + 0.5 - face.getStepY() * WALL_GAP;
        double z = pos.getZ() + 0.5 - face.getStepZ() * WALL_GAP - right.getStepZ() * widthShift + top.getStepZ() * heightShift;
        double sizeX = right.getAxis() == Direction.Axis.X ? width : height;
        double sizeZ = right.getAxis() == Direction.Axis.Z ? width : height;
        return AABB.ofSize(new Vec3(x, y, z), sizeX, DEPTH, sizeZ);
    }

    public static BlockPos lightPos(Entity painting, int width, int height, int column, int row) {
        Direction top = top(painting);
        Direction right = right(painting.getDirection(), top);
        double across = column - width / 2.0 + 0.5;
        double along = row - height / 2.0 + 0.5;
        return BlockPos.containing(painting.getX() + right.getStepX() * across + top.getStepX() * along, painting.getY(), painting.getZ() + right.getStepZ() * across + top.getStepZ() * along);
    }
}
