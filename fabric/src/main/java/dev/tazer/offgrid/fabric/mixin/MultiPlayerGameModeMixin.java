package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
//? if <26.3 {
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.level.block.SoundType;
//?}

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean offgrid$breakOneHalf(Level level, BlockPos pos, BlockState replacement, int flags, Operation<Boolean> original) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof MixedSlabBlock && this.minecraft.player != null && MixedSlabBlock.breakHalf(level, pos, state, this.minecraft.player)) return true;
        return original.call(level, pos, replacement, flags);
    }

    //? if <26.3 {
    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType offgrid$halfHitSound(BlockState state, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof MixedSlabBlock && this.minecraft.level != null && this.minecraft.player != null) {
            BlockState half = MixedSlabs.half(this.minecraft.level, pos, MixedSlabs.targetsSecond(this.minecraft.player, pos, state.getValue(MixedSlabBlock.AXIS)));
            if (half != null) return original.call(half);
        }
        return original.call(state);
    }
    //?}
}
