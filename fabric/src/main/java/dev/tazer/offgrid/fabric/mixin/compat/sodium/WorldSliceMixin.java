//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@IfModPresent("sodium")
@Mixin(WorldSlice.class)
public abstract class WorldSliceMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    private ClientLevel world;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.world);
    }
}
*///?}
