package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements OverlayBlockEntity {

    @Unique
    @Nullable
    private Overlay offgrid$overlay;

    @Override
    @Nullable
    public Overlay offgrid$getOverlay() {
        return this.offgrid$overlay;
    }

    @Override
    public void offgrid$setOverlay(@Nullable Overlay overlay) {
        this.offgrid$overlay = overlay;
    }
}
