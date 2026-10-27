package dev.tazer.offgrid.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.Layers;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayShapes;
import dev.tazer.offgrid.world.Overlays;
import dev.tazer.offgrid.world.Placement;
import dev.tazer.offgrid.world.Signals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.2 {
/*import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
*///?}

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow
    protected abstract BlockState asState();

    @ModifyReturnValue(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
    private VoxelShape offgrid$offsetShape(VoxelShape original, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (original.isEmpty()) return original;
        Vec3 offset = Offsets.of(level, pos, this.asState());
        return offset == null ? original : original.move(offset.x, offset.y, offset.z);
    }

    @ModifyReturnValue(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
    private VoxelShape offgrid$offgridCollision(VoxelShape original, BlockGetter level, BlockPos pos, CollisionContext context) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return original;
        Cell cell = index.getCell(pos.getX(), pos.getY(), pos.getZ());
        if (cell == null || OverlayContext.isActiveAt(level, pos)) return original;
        VoxelShape result = original;
        if (cell.offset() != null && !result.isEmpty() && level.getBlockState(pos).is(this.asState().getBlock())) {
            result = result.move(cell.offset().x, cell.offset().y, cell.offset().z);
        }
        for (Overlay overlay : cell.overlays()) {
            VoxelShape collision = OverlayShapes.collision(level, pos, overlay);
            if (!collision.isEmpty()) {
                result = Shapes.or(result, collision);
            }
        }
        return result;
    }

    //? if <1.21.2 {
    @ModifyReturnValue(method = "getOffset", at = @At("RETURN"))
    private Vec3 offgrid$renderOffset(Vec3 original, BlockGetter level, BlockPos pos) {
        if (Offsets.isSuppressed()) return original;
        Vec3 offset = Offsets.of(level, pos, this.asState());
        return offset == null ? original : original.add(offset);
    }
    //?}

    //? if >=1.21.2 {
    /*@ModifyVariable(method = "updateShape", at = @At("HEAD"), argsOnly = true)
    private BlockState offgrid$seeCutNeighbour(BlockState neighbour, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true, ordinal = 1) BlockPos neighbourPos) {
        return Connections.forShape(this.asState(), level, pos, neighbourPos, neighbour);
    }
    *///?} else {
    @ModifyVariable(method = "updateShape", at = @At("HEAD"), argsOnly = true)
    private BlockState offgrid$seeCutNeighbour(BlockState neighbour, @Local(argsOnly = true) LevelAccessor level, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true, ordinal = 1) BlockPos neighbourPos) {
        return Connections.forShape(this.asState(), level, pos, neighbourPos, neighbour);
    }
    //?}

    @ModifyReturnValue(method = "canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z", at = @At("RETURN"))
    private boolean offgrid$cutThroughLayer(boolean original, BlockPlaceContext context) {
        return original || Layers.yields(this.asState(), context);
    }

    @ModifyReturnValue(method = "canSurvive", at = @At("RETURN"))
    private boolean offgrid$offgridSurvival(boolean original, LevelReader level, BlockPos pos) {
        return original || Placement.assistSurvival(this.asState(), level, pos);
    }

    //? if >=1.21.2 {
    /*@Inject(method = "handleNeighborChanged", at = @At("TAIL"))
    private void offgrid$notifyOverlays(Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moving, CallbackInfo ci) {
        Cell cell = Overlays.notifiedCell(level, pos);
        if (cell == null) return;
        for (Overlay overlay : cell.overlays()) {
            if (Cells.contains(level, pos, overlay)) {
                OverlayContext.run(level, pos, overlay, () -> overlay.getState().handleNeighborChanged(level, pos, block, orientation, moving));
            }
        }
    }
    *///?} else if >=1.21 {
    @Inject(method = "handleNeighborChanged", at = @At("TAIL"))
    private void offgrid$notifyOverlays(Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving, CallbackInfo ci) {
        Cell cell = Overlays.notifiedCell(level, pos);
        if (cell == null) return;
        for (Overlay overlay : cell.overlays()) {
            if (Cells.contains(level, pos, overlay)) {
                OverlayContext.run(level, pos, overlay, () -> overlay.getState().handleNeighborChanged(level, pos, block, fromPos, moving));
            }
        }
    }
    //?} else {
    /*@Inject(method = "neighborChanged", at = @At("TAIL"))
    private void offgrid$notifyOverlays(Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving, CallbackInfo ci) {
        Cell cell = Overlays.notifiedCell(level, pos);
        if (cell == null) return;
        for (Overlay overlay : cell.overlays()) {
            if (Cells.contains(level, pos, overlay)) {
                OverlayContext.run(level, pos, overlay, () -> overlay.getState().neighborChanged(level, pos, block, fromPos, moving));
            }
        }
    }
    *///?}

    @ModifyReturnValue(method = "getSignal", at = @At("RETURN"))
    private int offgrid$overlaySignal(int original, BlockGetter level, BlockPos pos, Direction direction) {
        return Signals.adjust(this.asState(), level, pos, direction, false, original);
    }

    @ModifyReturnValue(method = "getDirectSignal", at = @At("RETURN"))
    private int offgrid$overlayDirectSignal(int original, BlockGetter level, BlockPos pos, Direction direction) {
        return Signals.adjust(this.asState(), level, pos, direction, true, original);
    }

    @ModifyReturnValue(method = "isRedstoneConductor", at = @At("RETURN"))
    private boolean offgrid$conductRelocated(boolean original, BlockGetter level, BlockPos pos) {
        if (original || !(level instanceof Level real)) return original;
        OffgridIndex index = OffgridIndex.of(real);
        return index != null && !index.isEmpty() && Offsets.hostsRelocatedSource(real, pos);
    }
}
