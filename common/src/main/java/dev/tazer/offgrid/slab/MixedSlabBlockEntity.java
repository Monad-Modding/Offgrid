package dev.tazer.offgrid.slab;

import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.world.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?} else if >=1.20.5 {
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
//?} else {
/*import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
*///?}

public class MixedSlabBlockEntity extends BlockEntity {

    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String FIRST_ENTITY = "first_block_entity";
    private static final String SECOND_ENTITY = "second_block_entity";

    @Nullable
    private BlockState first;
    @Nullable
    private BlockState second;
    @Nullable
    private CompoundTag firstEntityTag;
    @Nullable
    private CompoundTag secondEntityTag;
    @Nullable
    private BlockEntity firstEntity;
    @Nullable
    private BlockEntity secondEntity;

    public MixedSlabBlockEntity(BlockPos pos, BlockState state) {
        super(OffgridBlocks.getMixedSlabEntity(), pos, state);
    }

    @Nullable
    public BlockState getFirst() {
        return this.first;
    }

    @Nullable
    public BlockState getSecond() {
        return this.second;
    }

    @Nullable
    public BlockState getHalf(boolean second) {
        return second ? this.second : this.first;
    }

    public boolean isComplete() {
        return this.first != null && this.second != null && !this.first.isAir() && !this.second.isAir();
    }

    @Nullable
    public synchronized BlockEntity getHalfEntity(boolean second) {
        BlockState half = this.getHalf(second);
        BlockEntity existing = second ? this.secondEntity : this.firstEntity;
        if (half == null || !half.hasBlockEntity() || !(half.getBlock() instanceof EntityBlock entityBlock)) return null;
        if (existing != null) {
            if (existing.getLevel() != this.level && this.level != null) {
                existing.setLevel(this.level);
            }
            return existing;
        }
        if (this.level == null) return null;
        BlockEntity created = entityBlock.newBlockEntity(this.worldPosition, half);
        if (created == null) return null;
        CompoundTag tag = second ? this.secondEntityTag : this.firstEntityTag;
        if (tag != null) {
            BlockEntities.load(created, tag, this.level.registryAccess());
        }
        created.setLevel(this.level);
        if (second) {
            this.secondEntity = created;
        } else {
            this.firstEntity = created;
        }
        return created;
    }

    @Nullable
    public CompoundTag saveHalfEntity(boolean second) {
        BlockEntity live = second ? this.secondEntity : this.firstEntity;
        if (live != null && this.level != null) {
            CompoundTag saved = BlockEntities.save(live, this.level.registryAccess());
            if (second) {
                this.secondEntityTag = saved;
            } else {
                this.firstEntityTag = saved;
            }
        }
        return second ? this.secondEntityTag : this.firstEntityTag;
    }

    public void setHalfEntityTag(boolean second, @Nullable CompoundTag tag) {
        if (second) {
            this.secondEntityTag = tag;
            this.discardHalfEntity(true);
        } else {
            this.firstEntityTag = tag;
            this.discardHalfEntity(false);
        }
        this.setChanged();
    }

    public void swapHalfEntities() {
        CompoundTag firstTag = this.saveHalfEntity(false);
        CompoundTag secondTag = this.saveHalfEntity(true);
        this.discardHalfEntity(false);
        this.discardHalfEntity(true);
        this.firstEntityTag = secondTag;
        this.secondEntityTag = firstTag;
    }

    private void discardHalfEntity(boolean second) {
        BlockEntity live = second ? this.secondEntity : this.firstEntity;
        if (live != null) {
            live.setRemoved();
        }
        if (second) {
            this.secondEntity = null;
        } else {
            this.firstEntity = null;
        }
    }

    private void keepHalfEntity(boolean second, @Nullable BlockState previous, @Nullable BlockState next) {
        if (next != null && previous != null && next.getBlock() == previous.getBlock() && next.hasBlockEntity()) {
            BlockEntity live = second ? this.secondEntity : this.firstEntity;
            if (live != null) {
                live.setBlockState(next);
            }
            return;
        }
        this.discardHalfEntity(second);
        if (second) {
            this.secondEntityTag = null;
        } else {
            this.firstEntityTag = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.saveHalfEntity(false);
        this.saveHalfEntity(true);
        this.discardHalfEntity(false);
        this.discardHalfEntity(true);
    }

    public void setHalves(BlockState first, BlockState second) {
        this.keepHalfEntity(false, this.first, first);
        this.keepHalfEntity(true, this.second, second);
        this.first = first;
        this.second = second;
        this.onHalvesChanged();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    //? if >=26 {
    /*@Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.first != null) {
            output.store(FIRST, BlockState.CODEC, this.first);
        }
        if (this.second != null) {
            output.store(SECOND, BlockState.CODEC, this.second);
        }
        CompoundTag firstTag = this.saveHalfEntity(false);
        if (firstTag != null) {
            output.store(FIRST_ENTITY, CompoundTag.CODEC, firstTag);
        }
        CompoundTag secondTag = this.saveHalfEntity(true);
        if (secondTag != null) {
            output.store(SECOND_ENTITY, CompoundTag.CODEC, secondTag);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.first = input.read(FIRST, BlockState.CODEC).orElse(null);
        this.second = input.read(SECOND, BlockState.CODEC).orElse(null);
        this.loadHalfEntity(false, input.read(FIRST_ENTITY, CompoundTag.CODEC).orElse(null));
        this.loadHalfEntity(true, input.read(SECOND_ENTITY, CompoundTag.CODEC).orElse(null));
        this.refreshClient();
    }
    *///?} else if >=1.20.5 {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.write(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.read(tag, registries.lookupOrThrow(Registries.BLOCK));
    }
    //?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.write(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.read(tag, BuiltInRegistries.BLOCK.asLookup());
    }
    *///?}

    //? if <26 {
    private void write(CompoundTag tag) {
        if (this.first != null) {
            tag.put(FIRST, NbtUtils.writeBlockState(this.first));
        }
        if (this.second != null) {
            tag.put(SECOND, NbtUtils.writeBlockState(this.second));
        }
        CompoundTag firstTag = this.saveHalfEntity(false);
        if (firstTag != null) {
            tag.put(FIRST_ENTITY, firstTag);
        }
        CompoundTag secondTag = this.saveHalfEntity(true);
        if (secondTag != null) {
            tag.put(SECOND_ENTITY, secondTag);
        }
    }

    private void read(CompoundTag tag, HolderGetter<Block> blocks) {
        this.first = tag.contains(FIRST) ? NbtUtils.readBlockState(blocks, tag.getCompound(FIRST)) : null;
        this.second = tag.contains(SECOND) ? NbtUtils.readBlockState(blocks, tag.getCompound(SECOND)) : null;
        this.loadHalfEntity(false, tag.contains(FIRST_ENTITY) ? tag.getCompound(FIRST_ENTITY) : null);
        this.loadHalfEntity(true, tag.contains(SECOND_ENTITY) ? tag.getCompound(SECOND_ENTITY) : null);
        this.refreshClient();
    }
    //?}

    private void loadHalfEntity(boolean second, @Nullable CompoundTag tag) {
        BlockEntity live = second ? this.secondEntity : this.firstEntity;
        BlockState half = this.getHalf(second);
        if (live != null && (tag == null || half == null || live.getBlockState().getBlock() != half.getBlock())) {
            this.discardHalfEntity(second);
            live = null;
        }
        if (second) {
            this.secondEntityTag = tag;
        } else {
            this.firstEntityTag = tag;
        }
        if (live != null && tag != null && this.level != null) {
            live.setBlockState(half);
            BlockEntities.load(live, tag, this.level.registryAccess());
        }
    }

    protected void onHalvesChanged() {
    }

    private void refreshClient() {
        this.onHalvesChanged();
        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    //? if >=1.20.5 {
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
    //?} else {
    /*@Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    *///?}

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
