package dev.tazer.offgrid.slab;

import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.MovingBlockData;
import dev.tazer.offgrid.world.MovingBlockDataHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
//? if >=1.20.5 && <26.3 {
import com.mojang.serialization.MapCodec;
//?}
//? if >=1.20.5 && <1.21.2 {
import net.minecraft.world.ItemInteractionResult;
//?} else {
/*import net.minecraft.world.InteractionResult;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixedSlabBlock extends BaseEntityBlock {

    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<MixedSlabBlock> CODEC = simpleCodec(MixedSlabBlock::new);
    //?}
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty OPAQUE = BooleanProperty.create("opaque");
    public static final BooleanProperty WEATHERING = BooleanProperty.create("weathering");
    private static final float WEATHER_CHANCE = 0.05688889F;

    public MixedSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(LIGHT, 0).setValue(OPAQUE, true).setValue(WEATHERING, false));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, LIGHT, OPAQUE, WEATHERING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        BlockState placed = MixedSlabs.placedHalf();
        return placed == null ? super.getSoundType(state) : placed.getSoundType();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Services.PLATFORM.createMixedSlabEntity(pos, state);
    }

    @Nullable
    protected static BlockState targetedHalf(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return MixedSlabs.half(level, pos, MixedSlabs.targetsSecond(player, pos, state.getValue(AXIS)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            return MixedSlabs.halfShape(state.getValue(AXIS), MixedSlabs.targetsSecond(player, pos, state.getValue(AXIS)));
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    //? if >=1.21.2 {
    /*public VoxelShape getOcclusionShape(BlockState state) {
    *///?} else {
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
    //?}
        return state.getValue(OPAQUE) ? Shapes.block() : Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return !state.getValue(OPAQUE);
    }

    @Override
    //? if >=1.21.2 {
    /*public boolean propagatesSkylightDown(BlockState state) {
    *///?} else {
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
    //?}
        return false;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState half = targetedHalf(state, level, pos, player);
        return half == null ? super.getDestroyProgress(state, player, level, pos) : half.getDestroyProgress(player, level, pos);
    }

    //? if >=1.21 {
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState half = targetedHalf(state, level, pos, player);
        if (half == null) return super.playerWillDestroy(level, pos, state, player);
        level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(half));
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, half));
        return state;
    }
    //?} else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState half = targetedHalf(state, level, pos, player);
        if (half == null) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }
        level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(half));
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, half));
    }
    *///?}

    @Override
    //? if >=26.3 {
    /*public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    *///?} else {
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    //?}
        BlockState half = blockEntity instanceof MixedSlabBlockEntity entity && entity.isComplete() ? entity.getHalf(MixedSlabs.targetsSecond(player, pos, state.getValue(AXIS))) : null;
        if (half == null) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
            return;
        }
        player.awardStat(Stats.BLOCK_MINED.get(half.getBlock()));
        player.causeFoodExhaustion(0.005F);
        Block.dropResources(half, level, pos, null, player, tool);
    }

    public static boolean breakHalf(Level level, BlockPos pos, BlockState state, Player player) {
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return false;
        BlockState remaining = entity.getHalf(!MixedSlabs.targetsSecond(player, pos, state.getValue(AXIS)));
        return remaining != null && level.setBlock(pos, remaining, level.isClientSide() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(WEATHERING);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return;
        BlockState first = weather(entity.getFirst(), level, pos, random);
        BlockState second = weather(entity.getSecond(), level, pos, random);
        if (first != entity.getFirst() || second != entity.getSecond()) {
            MixedSlabs.replaceHalves(level, pos, entity, first, second);
        }
    }

    private static BlockState weather(BlockState half, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(half.getBlock() instanceof ChangeOverTimeBlock<?> aging) || random.nextFloat() >= WEATHER_CHANCE) return half;
        //? if >=1.21.2 {
        /*return aging.getNextState(half, level, pos, random).orElse(half);
        *///?} else {
        return aging.getNext(half).orElse(half);
        //?}
    }

    //? if >=1.21.2 {
    /*@Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.wax(stack, state, level, pos, player, hit) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    *///?} else if >=1.20.5 {
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.wax(stack, state, level, pos, player, hit) ? ItemInteractionResult.sidedSuccess(level.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    //?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.wax(player.getItemInHand(hand), state, level, pos, player, hit) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
    *///?}

    private boolean wax(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(stack.getItem() instanceof HoneycombItem)) return false;
        MixedSlabBlockEntity entity = MixedSlabs.entity(level, pos);
        if (entity == null) return false;
        boolean second = MixedSlabs.targetsSecond(hit, pos, state.getValue(AXIS));
        Optional<BlockState> waxed = HoneycombItem.getWaxed(entity.getHalf(second));
        if (waxed.isEmpty()) return false;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        MixedSlabs.replaceHalf(level, pos, second, waxed.get());
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
        level.levelEvent(player, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, pos, 0);
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MixedSlabBlockEntity entity && entity.isComplete()) return dropHalves(state, params, entity.getFirst(), entity.getSecond());
        MovingBlockData moving = blockEntity instanceof MovingBlockDataHolder holder ? holder.offgrid$getMovingBlockData() : null;
        if (moving == null || !moving.hasHalves()) return List.of();
        return dropHalves(state, params, moving.first(), moving.second());
    }

    private static List<ItemStack> dropHalves(BlockState state, LootParams.Builder params, BlockState first, BlockState second) {
        List<ItemStack> drops = new ArrayList<>(first.getDrops(params.withParameter(LootContextParams.BLOCK_STATE, first)));
        drops.addAll(second.getDrops(params.withParameter(LootContextParams.BLOCK_STATE, second)));
        params.withParameter(LootContextParams.BLOCK_STATE, state);
        return drops;
    }
}
