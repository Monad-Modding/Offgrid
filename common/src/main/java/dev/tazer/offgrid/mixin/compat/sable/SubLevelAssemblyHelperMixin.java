//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.tazer.offgrid.compat.sable.SubLevels;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.ArrayList;
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
        original.call(level, transform, moved);
        Rotation rotation = transform.getRotation();
        AssembledCell.landAll(target, assembled, moved, transform::apply, state -> state.rotate(rotation), offset -> AssembledCell.rotate(offset, rotation));
        AssembledCell.reevaluate(level, sources);
    }
}
//?}
