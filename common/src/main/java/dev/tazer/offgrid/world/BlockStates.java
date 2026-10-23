package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.5 {
/*import net.minecraft.server.level.ServerLevel;
*///?} else {
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Blocks;
//?}

public final class BlockStates {

    private BlockStates() {
    }

    public static void removed(BlockState oldState, Level level, BlockPos pos, BlockState newState, @Nullable BlockEntity blockEntity) {
        if (level.isClientSide()) return;
        //? if >=1.21.5 {
        /*if (oldState.is(newState.getBlock())) return;
        if (blockEntity != null) {
            blockEntity.preRemoveSideEffects(pos, oldState);
        }
        if (level instanceof ServerLevel serverLevel) {
            oldState.affectNeighborsAfterRemoval(serverLevel, pos, false);
        }
        *///?} else {
        oldState.onRemove(level, pos, newState, false);
        //?}
    }

    public static void moved(BlockState oldState, Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (level.isClientSide()) return;
        //? if >=1.21.5 {
        /*if (level instanceof ServerLevel serverLevel) {
            oldState.affectNeighborsAfterRemoval(serverLevel, pos, true);
        }
        *///?} else {
        Clearable.tryClear(blockEntity);
        oldState.onRemove(level, pos, Blocks.AIR.defaultBlockState(), true);
        //?}
    }

    public static void placed(BlockState newState, Level level, BlockPos pos, BlockState oldState) {
        if (level.isClientSide()) return;
        newState.onPlace(level, pos, oldState, false);
    }
}
