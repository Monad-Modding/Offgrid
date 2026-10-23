//? if >=1.21 {
package dev.tazer.offgrid.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@IfModPresent("sodium")
@Mixin(LevelSlice.class)
public abstract class LevelSliceMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    private ClientLevel level;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.level);
    }
}
//?}
