package dev.tazer.offgrid.fabric.data;

import dev.tazer.offgrid.OffgridTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
//? if >=26 {
/*import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
*///?} else {
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
//?}

import java.util.concurrent.CompletableFuture;

//? if >=26 {
/*public final class OffgridEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
*///?} else {
public final class OffgridEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
//?}

    public OffgridEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (String id : new String[]{"exposure:photograph_frame", "exposure:glass_photograph_frame", "exposure:glow_photograph_frame"}) {
            //? if >=26 {
            /*this.builder(OffgridTags.SURFACE_ATTACHING).addOptional(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse(id)));
            *///?} else if >=1.21 {
            this.getOrCreateTagBuilder(OffgridTags.SURFACE_ATTACHING).addOptional(ResourceLocation.parse(id));
            //?} else {
            /*this.getOrCreateTagBuilder(OffgridTags.SURFACE_ATTACHING).addOptional(new ResourceLocation(id));
            *///?}
        }
    }
}
