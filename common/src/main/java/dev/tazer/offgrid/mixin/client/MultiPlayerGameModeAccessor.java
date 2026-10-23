package dev.tazer.offgrid.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Invoker("ensureHasSentCarriedItem")
    void offgrid$ensureHasSentCarriedItem();

    @Invoker("performUseItemOn")
    InteractionResult offgrid$performUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit);
}
