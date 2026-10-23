package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.world.FlatPaintings;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26 {
/*import net.minecraft.world.entity.decoration.painting.Painting;
*///?} else {
import net.minecraft.world.entity.decoration.Painting;
//?}
//? if >=1.21 {
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}
//? if >=1.21.6 {
/*import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?} else {
import net.minecraft.nbt.CompoundTag;
//?}

@Mixin(Painting.class)
public abstract class PaintingMixin extends HangingEntity {

    protected PaintingMixin(EntityType<? extends HangingEntity> type, Level level) {
        super(type, level);
    }

    //? if >=1.21 {
    @Inject(method = "calculateBoundingBox", at = @At("HEAD"), cancellable = true)
    private void offgrid$flatBox(BlockPos pos, Direction face, CallbackInfoReturnable<AABB> cir) {
        if (!face.getAxis().isVertical()) return;
        Painting painting = (Painting) (Object) this;
        cir.setReturnValue(FlatPaintings.box(pos, face, FlatPaintings.top(painting), FlatPaintings.width(painting), FlatPaintings.height(painting)));
    }
    //?}

    //? if >=1.21.6 {
    /*@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$saveFlat(ValueOutput output, CallbackInfo ci) {
        if (!FlatPaintings.isFlat(this)) return;
        output.store(FlatPaintings.FACE_KEY, Direction.CODEC, this.getDirection());
        output.store(FlatPaintings.TOP_KEY, Direction.CODEC, FlatPaintings.top(this));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$loadFlat(ValueInput input, CallbackInfo ci) {
        Direction face = input.read(FlatPaintings.FACE_KEY, Direction.CODEC).orElse(null);
        Direction top = input.read(FlatPaintings.TOP_KEY, Direction.CODEC).orElse(null);
        this.offgrid$layFlat(face, top);
    }
    *///?} else {
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$saveFlat(CompoundTag tag, CallbackInfo ci) {
        if (!FlatPaintings.isFlat(this)) return;
        tag.putString(FlatPaintings.FACE_KEY, this.getDirection().getSerializedName());
        tag.putString(FlatPaintings.TOP_KEY, FlatPaintings.top(this).getSerializedName());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void offgrid$loadFlat(CompoundTag tag, CallbackInfo ci) {
        this.offgrid$layFlat(Direction.byName(tag.getString(FlatPaintings.FACE_KEY)), Direction.byName(tag.getString(FlatPaintings.TOP_KEY)));
    }
    //?}

    @Unique
    private void offgrid$layFlat(@Nullable Direction face, @Nullable Direction top) {
        if (face == null || top == null || !face.getAxis().isVertical() || !top.getAxis().isHorizontal()) return;
        this.setYRot(top.toYRot());
        this.setDirection(face);
    }
}
