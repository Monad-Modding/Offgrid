package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.SlabMergeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin implements SlabMergeHolder {

    @Shadow
    @Final
    private Direction pushDirection;

    @Shadow
    @Final
    private List<BlockPos> toPush;

    @Unique
    private final Map<BlockPos, BlockState> offgrid$slabMerges = new HashMap<>();

    @Override
    public Map<BlockPos, BlockState> offgrid$getSlabMerges() {
        return this.offgrid$slabMerges;
    }

    @Inject(method = "resolve", at = @At("HEAD"))
    private void offgrid$clearSlabMerges(CallbackInfoReturnable<Boolean> cir) {
        this.offgrid$slabMerges.clear();
    }

    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean offgrid$dropMovedSlabMerges(boolean resolved) {
        this.offgrid$slabMerges.keySet().removeIf(this.toPush::contains);
        return resolved;
    }

    @WrapOperation(method = "addBlockLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 3))
    private BlockState offgrid$mergeSlab(Level level, BlockPos pos, Operation<BlockState> original) {
        BlockState state = original.call(level, pos);
        BlockPos from = pos.relative(this.pushDirection.getOpposite());
        if (!MixedSlabs.mergesInto(level, from, level.getBlockState(from), pos, state, this.pushDirection)) return state;
        this.offgrid$slabMerges.put(pos.immutable(), state);
        return Blocks.AIR.defaultBlockState();
    }
}
