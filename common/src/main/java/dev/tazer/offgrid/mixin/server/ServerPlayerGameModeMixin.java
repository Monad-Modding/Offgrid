package dev.tazer.offgrid.mixin.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @WrapMethod(method = "useItemOn")
    private InteractionResult offgrid$transactUse(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit, Operation<InteractionResult> original) {
        return Transaction.run(player, () -> original.call(player, level, stack, hand, hit));
    }
}
