package dev.tazer.offgrid.mixin.slab;

import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemSlabMixin {

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void offgrid$placeMixedSlab(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (MixedSlabs.tryPlace(context, state)) {
            cir.setReturnValue(true);
        }
    }
}
