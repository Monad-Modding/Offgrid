//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.schematics.cannon.LaunchedItem;
import dev.tazer.offgrid.compat.create.OffsetLaunch;
import dev.tazer.offgrid.compat.create.PrintedOverlay;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@IfModPresent("create")
@Mixin(LaunchedItem.ForBlockState.class)
public abstract class LaunchedBlockStateMixin implements OffsetLaunch {

    @Unique
    private static final String offgrid$OFFSET = "offgrid:offset";

    @Unique
    @Nullable
    private Vec3 offgrid$offset;

    @Override
    public void offgrid$setOffset(@Nullable Vec3 offset) {
        this.offgrid$offset = offset;
    }

    @Inject(method = "place", at = @At("TAIL"), remap = false)
    private void offgrid$placeOffset(CallbackInfo ci, @Local(argsOnly = true) Level world) {
        if (this.offgrid$offset == null || !(world instanceof ServerLevel level)) return;
        LaunchedItem.ForBlockState self = (LaunchedItem.ForBlockState) (Object) this;
        PrintedOverlay.printOffset(level, self.target, self.state, this.offgrid$offset);
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"), remap = false)
    private void offgrid$saveOffset(CallbackInfoReturnable<CompoundTag> cir) {
        if (this.offgrid$offset == null) return;
        ListTag offset = new ListTag();
        offset.add(DoubleTag.valueOf(this.offgrid$offset.x));
        offset.add(DoubleTag.valueOf(this.offgrid$offset.y));
        offset.add(DoubleTag.valueOf(this.offgrid$offset.z));
        cir.getReturnValue().put(offgrid$OFFSET, offset);
    }

    @Inject(method = "readNBT", at = @At("TAIL"), remap = false)
    private void offgrid$loadOffset(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) CompoundTag tag) {
        ListTag offset = tag.getList(offgrid$OFFSET, Tag.TAG_DOUBLE);
        this.offgrid$offset = offset.size() == 3 ? new Vec3(offset.getDouble(0), offset.getDouble(1), offset.getDouble(2)) : null;
    }
}
//?}
