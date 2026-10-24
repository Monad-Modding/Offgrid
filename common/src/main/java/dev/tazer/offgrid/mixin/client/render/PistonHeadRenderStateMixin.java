//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import dev.tazer.offgrid.client.MovingOverlayPart;
import dev.tazer.offgrid.client.MovingPistonParts;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(PistonHeadRenderState.class)
public abstract class PistonHeadRenderStateMixin implements MovingPistonParts {

    @Unique
    @Nullable
    private MovingBlockRenderState offgrid$secondHalf;

    @Unique
    @Nullable
    private MovingBlockRenderState offgrid$stationaryHalf;

    @Unique
    private List<MovingOverlayPart> offgrid$overlays = List.of();

    @Override
    @Nullable
    public MovingBlockRenderState offgrid$getSecondHalf() {
        return this.offgrid$secondHalf;
    }

    @Override
    public void offgrid$setSecondHalf(@Nullable MovingBlockRenderState half) {
        this.offgrid$secondHalf = half;
    }

    @Override
    @Nullable
    public MovingBlockRenderState offgrid$getStationaryHalf() {
        return this.offgrid$stationaryHalf;
    }

    @Override
    public void offgrid$setStationaryHalf(@Nullable MovingBlockRenderState half) {
        this.offgrid$stationaryHalf = half;
    }

    @Override
    public List<MovingOverlayPart> offgrid$getOverlays() {
        return this.offgrid$overlays;
    }

    @Override
    public void offgrid$setOverlays(List<MovingOverlayPart> overlays) {
        this.offgrid$overlays = overlays;
    }
}
*///?}
