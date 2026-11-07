//? if >=26 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.BlockEntities;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@IfModPresent("sodium")
@Mixin(LevelSlice.class)
public abstract class LevelSliceBlockEntityMixin {

    @Inject(method = "getBlockEntity(III)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), cancellable = true)
    private void offgrid$viewOverlayBlockEntity(int x, int y, int z, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(x, y, z);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }
}
*///?} else if >=1.21 {
package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.BlockEntities;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@IfModPresent("sodium")
@Mixin(LevelSlice.class)
public abstract class LevelSliceBlockEntityMixin {

    @Inject(method = "getBlockEntity(III)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), cancellable = true)
    private void offgrid$viewOverlayBlockEntity(int x, int y, int z, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(x, y, z);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }

    @Inject(method = {"getBlockEntityRenderData", "getBlockEntityRenderAttachment"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$viewOverlayRenderData(BlockPos pos, CallbackInfoReturnable<Object> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(pos);
        if (viewed != null) {
            cir.setReturnValue(viewed.getRenderData());
        }
    }
}
//?}
