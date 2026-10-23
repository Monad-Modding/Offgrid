package dev.tazer.offgrid.mixin.light;

import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LayerLightSectionStorage.class)
public interface LayerLightSectionStorageAccessor {

    @Invoker("lightOnInSection")
    boolean offgrid$lightOnInSection(long section);
}
