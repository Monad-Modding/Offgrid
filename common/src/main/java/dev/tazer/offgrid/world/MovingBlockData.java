package dev.tazer.offgrid.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tazer.offgrid.network.MovingCell;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record MovingBlockData(@Nullable BlockState first, @Nullable BlockState second, @Nullable AssembledCell cell, Stationary stationary) {

    public static final String KEY = "offgrid:moving_block";
    public static final Codec<MovingBlockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("first").forGetter(data -> Optional.ofNullable(data.first())),
            BlockState.CODEC.optionalFieldOf("second").forGetter(data -> Optional.ofNullable(data.second())),
            AssembledCell.CODEC.optionalFieldOf("cell").forGetter(data -> Optional.ofNullable(data.cell())),
            Stationary.CODEC.optionalFieldOf("stationary", Stationary.NONE).forGetter(MovingBlockData::stationary)
    ).apply(instance, (first, second, cell, stationary) -> new MovingBlockData(first.orElse(null), second.orElse(null), cell.orElse(null), stationary)));

    public static void capture(Level level, BlockPos source, BlockEntity moving, @Nullable MixedSlabs.Merge merge) {
        if (!(moving instanceof MovingBlockDataHolder holder)) return;
        BlockState first = null;
        BlockState second = null;
        Stationary stationary = Stationary.NONE;
        if (merge != null) {
            first = merge.first();
            second = merge.second();
            stationary = merge.stationary();
        } else if (level.getBlockEntity(source) instanceof MixedSlabBlockEntity slab && slab.isComplete()) {
            first = slab.getFirst();
            second = slab.getSecond();
        }
        AssembledCell cell = capturedCell(level, source, moving.getBlockPos());
        if (first != null || cell != null) {
            holder.offgrid$setMovingBlockData(new MovingBlockData(first, second, cell, stationary));
        }
    }

    @Nullable
    private static AssembledCell capturedCell(Level level, BlockPos source, BlockPos moving) {
        if (!(level instanceof ServerLevel serverLevel)) {
            OffgridIndex index = OffgridIndex.of(level);
            return index == null ? null : index.takeMovingCell(moving.asLong(), level.getGameTime());
        }
        AssembledCell cell = AssembledCell.assemble(serverLevel, source);
        if (cell != null && !cell.overlays().isEmpty()) {
            Cells.sendTracking(serverLevel, source, OffgridNetwork.MOVING_CELL, new MovingCell(moving.asLong(), cell));
        }
        return cell;
    }

    public static void restore(@Nullable Level level, BlockPos pos, BlockEntity moving) {
        if (level == null || !(moving instanceof MovingBlockDataHolder holder)) return;
        MovingBlockData data = holder.offgrid$getMovingBlockData();
        if (data == null) return;
        holder.offgrid$setMovingBlockData(null);
        if (data.hasHalves() && level.getBlockEntity(pos) instanceof MixedSlabBlockEntity slab) {
            slab.setHalves(data.first(), data.second());
        }
        if (data.cell() != null && level instanceof ServerLevel serverLevel) {
            data.cell().disassemble(serverLevel, pos, Rotation.NONE);
            AssembledCell.reevaluate(serverLevel, List.of(pos));
        }
    }

    public boolean hasHalves() {
        return this.first != null && this.second != null;
    }

    public enum Stationary implements StringRepresentable {
        NONE("none"),
        FIRST("first"),
        SECOND("second");

        public static final Codec<Stationary> CODEC = StringRepresentable.fromEnum(Stationary::values);

        private final String name;

        Stationary(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
