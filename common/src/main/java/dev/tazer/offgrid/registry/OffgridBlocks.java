package dev.tazer.offgrid.registry;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
//? if >=1.21.2 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
*///?}

import java.util.function.BiConsumer;

public final class OffgridBlocks {

    public static final ResourceLocation MIXED_SLAB_ID = Offgrid.id("mixed_slab");

    private static MixedSlabBlock mixedSlab;
    private static BlockEntityType<MixedSlabBlockEntity> mixedSlabEntity;

    private OffgridBlocks() {
    }

    public static MixedSlabBlock getMixedSlab() {
        return mixedSlab;
    }

    public static BlockEntityType<MixedSlabBlockEntity> getMixedSlabEntity() {
        return mixedSlabEntity;
    }

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        mixedSlab = Services.PLATFORM.createMixedSlab(mixedSlabProperties());
        registrar.accept(MIXED_SLAB_ID, mixedSlab);
    }

    public static void registerBlockEntities(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar) {
        mixedSlabEntity = Services.PLATFORM.createBlockEntityType(MixedSlabBlockEntity::new, mixedSlab);
        registrar.accept(MIXED_SLAB_ID, mixedSlabEntity);
    }

    private static BlockBehaviour.Properties mixedSlabProperties() {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2.0F, 6.0F).noLootTable()
                .lightLevel(state -> state.getValue(MixedSlabBlock.LIGHT))
                .isRedstoneConductor((state, level, pos) -> state.getValue(MixedSlabBlock.OPAQUE))
                .isSuffocating((state, level, pos) -> state.getValue(MixedSlabBlock.OPAQUE))
                //? if >=26.3 {
                /*.isViewBlocking((state, level, pos, nearPlane) -> state.getValue(MixedSlabBlock.OPAQUE));
                *///?} else {
                .isViewBlocking((state, level, pos) -> state.getValue(MixedSlabBlock.OPAQUE));
                //?}
        //? if >=1.21.2 {
        /*properties.setId(ResourceKey.create(Registries.BLOCK, MIXED_SLAB_ID));
        *///?}
        return properties;
    }
}
