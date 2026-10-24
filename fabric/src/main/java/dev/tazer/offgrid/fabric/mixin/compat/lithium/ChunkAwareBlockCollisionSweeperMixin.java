//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin.compat.lithium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridIndex;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("lithium")
@Mixin(ChunkAwareBlockCollisionSweeper.class)
public abstract class ChunkAwareBlockCollisionSweeperMixin {

    @Shadow
    @Final
    private Level world;

    @ModifyReturnValue(method = "hasChunkSectionOversizedBlocks", at = @At("RETURN"))
    private static boolean offgrid$offgridSections(boolean original, @Local(argsOnly = true) ChunkAccess chunk, @Local(argsOnly = true) int chunkY) {
        return original || OffgridIndex.hasCellsInSection(chunk, chunkY);
    }

    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/lithium/common/entity/movement/ChunkAwareBlockCollisionSweeper;canInteractWithBlock(Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean offgrid$offgridNeighbours(BlockState state, int edgesHit, Operation<Boolean> original, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "z") int z) {
        return original.call(state, edgesHit) || OffgridIndex.hasCell(this.world, x, y, z);
    }
}
*///?}
