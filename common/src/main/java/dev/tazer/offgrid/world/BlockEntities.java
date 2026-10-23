package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import dev.tazer.offgrid.Offgrid;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
*///?}

public final class BlockEntities {

    private BlockEntities() {
    }

    @Nullable
    public static CompoundTag saveAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? null : save(blockEntity, level.registryAccess());
    }

    public static CompoundTag save(BlockEntity blockEntity, HolderLookup.Provider registries) {
        //? if >=1.20.5 {
        return blockEntity.saveWithoutMetadata(registries);
        //?} else {
        /*return blockEntity.saveWithoutMetadata();
        *///?}
    }

    public static CompoundTag updateTag(BlockEntity blockEntity, HolderLookup.Provider registries) {
        //? if >=1.20.5 {
        return blockEntity.getUpdateTag(registries);
        //?} else {
        /*return blockEntity.getUpdateTag();
        *///?}
    }

    public static void load(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries) {
        //? if >=26 {
        /*try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Offgrid.LOGGER)) {
            blockEntity.loadWithComponents(TagValueInput.create(reporter, registries, tag));
        }
        *///?} else if >=1.20.5 {
        blockEntity.loadWithComponents(tag, registries);
        //?} else {
        /*blockEntity.load(tag);
        *///?}
    }
}
