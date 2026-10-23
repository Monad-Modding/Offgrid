package dev.tazer.offgrid.mixin.block;

import dev.tazer.offgrid.world.Raycasts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class LevelClipMixin implements BlockGetter {

    @Override
    @Nullable
    public BlockHitResult clipWithInteractionOverride(Vec3 from, Vec3 to, BlockPos pos, VoxelShape shape, BlockState state) {
        Level level = (Level) (Object) this;
        return Raycasts.clip(level, from, to, pos, Raycasts.clipBlock(level, from, to, pos, shape, state));
    }
}
