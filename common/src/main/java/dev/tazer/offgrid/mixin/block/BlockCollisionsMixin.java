package dev.tazer.offgrid.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.tazer.offgrid.world.OffgridIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

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
        if (original) return true;
        OffgridIndex index = OffgridIndex.of(this.collisionGetter);
        return index != null && index.getCell(this.pos.getX(), this.pos.getY(), this.pos.getZ()) != null;
    }
}
