package dev.tazer.offgrid.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Transaction {

    @Nullable
    private final Transaction previous;
    private final List<Runnable> deferred = new ArrayList<>();

    private boolean refused;

    private Transaction(@Nullable Transaction previous) {
        this.previous = previous;
    }

    public static InteractionResult run(Player player, Supplier<InteractionResult> action) {
        Inventory inventory = player.getInventory();
        ItemStack[] before = new ItemStack[inventory.getContainerSize()];
        for (int slot = 0; slot < before.length; slot++) {
            before[slot] = inventory.getItem(slot).copy();
        }
        Transaction transaction = new Transaction(Scope.get(Scope.TRANSACTION));
        InteractionResult result = Scope.with(Scope.TRANSACTION, transaction, action);
        if (transaction.refused) {
            for (int slot = 0; slot < before.length; slot++) {
                if (!ItemStack.matches(inventory.getItem(slot), before[slot])) {
                    inventory.setItem(slot, before[slot]);
                }
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.sendAllDataToRemote();
            }
            return InteractionResult.FAIL;
        }
        for (Runnable effect : transaction.deferred) {
            if (transaction.previous != null) {
                transaction.previous.deferred.add(effect);
            } else {
                effect.run();
            }
        }
        return result;
    }

    public static boolean isOpen() {
        return Scope.get(Scope.TRANSACTION) != null;
    }

    public static boolean defer(Runnable effect) {
        Transaction transaction = Scope.get(Scope.TRANSACTION);
        if (transaction == null) return false;
        transaction.deferred.add(effect);
        return true;
    }

    public static void refuse() {
        Transaction transaction = Scope.get(Scope.TRANSACTION);
        if (transaction != null) {
            transaction.refused = true;
        }
    }
}
