package dev.tazer.offgrid;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class OffgridTags {

    public static final TagKey<Block> CAN_OFFSET = TagKey.create(Registries.BLOCK, Offgrid.id("can_offset"));
    public static final TagKey<Block> CAN_OVERLAY = TagKey.create(Registries.BLOCK, Offgrid.id("can_overlay"));
    public static final TagKey<Block> FACING_ATTACHING = TagKey.create(Registries.BLOCK, Offgrid.id("facing_attaching"));
    public static final TagKey<Block> CAN_BLOCKLOG = TagKey.create(Registries.BLOCK, Offgrid.id("can_blocklog"));
    public static final TagKey<Block> BLOCKLOGGABLE = TagKey.create(Registries.BLOCK, Offgrid.id("blockloggable"));
    public static final TagKey<Block> LAYERS = TagKey.create(Registries.BLOCK, Offgrid.id("layers"));
    public static final TagKey<EntityType<?>> SURFACE_ATTACHING = TagKey.create(Registries.ENTITY_TYPE, Offgrid.id("surface_attaching"));

    private OffgridTags() {
    }
}
