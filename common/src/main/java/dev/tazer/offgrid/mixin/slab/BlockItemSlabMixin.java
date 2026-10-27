package dev.tazer.offgrid.mixin.slab;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.Scope;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BlockItem.class)
public abstract class BlockItemSlabMixin {

    @WrapMethod(method = "place")
    private InteractionResult offgrid$trackPlacedHalf(BlockPlaceContext context, Operation<InteractionResult> original) {
        return Scope.with(Scope.PLACED_HALF, new AtomicReference<>(), () -> original.call(context));
    }

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void offgrid$placeMixedSlab(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (MixedSlabs.tryPlace(context, state)) {
            cir.setReturnValue(true);
        }
    }
}
