package dev.tazer.offgrid.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class OffgridCommand {

    private OffgridCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("offgrid")
                //? if >=26 {
                /*.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                *///?} else {
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                //?}
                .then(Commands.literal("inspect").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(OffgridCommand::inspect)))
                .then(Commands.literal("clear").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(OffgridCommand::clear)));
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        ServerLevel level = context.getSource().getLevel();
        Cell cell = Cells.get(level, pos);
        if (cell == null) {
            context.getSource().sendSuccess(() -> Component.translatable("commands.offgrid.inspect.empty", pos.toShortString()), false);
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.offgrid.inspect.cell", pos.toShortString(), format(cell.offset()), cell.overlays().size()), false);
        for (Overlay overlay : cell.overlays()) {
            context.getSource().sendSuccess(() -> Component.translatable("commands.offgrid.inspect.overlay", overlay.getId(), overlay.getState().toString(), format(overlay.getOffset())), false);
        }
        return cell.overlays().size();
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        ServerLevel level = context.getSource().getLevel();
        int count = Cells.overlays(level, pos).size();
        Overlays.destroyAll(level, pos);
        Offsets.set(level, pos, null);
        context.getSource().sendSuccess(() -> Component.translatable("commands.offgrid.clear", count, pos.toShortString()), true);
        return count;
    }

    private static String format(@Nullable Vec3 offset) {
        if (offset == null) return "-";
        return String.format("%.4f %.4f %.4f", offset.x, offset.y, offset.z);
    }
}
