//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderSectionRegion.class)
public abstract class RenderSectionRegionMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    private ClientLevel level;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.level);
    }
}
*///?}
