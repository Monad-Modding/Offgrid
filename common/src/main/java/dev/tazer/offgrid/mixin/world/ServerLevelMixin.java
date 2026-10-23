package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.EffectOffset;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayTicker;
import dev.tazer.offgrid.world.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
//? if >=26 {
/*import net.minecraft.world.entity.Entity;
*///?} else {
import net.minecraft.world.entity.player.Player;
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.3 {
/*import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
*///?}

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "blockEvent", at = @At("HEAD"), cancellable = true)
    private void offgrid$queueOverlayEvent(BlockPos pos, Block block, int type, int param, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame != null && frame.getOverlay() != null) {
            OverlayTicker.queueEvent(level, frame.getHost(), frame.getOverlay(), block, type, param);
            ci.cancel();
        }
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"), cancellable = true)
    private void offgrid$syncOverlay(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame == null || frame.getOverlay() == null) return;
        if (Cells.contains(level, frame.getHost(), frame.getOverlay())) {
            Cells.sync(level, frame.getHost());
        }
        ci.cancel();
    }

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    /*private void offgrid$overlayDestroyEffects(@Nullable Entity source, int type, BlockPos pos, int data, CallbackInfo ci) {
    *///?} else {
    private void offgrid$overlayDestroyEffects(@Nullable Player source, int type, BlockPos pos, int data, CallbackInfo ci) {
    //?}
        if (type != LevelEvent.PARTICLES_DESTROY_BLOCK) return;
        OverlayContext.Frame frame = OverlayContext.at((ServerLevel) (Object) this, pos);
        if (frame != null && frame.getOverlay() != null) {
            frame.requestDestroyEffects();
            ci.cancel();
        }
    }

    //? if >=26.3 {
    /*@WrapMethod(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDIDDDDDDLnet/minecraft/network/protocol/game/ClientboundLevelParticlesPacket$RandomizationType;)I")
    private <T extends ParticleOptions> int offgrid$shiftParticles(T type, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, int count, double dx, double dy, double dz, double xSpeed, double ySpeed, double zSpeed, ClientboundLevelParticlesPacket.RandomizationType randomization, Operation<Integer> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        return original.call(type, overrideLimiter, alwaysShow, x + shift.x, y + shift.y, z + shift.z, count, dx, dy, dz, xSpeed, ySpeed, zSpeed, randomization);
    }
    *///?} else if >=26 {
    /*@WrapMethod(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDIDDDD)I")
    private <T extends ParticleOptions> int offgrid$shiftParticles(T type, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, int count, double dx, double dy, double dz, double speed, Operation<Integer> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        return original.call(type, overrideLimiter, alwaysShow, x + shift.x, y + shift.y, z + shift.z, count, dx, dy, dz, speed);
    }
    *///?} else {
    @WrapMethod(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I")
    private <T extends ParticleOptions> int offgrid$shiftParticles(T type, double x, double y, double z, int count, double dx, double dy, double dz, double speed, Operation<Integer> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        return original.call(type, x + shift.x, y + shift.y, z + shift.z, count, dx, dy, dz, speed);
    }
    //?}

    //? if >=26 {
    /*@WrapMethod(method = "sendParticles(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/particles/ParticleOptions;ZZDDDIDDDD)Z")
    private <T extends ParticleOptions> boolean offgrid$shiftPlayerParticles(ServerPlayer player, T type, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, int count, double dx, double dy, double dz, double speed, Operation<Boolean> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        return original.call(player, type, overrideLimiter, alwaysShow, x + shift.x, y + shift.y, z + shift.z, count, dx, dy, dz, speed);
    }

    @WrapMethod(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void offgrid$shiftSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        if (Transaction.defer(() -> original.call(except, x + shift.x, y + shift.y, z + shift.z, sound, source, volume, pitch, seed))) return;
        original.call(except, x + shift.x, y + shift.y, z + shift.z, sound, source, volume, pitch, seed);
    }
    *///?} else {
    @WrapMethod(method = "sendParticles(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/particles/ParticleOptions;ZDDDIDDDD)Z")
    private <T extends ParticleOptions> boolean offgrid$shiftPlayerParticles(ServerPlayer player, T type, boolean longDistance, double x, double y, double z, int count, double dx, double dy, double dz, double speed, Operation<Boolean> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        return original.call(player, type, longDistance, x + shift.x, y + shift.y, z + shift.z, count, dx, dy, dz, speed);
    }

    @WrapMethod(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void offgrid$shiftSound(@Nullable Player except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, Operation<Void> original) {
        Vec3 shift = EffectOffset.at((ServerLevel) (Object) this, x, y, z);
        if (Transaction.defer(() -> original.call(except, x + shift.x, y + shift.y, z + shift.z, sound, source, volume, pitch, seed))) return;
        original.call(except, x + shift.x, y + shift.y, z + shift.z, sound, source, volume, pitch, seed);
    }
    //?}

    //? if >=1.21 {
    @WrapMethod(method = "gameEvent")
    private void offgrid$deferGameEvent(Holder<GameEvent> event, Vec3 position, GameEvent.Context context, Operation<Void> original) {
        if (Transaction.defer(() -> original.call(event, position, context))) return;
        original.call(event, position, context);
    }
    //?} else {
    /*@WrapMethod(method = "gameEvent")
    private void offgrid$deferGameEvent(GameEvent event, Vec3 position, GameEvent.Context context, Operation<Void> original) {
        if (Transaction.defer(() -> original.call(event, position, context))) return;
        original.call(event, position, context);
    }
    *///?}

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void offgrid$shiftRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Operation<Void> original) {
        EffectOffset.run(EffectOffset.forBlock(level, pos, state), () -> original.call(state, level, pos, random));
    }

    @WrapOperation(method = "tickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void offgrid$shiftScheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Operation<Void> original) {
        EffectOffset.run(EffectOffset.forBlock(level, pos, state), () -> original.call(state, level, pos, random));
    }
}
