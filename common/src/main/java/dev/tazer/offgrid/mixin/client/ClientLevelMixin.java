package dev.tazer.offgrid.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.client.OverlayEffects;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.EffectOffset;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26 {
/*import dev.tazer.offgrid.client.MixedSlabParticles;
import dev.tazer.offgrid.client.OverlayBreaking;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
*///?} else {
import net.minecraft.world.entity.player.Player;
//?}
//? if >=26.3 {
/*import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.client.MixedSlabCracks;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
*///?}

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    /*private void offgrid$overlayDestroyEffects(@Nullable Entity source, int type, BlockPos pos, int data, CallbackInfo ci) {
    *///?} else {
    private void offgrid$overlayDestroyEffects(@Nullable Player source, int type, BlockPos pos, int data, CallbackInfo ci) {
    //?}
        if (type != LevelEvent.PARTICLES_DESTROY_BLOCK) return;
        ClientLevel level = (ClientLevel) (Object) this;
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame == null || frame.getOverlay() == null) return;
        Overlay overlay = frame.getOverlay();
        BlockPos host = frame.getHost();
        OverlayContext.runOutside(() -> OverlayEffects.destroy(level, host, overlay));
        ci.cancel();
    }

    //? if >=26 {
    /*@WrapMethod(method = "doAddParticle")
    private void offgrid$shiftParticle(ParticleOptions options, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(options, overrideLimiter, alwaysShow, x + shift.x, y + shift.y, z + shift.z, dx, dy, dz);
    }

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void offgrid$destroyBothHalves(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!(state.getBlock() instanceof MixedSlabBlock)) return;
        ClientLevel level = (ClientLevel) (Object) this;
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return;
        ci.cancel();
        level.addDestroyBlockEffect(pos, entity.getFirst());
        level.addDestroyBlockEffect(pos, entity.getSecond());
    }

    //? if >=26.3 {
    /^@Inject(method = "addBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void offgrid$breakingEffects(BlockPos pos, Direction direction, BlockState state, CallbackInfo ci) {
        if (OverlayBreaking.isAimingAt(pos) || MixedSlabParticles.crack((ClientLevel) (Object) this, pos, direction)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "playBreakingSound", at = @At("HEAD"), argsOnly = true)
    private BlockState offgrid$halfHitSound(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return MixedSlabCracks.half(state, (ClientLevel) (Object) this, pos);
    }
    ^///?} else {
    @Inject(method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", at = @At("HEAD"), cancellable = true)
    private void offgrid$breakingEffects(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (OverlayBreaking.isAimingAt(pos) || MixedSlabParticles.crack((ClientLevel) (Object) this, pos, direction)) {
            ci.cancel();
        }
    }
    //?}
    *///?} else {
    @WrapMethod(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    private void offgrid$shiftParticle(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(options, x + shift.x, y + shift.y, z + shift.z, dx, dy, dz);
    }

    @WrapMethod(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V")
    private void offgrid$shiftForcedParticle(ParticleOptions options, boolean force, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(options, force, x + shift.x, y + shift.y, z + shift.z, dx, dy, dz);
    }

    @WrapMethod(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    private void offgrid$shiftVisibleParticle(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(options, x + shift.x, y + shift.y, z + shift.z, dx, dy, dz);
    }

    @WrapMethod(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V")
    private void offgrid$shiftForcedVisibleParticle(ParticleOptions options, boolean ignoreRange, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(options, ignoreRange, x + shift.x, y + shift.y, z + shift.z, dx, dy, dz);
    }
    //?}

    @WrapMethod(method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZJ)V")
    private void offgrid$shiftSound(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean delayed, long seed, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ClientLevel) (Object) this, x, y, z);
        original.call(x + shift.x, y + shift.y, z + shift.z, sound, source, volume, pitch, delayed, seed);
    }

    @WrapOperation(method = "doAnimateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void offgrid$animateOffgrid(Block block, BlockState state, Level level, BlockPos pos, RandomSource random, Operation<Void> original) {
        EffectOffset.run(EffectOffset.forBlock(level, pos, state), () -> original.call(block, state, level, pos, random));
        OffgridIndex index = OffgridIndex.of(level);
        Cell cell = index == null ? null : index.getCell(pos);
        if (cell == null || !cell.hasOverlays()) return;
        BlockPos host = pos.immutable();
        for (Overlay overlay : cell.overlays()) {
            OverlayContext.run(level, host, overlay, () -> overlay.getState().getBlock().animateTick(overlay.getState(), level, host, random));
        }
    }
}
