//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import dev.tazer.offgrid.compat.create.CarriedCells;
import dev.tazer.offgrid.compat.create.ContraptionCarrier;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@IfModPresent("create")
@Mixin(AbstractContraptionEntity.class)
public abstract class AbstractContraptionEntityMixin implements ContraptionCarrier {

    @Shadow(remap = false)
    protected Contraption contraption;

    @Unique
    @Nullable
    private BlockHitResult offgrid$lastHit;

    @Shadow(remap = false)
    public abstract Vec3 toGlobalVector(Vec3 local, float partialTicks);

    @Shadow(remap = false)
    public abstract Vec3 toLocalVector(Vec3 global, float partialTicks);

    @Override
    @Nullable
    public CarriedCells offgrid$getCarried() {
        return this.contraption instanceof CarriedCells carried ? carried : null;
    }

    @Override
    public Vec3 offgrid$toGlobal(Vec3 local) {
        return this.toGlobalVector(local, 1.0F);
    }

    @Override
    public Vec3 offgrid$toLocal(Vec3 global) {
        return this.toLocalVector(global, 1.0F);
    }

    @Override
    @Nullable
    public BlockHitResult offgrid$getLastHit() {
        return this.offgrid$lastHit;
    }

    @Override
    public void offgrid$setLastHit(@Nullable BlockHitResult hit) {
        this.offgrid$lastHit = hit;
    }
}
//?}
