package dev.tazer.offgrid.slab;

import dev.tazer.offgrid.registry.OffgridBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
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

    @Nullable
    private BlockState first;
    @Nullable
    private BlockState second;

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

    public void setHalves(BlockState first, BlockState second) {
        this.first = first;
        this.second = second;
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.first = input.read(FIRST, BlockState.CODEC).orElse(null);
        this.second = input.read(SECOND, BlockState.CODEC).orElse(null);
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
    }

    private void read(CompoundTag tag, HolderGetter<Block> blocks) {
        this.first = tag.contains(FIRST) ? NbtUtils.readBlockState(blocks, tag.getCompound(FIRST)) : null;
        this.second = tag.contains(SECOND) ? NbtUtils.readBlockState(blocks, tag.getCompound(SECOND)) : null;
        this.refreshClient();
    }
    //?}

    private void refreshClient() {
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
