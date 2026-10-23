package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.MovingBlockData;
import dev.tazer.offgrid.world.MovingBlockDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26 {
/*import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?} else if >=1.20.5 {
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
*///?}

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements MovingBlockDataHolder {

    @Unique
    private final Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$renderedEntities = new IdentityHashMap<>();

    @Unique
    @Nullable
    private MovingBlockData offgrid$movingBlockData;

    protected PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nullable
    public MovingBlockData offgrid$getMovingBlockData() {
        return this.offgrid$movingBlockData;
    }

    @Override
    public void offgrid$setMovingBlockData(@Nullable MovingBlockData data) {
        this.offgrid$movingBlockData = data;
    }

    @Override
    public Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$getRenderedEntities() {
        return this.offgrid$renderedEntities;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    private static void offgrid$restoreMovingBlock(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        MovingBlockData.restore(level, pos, blockEntity);
    }

    //? if >=26.3 {
    /*@Inject(method = "finalTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.AFTER))
    *///?} else {
    @Inject(method = "finalTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    //?}
    private void offgrid$restoreMovingBlockEarly(CallbackInfo ci) {
        MovingBlockData.restore(this.level, this.worldPosition, this);
    }

    //? if >=26 {
    /*@Inject(method = "saveAdditional", at = @At("TAIL"))
    private void offgrid$saveMovingBlock(ValueOutput output, CallbackInfo ci) {
        if (this.offgrid$movingBlockData != null) {
            output.store(MovingBlockData.KEY, MovingBlockData.CODEC, this.offgrid$movingBlockData);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void offgrid$loadMovingBlock(ValueInput input, CallbackInfo ci) {
        this.offgrid$movingBlockData = input.read(MovingBlockData.KEY, MovingBlockData.CODEC).orElse(null);
    }
    *///?} else if >=1.20.5 {
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void offgrid$saveMovingBlock(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.offgrid$movingBlockData != null) {
            MovingBlockData.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.offgrid$movingBlockData).result().ifPresent(encoded -> tag.put(MovingBlockData.KEY, encoded));
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void offgrid$loadMovingBlock(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.offgrid$movingBlockData = tag.contains(MovingBlockData.KEY) ? MovingBlockData.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(MovingBlockData.KEY)).result().orElse(null) : null;
    }
    //?} else {
    /*@Inject(method = "saveAdditional", at = @At("TAIL"))
    private void offgrid$saveMovingBlock(CompoundTag tag, CallbackInfo ci) {
        if (this.offgrid$movingBlockData != null) {
            MovingBlockData.CODEC.encodeStart(NbtOps.INSTANCE, this.offgrid$movingBlockData).result().ifPresent(encoded -> tag.put(MovingBlockData.KEY, encoded));
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void offgrid$loadMovingBlock(CompoundTag tag, CallbackInfo ci) {
        this.offgrid$movingBlockData = tag.contains(MovingBlockData.KEY) ? MovingBlockData.CODEC.parse(NbtOps.INSTANCE, tag.get(MovingBlockData.KEY)).result().orElse(null) : null;
    }
    *///?}
}
