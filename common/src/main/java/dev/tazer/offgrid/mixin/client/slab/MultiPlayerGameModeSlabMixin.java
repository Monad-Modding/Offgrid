package dev.tazer.offgrid.mixin.client.slab;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeSlabMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean offgrid$destroyingSecond;

    @ModifyReturnValue(method = "sameDestroyTarget", at = @At("RETURN"))
    private boolean offgrid$sameSlabHalf(boolean same, BlockPos pos) {
        if (!same || this.minecraft.level == null || this.minecraft.player == null) return same;
        BlockState state = this.minecraft.level.getBlockState(pos);
        if (!(state.getBlock() instanceof MixedSlabBlock)) return true;
        return MixedSlabs.targetsSecond(this.minecraft.player, pos, state.getValue(MixedSlabBlock.AXIS)) == this.offgrid$destroyingSecond;
    }

    @Inject(method = "startDestroyBlock", at = @At("TAIL"))
    private void offgrid$rememberSlabHalf(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.level == null || this.minecraft.player == null) return;
        BlockState state = this.minecraft.level.getBlockState(pos);
        if (state.getBlock() instanceof MixedSlabBlock) {
            this.offgrid$destroyingSecond = MixedSlabs.targetsSecond(this.minecraft.player, pos, state.getValue(MixedSlabBlock.AXIS));
        }
    }
}
