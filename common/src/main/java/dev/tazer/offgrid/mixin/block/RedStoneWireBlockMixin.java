package dev.tazer.offgrid.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.Signals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {

    @ModifyReturnValue(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At("RETURN"))
    private RedstoneSide offgrid$connectToOverlays(RedstoneSide original, BlockGetter level, BlockPos pos, Direction face) {
        if (original != RedstoneSide.NONE || !(level instanceof Level real)) return original;
        BlockPos neighbour = pos.relative(face);
        return Signals.hasSource(real, neighbour) || Offsets.hostsRelocatedSource(real, neighbour) ? RedstoneSide.SIDE : original;
    }
}
