package dev.tazer.offgrid.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.tazer.offgrid.world.OffgridIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//? if <26 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
//?}

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @Shadow
    @Final
    private CollisionGetter collisionGetter;

    @ModifyExpressionValue(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasLargeCollisionShape()Z"))
    private boolean offgrid$visitOffgridCells(boolean original) {
        return original || this.offgrid$hasCell();
    }

    //? if >=26 {
    /*@ModifyExpressionValue(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    *///?} else {
    @ModifyExpressionValue(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    //?}
    private boolean offgrid$visitOffgridEdges(boolean original) {
        return original || this.offgrid$hasCell();
    }

    //? if <26 {
    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape offgrid$offgridShapes(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        return original.call(state, this.offgrid$hasCell() ? this.collisionGetter : getter, pos, context);
    }
    //?}

    @Unique
    private boolean offgrid$hasCell() {
        return OffgridIndex.hasCell(this.collisionGetter, this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }
}
