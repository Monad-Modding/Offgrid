//? if >=1.21.4 {
/*package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @WrapOperation(method = "handlePickItemFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack offgrid$pickSlabHalf(BlockState state, LevelReader level, BlockPos pos, boolean includeData, Operation<ItemStack> original) {
        if (!(state.getBlock() instanceof MixedSlabBlock)) return original.call(state, level, pos, includeData);
        BlockState half = MixedSlabs.half(level, pos, MixedSlabs.targetsSecond(this.player, pos, state.getValue(MixedSlabBlock.AXIS)));
        return half == null ? ItemStack.EMPTY : original.call(half, level, pos, includeData);
    }
}
*///?}
