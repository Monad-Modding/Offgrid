package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PathNavigationRegion.class)
public abstract class PathNavigationRegionMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    protected Level level;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.level);
    }
}
