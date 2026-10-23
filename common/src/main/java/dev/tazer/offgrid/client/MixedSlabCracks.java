package dev.tazer.offgrid.client;

import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class MixedSlabCracks {

    private MixedSlabCracks() {
    }

    public static BlockState half(BlockState state, BlockGetter level, BlockPos pos) {
        if (!(state.getBlock() instanceof MixedSlabBlock)) return state;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return state;
        boolean second = minecraft.hitResult instanceof BlockHitResult hit && hit.getBlockPos().equals(pos) ? MixedSlabs.targetsSecond(hit, pos, state.getValue(MixedSlabBlock.AXIS)) : MixedSlabs.targetsSecond(minecraft.player, pos, state.getValue(MixedSlabBlock.AXIS));
        BlockState half = MixedSlabs.half(level, pos, second);
        return half == null ? state : half;
    }
}
