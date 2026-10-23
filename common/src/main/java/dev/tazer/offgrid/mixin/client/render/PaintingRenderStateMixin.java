//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import dev.tazer.offgrid.client.FlatPaintingState;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PaintingRenderState.class)
public abstract class PaintingRenderStateMixin implements FlatPaintingState {

    @Unique
    private Direction offgrid$top = Direction.NORTH;

    @Override
    public Direction offgrid$getTop() {
        return this.offgrid$top;
    }

    @Override
    public void offgrid$setTop(Direction top) {
        this.offgrid$top = top;
    }
}
*///?}
