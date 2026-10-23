package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayShapes;
import dev.tazer.offgrid.world.Support;
import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;
//? if >=26 {
/*import net.minecraft.world.entity.InsideBlockEffectApplier;
import org.spongepowered.asm.mixin.Final;
import java.util.List;
*///?}

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract double getY();

    //? if >=26 {
    /*@Shadow
    @Final
    private InsideBlockEffectApplier.StepBasedCollector insideEffectCollector;
    *///?}

    //? if >=26 {
    /*@Inject(method = "applyEffectsFromBlocks(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;checkInsideBlocks(Ljava/util/List;Lnet/minecraft/world/entity/InsideBlockEffectApplier$StepBasedCollector;)V", shift = At.Shift.AFTER))
    private void offgrid$insideOverlays(List<?> movements, CallbackInfo ci) {
        Level level = this.level();
        Entity self = (Entity) (Object) this;
        this.offgrid$touchOverlays((host, overlay) -> overlay.getState().entityInside(level, host, self, this.insideEffectCollector, true));
    }
    *///?} else {
    @Inject(method = "checkInsideBlocks()V", at = @At("TAIL"))
    private void offgrid$insideOverlays(CallbackInfo ci) {
        Level level = this.level();
        Entity self = (Entity) (Object) this;
        this.offgrid$touchOverlays((host, overlay) -> overlay.getState().entityInside(level, host, self));
    }
    //?}

    @Unique
    private void offgrid$touchOverlays(BiConsumer<BlockPos, Overlay> touch) {
        Level level = this.level();
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return;
        AABB box = this.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX - 1.0E-7, box.minY - 1.0E-7, box.minZ - 1.0E-7);
        BlockPos max = BlockPos.containing(box.maxX + 1.0E-7, box.maxY + 1.0E-7, box.maxZ + 1.0E-7);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            Cell cell = index.getCell(cursor);
            if (cell == null || !cell.hasOverlays()) {
                continue;
            }
            BlockPos host = cursor.immutable();
            for (Overlay overlay : cell.overlays()) {
                VoxelShape shape = OverlayShapes.of(level, host, overlay);
                if (shape.isEmpty() || !shape.bounds().move(host).intersects(box)) {
                    continue;
                }
                OverlayContext.run(level, host, overlay, () -> touch.accept(host, overlay));
            }
        }
    }

    @Inject(method = "startSeenByPlayer", at = @At("TAIL"))
    private void offgrid$sendSurfaceOffset(ServerPlayer player, CallbackInfo ci) {
        SurfaceMounts.sendTo(player, (Entity) (Object) this);
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void offgrid$offgridStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockState stepped = this.offgrid$steppedState(pos, state);
        if (stepped == null) return;
        SoundType sound = stepped.getSoundType();
        ((Entity) (Object) this).playSound(sound.getStepSound(), sound.getVolume() * 0.15F, sound.getPitch());
        ci.cancel();
    }

    @Unique
    @Nullable
    private BlockState offgrid$steppedState(BlockPos pos, BlockState state) {
        Overlay standing = this.offgrid$standingOn(pos);
        if (standing != null) return standing.getState();
        if (!(state.getBlock() instanceof MixedSlabBlock)) return null;
        return MixedSlabs.half(this.level(), pos, MixedSlabs.containsSecond(((Entity) (Object) this).position(), pos, state.getValue(MixedSlabBlock.AXIS)));
    }

    @Unique
    @Nullable
    private Overlay offgrid$standingOn(BlockPos pos) {
        OffgridIndex index = OffgridIndex.of(this.level());
        if (index == null) return null;
        Cell cell = index.getCell(pos);
        if (cell == null || !cell.hasOverlays()) return null;
        AABB box = this.getBoundingBox();
        Overlay best = null;
        double bestTop = Double.NEGATIVE_INFINITY;
        for (Overlay overlay : cell.overlays()) {
            VoxelShape shape = OverlayShapes.collision(this.level(), pos, overlay);
            if (shape.isEmpty()) {
                continue;
            }
            AABB bounds = shape.bounds().move(pos);
            if (bounds.maxX <= box.minX + Support.EPSILON || bounds.minX >= box.maxX - Support.EPSILON || bounds.maxZ <= box.minZ + Support.EPSILON || bounds.minZ >= box.maxZ - Support.EPSILON) {
                continue;
            }
            if (Math.abs(bounds.maxY - this.getY()) > 0.05 || bounds.maxY <= bestTop) {
                continue;
            }
            bestTop = bounds.maxY;
            best = overlay;
        }
        return best;
    }
}
