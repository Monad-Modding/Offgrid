//? if >=1.21 {
package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.world.Overlays;
import dev.tazer.offgrid.world.SurfaceMounted;
import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.6 {
/*import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?} else {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
//?}

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin implements SurfaceMounted {

    @Unique
    private Vec3 offgrid$surfaceOffset = Vec3.ZERO;

    @Shadow
    protected abstract void recalculateBoundingBox();

    @Override
    public Vec3 offgrid$getSurfaceOffset() {
        return this.offgrid$surfaceOffset;
    }

    @Override
    public void offgrid$setSurfaceOffset(Vec3 offset) {
        this.offgrid$surfaceOffset = offset;
        this.recalculateBoundingBox();
    }

    //? if >=1.21.6 {
    /*@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$saveSurfaceOffset(ValueOutput output, CallbackInfo ci) {
        if (!Overlays.isZero(this.offgrid$surfaceOffset)) {
            output.store(SurfaceMounts.KEY, Vec3.CODEC, this.offgrid$surfaceOffset);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$loadSurfaceOffset(ValueInput input, CallbackInfo ci) {
        this.offgrid$setSurfaceOffset(input.read(SurfaceMounts.KEY, Vec3.CODEC).orElse(Vec3.ZERO));
    }
    *///?} else {
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$saveSurfaceOffset(CompoundTag tag, CallbackInfo ci) {
        if (!Overlays.isZero(this.offgrid$surfaceOffset)) {
            Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.offgrid$surfaceOffset).result().ifPresent(encoded -> tag.put(SurfaceMounts.KEY, encoded));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$loadSurfaceOffset(CompoundTag tag, CallbackInfo ci) {
        Vec3 offset = tag.contains(SurfaceMounts.KEY) ? Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get(SurfaceMounts.KEY)).result().orElse(Vec3.ZERO) : Vec3.ZERO;
        this.offgrid$setSurfaceOffset(offset);
    }
    //?}
}
//?}
