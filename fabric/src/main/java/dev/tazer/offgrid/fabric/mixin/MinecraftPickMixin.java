//? if <1.21.4 {
package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=1.20.5 {
import net.minecraft.world.level.LevelReader;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

@Mixin(Minecraft.class)
public abstract class MinecraftPickMixin {

    //? if >=1.20.5 {
    @WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack offgrid$pickSlabHalf(Block block, LevelReader level, BlockPos pos, BlockState state, Operation<ItemStack> original) {
    //?} else {
    /*@WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getCloneItemStack(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack offgrid$pickSlabHalf(Block block, BlockGetter level, BlockPos pos, BlockState state, Operation<ItemStack> original) {
    *///?}
        HitResult hit = ((Minecraft) (Object) this).hitResult;
        if (!(block instanceof MixedSlabBlock) || hit == null) return original.call(block, level, pos, state);
        BlockState half = MixedSlabs.half(level, pos, MixedSlabs.targetsSecond(hit, pos, state.getValue(MixedSlabBlock.AXIS)));
        return half == null ? ItemStack.EMPTY : original.call(half.getBlock(), level, pos, half);
    }
}
//?}
