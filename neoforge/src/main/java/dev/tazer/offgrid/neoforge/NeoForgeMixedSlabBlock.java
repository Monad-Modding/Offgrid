package dev.tazer.offgrid.neoforge;

import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
//? if <26 {
import net.minecraft.world.phys.HitResult;
//?}
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public final class NeoForgeMixedSlabBlock extends MixedSlabBlock {

    public NeoForgeMixedSlabBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    private static BlockState under(BlockState state, BlockGetter level, BlockPos pos, Vec3 point) {
        return MixedSlabs.half(level, pos, MixedSlabs.containsSecond(point, pos, state.getValue(AXIS)));
    }

    //? if >=26 {
    /*@Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        return breakHalf(level, pos, state, player) || super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }
    *///?} else {
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return breakHalf(level, pos, state, player) || super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
    //?}

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState half = targetedHalf(state, level, pos, player);
        return half == null ? super.canHarvestBlock(state, level, pos, player) : half.canHarvestBlock(level, pos, player);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState half;
        if (entity instanceof Player player && !pos.equals(player.getOnPosLegacy())) {
            half = targetedHalf(state, level, pos, player);
        } else if (entity != null) {
            half = under(state, level, pos, entity.position());
        } else {
            half = MixedSlabs.half(level, pos, true);
        }
        return half == null ? super.getSoundType(state, level, pos, entity) : half.getSoundType(level, pos, entity);
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        BlockState half = MixedSlabs.half(level, pos, true);
        return half == null ? defaultColor : half.getMapColor(level, pos);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState half = entity == null ? MixedSlabs.half(level, pos, true) : under(state, level, pos, entity.position());
        return half == null ? super.getFriction(state, level, pos, entity) : half.getFriction(level, pos, entity);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return super.getExplosionResistance(state, level, pos, explosion);
        return Math.max(entity.getFirst().getExplosionResistance(level, pos, explosion), entity.getSecond().getExplosionResistance(level, pos, explosion));
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return 0;
        return Math.max(entity.getFirst().getFlammability(level, pos, direction), entity.getSecond().getFlammability(level, pos, direction));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return 0;
        return Math.max(entity.getFirst().getFireSpreadSpeed(level, pos, direction), entity.getSecond().getFireSpreadSpeed(level, pos, direction));
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return null;
        Direction.Axis axis = state.getValue(AXIS);
        boolean second = MixedSlabs.targetsSecond(context.getClickLocation(), pos, axis);
        BlockState half = entity.getHalf(second);
        BlockState modified = half.getToolModifiedState(context, ability, simulate);
        if (modified == null || modified == half || !MixedSlabs.keepsShape(modified, half)) return null;
        BlockState first = second ? entity.getFirst() : modified;
        BlockState other = second ? modified : entity.getSecond();
        if (!simulate) {
            entity.setHalves(first, other);
        }
        return MixedSlabs.mixedState(axis, first, other);
    }

    //? if >=26 {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        BlockState half = MixedSlabs.half(level, pos, MixedSlabs.targetsSecond(player, pos, state.getValue(AXIS)));
        return half == null ? ItemStack.EMPTY : half.getCloneItemStack(pos, level, includeData, player);
    }
    *///?} else {
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockState half = MixedSlabs.half(level, pos, MixedSlabs.targetsSecond(target, pos, state.getValue(AXIS)));
        return half == null ? ItemStack.EMPTY : half.getCloneItemStack(target, level, pos, player);
    }
    //?}

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        BlockState half = under(state, level, pos, entity.position());
        if (half == null) return false;
        Vec3 motion = entity.getDeltaMovement();
        BlockPos at = entity.blockPosition();
        double x = entity.getX() + (level.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
        double z = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
        if (at.getX() != pos.getX()) {
            x = Mth.clamp(x, pos.getX(), pos.getX() + 1.0);
        }
        if (at.getZ() != pos.getZ()) {
            z = Mth.clamp(z, pos.getZ(), pos.getZ() + 1.0);
        }
        level.addParticle(particle(half, pos), x, entity.getY() + 0.1, z, motion.x * -4.0, 1.5, motion.z * -4.0);
        return true;
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel level, BlockPos pos, BlockState stateAgain, LivingEntity entity, int count) {
        BlockState half = under(state, level, pos, entity.position());
        if (half == null) return false;
        level.sendParticles(particle(half, pos), entity.getX(), entity.getY(), entity.getZ(), count, 0.0, 0.0, 0.0, 0.15F);
        return true;
    }

    private static BlockParticleOption particle(BlockState half, BlockPos pos) {
        //? if >=26 {
        /*return new BlockParticleOption(ParticleTypes.BLOCK, half, pos);
        *///?} else {
        return new BlockParticleOption(ParticleTypes.BLOCK, half).setPos(pos);
        //?}
    }
}
