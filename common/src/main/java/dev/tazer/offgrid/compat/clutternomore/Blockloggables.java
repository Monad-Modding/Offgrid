package dev.tazer.offgrid.compat.clutternomore;

import dev.tazer.clutternomore.block.StepBlock;
import dev.tazer.clutternomore.block.VerticalSlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class Blockloggables {

    private Blockloggables() {
    }

    public static boolean isBlockloggable(BlockState state) {
        return state.getBlock() instanceof VerticalSlabBlock || state.getBlock() instanceof StepBlock;
    }
}
