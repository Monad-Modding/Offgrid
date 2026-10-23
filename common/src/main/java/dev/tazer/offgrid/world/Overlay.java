package dev.tazer.offgrid.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class Overlay {

    public static final Codec<Overlay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(Overlay::getId),
            BlockState.CODEC.fieldOf("state").forGetter(Overlay::getState),
            Vec3.CODEC.fieldOf("offset").forGetter(Overlay::getOffset),
            CompoundTag.CODEC.optionalFieldOf("block_entity").forGetter(overlay -> Optional.ofNullable(overlay.saveBlockEntity()))
    ).apply(instance, (id, state, offset, tag) -> new Overlay(id, state, offset, tag.orElse(null))));

    private final int id;
    private final Vec3 offset;
    private volatile BlockState state;
    @Nullable
    private CompoundTag blockEntityTag;
    @Nullable
    private BlockEntity blockEntity;
    @Nullable
    private BlockEntityTicker<BlockEntity> ticker;
    private boolean tickerResolved;

    public Overlay(int id, BlockState state, Vec3 offset, @Nullable CompoundTag blockEntityTag) {
        this.id = id;
        this.state = state;
        this.offset = offset;
        this.blockEntityTag = blockEntityTag;
    }

    public int getId() {
        return this.id;
    }

    public BlockState getState() {
        return this.state;
    }

    public Vec3 getOffset() {
        return this.offset;
    }

    @Nullable
    public CompoundTag getBlockEntityTag() {
        return this.blockEntityTag;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public void setState(BlockState state) {
        this.state = state;
        this.tickerResolved = false;
        if (this.blockEntity == null) return;
        if (this.blockEntity.getBlockState().getBlock() == state.getBlock() && state.hasBlockEntity()) {
            this.blockEntity.setBlockState(state);
            return;
        }
        this.discardBlockEntity();
        this.blockEntityTag = null;
    }

    @Nullable
    public BlockEntity materialize(Level level, BlockPos host) {
        if (this.blockEntity != null) {
            if (this.blockEntity.getLevel() != level) {
                this.blockEntity.setLevel(level);
            }
            return this.blockEntity;
        }
        if (!(this.state.getBlock() instanceof EntityBlock entityBlock)) return null;
        BlockEntity created = entityBlock.newBlockEntity(host.immutable(), this.state);
        if (created == null) return null;
        ((OverlayBlockEntity) created).offgrid$setOverlay(this);
        if (this.blockEntityTag != null) {
            BlockEntities.load(created, this.blockEntityTag, level.registryAccess());
        }
        created.setLevel(level);
        this.blockEntity = created;
        this.tickerResolved = false;
        return created;
    }

    public void loadBlockEntity(Level level, @Nullable CompoundTag tag) {
        this.blockEntityTag = tag;
        if (this.blockEntity != null && tag != null) {
            BlockEntities.load(this.blockEntity, tag, level.registryAccess());
        }
    }

    @Nullable
    public CompoundTag saveBlockEntity() {
        BlockEntity current = this.blockEntity;
        if (current != null && !current.isRemoved() && current.getLevel() != null) {
            this.blockEntityTag = BlockEntities.save(current, current.getLevel().registryAccess());
        }
        return this.blockEntityTag;
    }

    public void discardBlockEntity() {
        if (this.blockEntity != null) {
            this.blockEntity.setRemoved();
            this.blockEntity = null;
        }
        this.ticker = null;
        this.tickerResolved = false;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public BlockEntityTicker<BlockEntity> getTicker(Level level) {
        if (this.tickerResolved) return this.ticker;
        this.tickerResolved = true;
        this.ticker = null;
        if (this.blockEntity != null && this.state.getBlock() instanceof EntityBlock entityBlock) {
            this.ticker = entityBlock.getTicker(level, this.state, (BlockEntityType<BlockEntity>) this.blockEntity.getType());
        }
        return this.ticker;
    }

    public void stopTicking() {
        this.ticker = null;
        this.tickerResolved = true;
    }

    public boolean isSamePlacement(BlockState state, Vec3 offset) {
        return this.state.getBlock() == state.getBlock() && this.offset.distanceToSqr(offset) < 1.0E-6;
    }
}
