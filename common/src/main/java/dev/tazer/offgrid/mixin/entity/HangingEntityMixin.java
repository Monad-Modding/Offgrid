package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.FlatPaintings;
import dev.tazer.offgrid.world.Overlays;
import dev.tazer.offgrid.world.SurfaceMounted;
import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
//?} else {
/*import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
*///?}

@Mixin(HangingEntity.class)
//? if >=1.21 {
public abstract class HangingEntityMixin extends BlockAttachedEntity {
//?} else {
/*public abstract class HangingEntityMixin {
*///?}

    //? if >=1.21 && <26 {
    @Shadow
    protected Direction direction;
    //?}
    //? if <1.21 {
    /*@Shadow
    protected Direction direction;

    @Shadow
    protected BlockPos pos;

    @Shadow
    protected abstract void recalculateBoundingBox();
    *///?}
    //? if >=26 {
    /*@Shadow
    protected abstract void setDirectionRaw(Direction direction);
    *///?}

    //? if >=1.21 {
    protected HangingEntityMixin(EntityType<? extends BlockAttachedEntity> type, Level level) {
        super(type, level);
    }
    //?}

    @Inject(method = "setDirection", at = @At("HEAD"), cancellable = true)
    private void offgrid$lieFlat(Direction face, CallbackInfo ci) {
        HangingEntity self = (HangingEntity) (Object) this;
        if (!face.getAxis().isVertical() || !FlatPaintings.isFlatCandidate(self)) return;
        Direction placing = FlatPaintings.placingTop();
        Direction top = placing != null ? placing : FlatPaintings.top(self);
        //? if >=26 {
        /*this.setDirectionRaw(face);
        *///?} else {
        this.direction = face;
        //?}
        self.setYRot(top.toYRot());
        self.yRotO = self.getYRot();
        this.recalculateBoundingBox();
        ci.cancel();
    }

    @Inject(method = "survives", at = @At("HEAD"), cancellable = true)
    private void offgrid$surviveOnSurface(CallbackInfoReturnable<Boolean> cir) {
        HangingEntity self = (HangingEntity) (Object) this;
        if (SurfaceMounts.isShifted(self)) {
            cir.setReturnValue(SurfaceMounts.survives(self));
        }
    }

    //? if >=1.21 {
    @ModifyExpressionValue(method = "recalculateBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;calculateBoundingBox(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/AABB;"))
    private AABB offgrid$mountOnSurface(AABB box) {
        Vec3 offset = ((SurfaceMounted) this).offgrid$getSurfaceOffset();
        return Overlays.isZero(offset) ? box : box.move(offset);
    }
    //?} else {
    /*@Inject(method = "recalculateBoundingBox", at = @At("HEAD"), cancellable = true)
    private void offgrid$flatBox(CallbackInfo ci) {
        HangingEntity self = (HangingEntity) (Object) this;
        if (!(self instanceof Painting painting) || !FlatPaintings.isFlat(painting)) return;
        AABB box = FlatPaintings.box(this.pos, this.direction, FlatPaintings.top(painting), FlatPaintings.width(painting), FlatPaintings.height(painting));
        self.setPosRaw(box.getCenter().x, box.getCenter().y, box.getCenter().z);
        self.setBoundingBox(box);
        SurfaceMounts.shift(self);
        ci.cancel();
    }

    @Inject(method = "recalculateBoundingBox", at = @At("TAIL"))
    private void offgrid$mountOnSurface(CallbackInfo ci) {
        SurfaceMounts.shift((HangingEntity) (Object) this);
    }

    @Inject(method = "survives", at = @At("HEAD"), cancellable = true)
    private void offgrid$surviveFlat(CallbackInfoReturnable<Boolean> cir) {
        HangingEntity self = (HangingEntity) (Object) this;
        if (!FlatPaintings.isFlat(self)) return;
        Level level = self.level();
        if (!level.noCollision(self)) {
            cir.setReturnValue(false);
            return;
        }
        AABB support = self.getBoundingBox().move(this.direction.getStepX() * -0.5, this.direction.getStepY() * -0.5, this.direction.getStepZ() * -0.5).deflate(1.0E-7);
        for (BlockPos cursor : BlockPos.betweenClosed(BlockPos.containing(support.minX, support.minY, support.minZ), BlockPos.containing(support.maxX, support.maxY, support.maxZ))) {
            if (Block.canSupportCenter(level, cursor, this.direction)) {
                continue;
            }
            BlockState state = level.getBlockState(cursor);
            if (!state.isSolid() && !DiodeBlock.isDiode(state)) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(level.getEntities(self, self.getBoundingBox(), entity -> entity instanceof HangingEntity).isEmpty());
    }
    *///?}

    @Inject(method = "rotate", at = @At("HEAD"))
    private void offgrid$rotateSurfaceOffset(Rotation rotation, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof SurfaceMounted mounted && !Overlays.isZero(mounted.offgrid$getSurfaceOffset())) {
            mounted.offgrid$setSurfaceOffset(AssembledCell.rotate(mounted.offgrid$getSurfaceOffset(), rotation));
        }
    }

    @Inject(method = "mirror", at = @At("HEAD"))
    private void offgrid$mirrorSurfaceOffset(Mirror mirror, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof SurfaceMounted mounted && !Overlays.isZero(mounted.offgrid$getSurfaceOffset())) {
            mounted.offgrid$setSurfaceOffset(AssembledCell.mirror(mounted.offgrid$getSurfaceOffset(), mirror));
        }
    }
}
