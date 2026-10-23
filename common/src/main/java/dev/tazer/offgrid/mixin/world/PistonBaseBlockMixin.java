package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.MovingBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {

    @WrapOperation(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getPistonPushReaction()Lnet/minecraft/world/level/material/PushReaction;"))
    private static PushReaction offgrid$mixedSlabReaction(BlockState state, Operation<PushReaction> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        PushReaction reaction = original.call(state);
        return state.getBlock() instanceof MixedSlabBlock ? MixedSlabs.pushReaction(level, pos, reaction) : reaction;
    }

    @WrapOperation(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean offgrid$mixedSlabsMove(BlockState state, Operation<Boolean> original) {
        return original.call(state) && !(state.getBlock() instanceof MixedSlabBlock);
    }

    @WrapOperation(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0))
    private BlockEntity offgrid$captureMovingBlock(BlockPos pos, BlockState movingState, BlockState movedState, Direction facing, boolean extending, boolean sourcePiston, Operation<BlockEntity> original, @Local(argsOnly = true) Level level) {
        BlockEntity moving = original.call(pos, movingState, movedState, facing, extending, sourcePiston);
        MovingBlockData.capture(level, pos.relative(extending ? facing.getOpposite() : facing), moving);
        return moving;
    }
}
