//? if >=26 {
/*package dev.tazer.offgrid.mixin.compat.lithium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridIndex;
import net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeperBlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("lithium")
@Mixin(ChunkAwareBlockCollisionSweeperBlockPos.class)
public abstract class ChunkAwareBlockCollisionSweeperBlockPosMixin {

    @WrapOperation(method = "computeNext()Lnet/minecraft/core/BlockPos$MutableBlockPos;", at = @At(value = "INVOKE", target = "canInteractWithBlock(Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean offgrid$offgridNeighbours(BlockState state, int edgesHit, Operation<Boolean> original, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "z") int z) {
        return original.call(state, edgesHit) || OffgridIndex.hasCell(((ChunkAwareBlockCollisionSweeperAccessor) (Object) this).offgrid$getWorld(), x, y, z);
    }
}
*///?}
