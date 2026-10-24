//? if >=1.21 {
package dev.tazer.offgrid.mixin.compat.lithium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridIndex;
import net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
//? if <26 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//?}

@Pseudo
@IfModPresent("lithium")
@Mixin(ChunkAwareBlockCollisionSweeper.class)
public abstract class ChunkAwareBlockCollisionSweeperMixin {

    //? if <26 {
    @Shadow
    @Final
    private Level world;
    //?}

    @ModifyReturnValue(method = "hasChunkSectionOversizedBlocks", at = @At("RETURN"))
    private static boolean offgrid$offgridSections(boolean original, @Local(argsOnly = true) ChunkAccess chunk, @Local(argsOnly = true) int chunkY) {
        return original || OffgridIndex.hasCellsInSection(chunk, chunkY);
    }

    //? if <26 {
    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/lithium/common/entity/movement/ChunkAwareBlockCollisionSweeper;canInteractWithBlock(Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean offgrid$offgridNeighbours(BlockState state, int edgesHit, Operation<Boolean> original, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "z") int z) {
        return original.call(state, edgesHit) || OffgridIndex.hasCell(this.world, x, y, z);
    }
    //?}
}
//?}
