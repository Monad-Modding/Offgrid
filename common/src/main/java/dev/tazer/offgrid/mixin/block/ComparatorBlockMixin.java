package dev.tazer.offgrid.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Signals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin {

    @ModifyReturnValue(method = "getInputSignal", at = @At("RETURN"))
    private int offgrid$overlayAnalogOutput(int original, Level level, BlockPos pos, BlockState state) {
        if (original >= 15) return original;
        Direction facing = state.getValue(ComparatorBlock.FACING);
        BlockPos input = pos.relative(facing);
        int best = Math.max(original, Signals.analogOutput(level, input));
        if (best < 15 && level.getBlockState(input).isRedstoneConductor(level, input)) {
            best = Math.max(best, Signals.analogOutput(level, input.relative(facing)));
        }
        return best;
    }
}
