//? if >=26 {
/*package dev.tazer.offgrid.mixin.compat.lithium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@IfModPresent("lithium")
@Mixin(ChunkAwareBlockCollisionSweeper.class)
public interface ChunkAwareBlockCollisionSweeperAccessor {

    @Accessor("world")
    Level offgrid$getWorld();
}
*///?}
