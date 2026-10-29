//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.tazer.offgrid.compat.sable.SubLevelLanding;
import dev.tazer.offgrid.compat.sable.SubLevels;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Pseudo
@IfModPresent("sable")
@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {

    @WrapMethod(method = "moveBlocks")
    private static void offgrid$moveCells(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, Operation<Void> original) {
        List<BlockPos> moved = new ArrayList<>();
        for (BlockPos pos : blocks) {
            moved.add(pos.immutable());
        }
        boolean leaving = !moved.isEmpty() && SubLevels.contains(level, moved.get(0));
        AssembledCell.Held held = leaving ? AssembledCell.everything(level, moved) : AssembledCell.held(level, moved);
        moved.addAll(held.blocks());
        Map<BlockPos, AssembledCell> assembled = AssembledCell.assembleHeld(level, moved, held);
        ServerLevel target = transform.getLevel();
        Set<BlockPos> sources = new LinkedHashSet<>(moved);
        sources.addAll(assembled.keySet());
        if (!leaving) {
            List<BlockPos> stale = new ArrayList<>(sources.size());
            for (BlockPos pos : sources) {
                stale.add(transform.apply(pos));
            }
            AssembledCell.clear(target, stale);
        }
        Map<BlockPos, Direction.Axis> slabs = new LinkedHashMap<>();
        for (BlockPos pos : moved) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof MixedSlabBlock) {
                slabs.put(transform.apply(pos), state.getValue(MixedSlabBlock.AXIS));
            }
        }
        SubLevelLanding landing = new SubLevelLanding(transform, assembled, moved, slabs);
        SubLevelLanding.begin(landing);
        try {
            original.call(level, transform, moved);
        } finally {
            SubLevelLanding.end();
        }
        landing.land();
        AssembledCell.reevaluate(level, sources);
    }

    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper;markAndNotifyBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;II)V"))
    private static void offgrid$landBeforeNotify(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        SubLevelLanding.landActive();
    }
}
//?}
