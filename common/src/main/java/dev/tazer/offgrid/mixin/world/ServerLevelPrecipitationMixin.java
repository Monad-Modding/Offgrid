package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.world.Layers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26 {
/*import net.minecraft.world.level.gamerules.GameRules;
*///?} else {
import net.minecraft.world.level.GameRules;
//?}

@Mixin(ServerLevel.class)
public abstract class ServerLevelPrecipitationMixin {

    //? if >=1.21 {
    @WrapOperation(method = "tickPrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    //?} else {
    /*@WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    *///?}
    private boolean offgrid$snowAroundHosts(Biome biome, LevelReader reader, BlockPos top, Operation<Boolean> original) {
        boolean snows = original.call(biome, reader, top);
        if (!snows) {
            ServerLevel level = (ServerLevel) (Object) this;
            //? if >=26 {
            /*int maxLayers = level.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT);
            *///?} else {
            int maxLayers = level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT);
            //?}
            Layers.snowfall(level, biome, top, maxLayers);
        }
        return snows;
    }
}
