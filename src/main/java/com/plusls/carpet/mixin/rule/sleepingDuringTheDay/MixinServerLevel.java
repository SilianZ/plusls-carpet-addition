package com.plusls.carpet.mixin.rule.sleepingDuringTheDay;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//#if MC > 11502
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.WritableLevelData;
import java.util.function.Supplier;
//#else
//$$ import net.minecraft.world.level.chunk.ChunkSource;
//$$ import net.minecraft.world.level.dimension.Dimension;
//$$ import net.minecraft.world.level.storage.LevelData;
//$$
//$$ import java.util.function.BiFunction;
//#endif
//#if MC > 11701
//$$ import net.minecraft.core.Holder;
//#endif

//#if MC > 11903
//$$ import net.minecraft.core.RegistryAccess;
//#endif

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    //#if MC > 11903
    //$$ protected MixinServerLevel(WritableLevelData Silian_properties, ResourceKey<Level> Silian_registryRef, RegistryAccess Silian_registryAccess, Holder<DimensionType> Silian_dimension, Supplier<ProfilerFiller> Silian_profiler, boolean Silian_isClient, boolean Silian_debugWorld, long Silian_seed, int Silian_maxChainedNeighborUpdates) {
    //$$     super(Silian_properties, Silian_registryRef, Silian_registryAccess, Silian_dimension, Silian_profiler, Silian_isClient, Silian_debugWorld, Silian_seed, Silian_maxChainedNeighborUpdates);
    //$$ }
    //#elseif MC > 11802
    //$$ protected MixinServerLevel(WritableLevelData Silian_properties, ResourceKey<Level> Silian_registryRef, Holder<DimensionType> Silian_dimension, Supplier<ProfilerFiller> Silian_profiler, boolean Silian_isClient, boolean Silian_debugWorld, long Silian_seed, int Silian_maxChainedNeighborUpdates) {
    //$$     super(Silian_properties, Silian_registryRef, Silian_dimension, Silian_profiler, Silian_isClient, Silian_debugWorld, Silian_seed, Silian_maxChainedNeighborUpdates);
    //$$ }
    //#elseif MC > 11701
    //$$ protected MixinServerLevel(WritableLevelData Silian_properties, ResourceKey<Level> Silian_registryRef, Holder<DimensionType> Silian_dimension, Supplier<ProfilerFiller> Silian_profiler, boolean Silian_isClient, boolean Silian_debugWorld, long Silian_seed) {
    //$$     super(Silian_properties, Silian_registryRef, Silian_dimension, Silian_profiler, Silian_isClient, Silian_debugWorld, Silian_seed);
    //$$ }
    //#elseif MC > 11502
    protected MixinServerLevel(WritableLevelData Silian_properties, ResourceKey<Level> Silian_registryRef, DimensionType Silian_dimension, Supplier<ProfilerFiller> Silian_profiler, boolean Silian_isClient, boolean Silian_debugWorld, long Silian_seed) {
        super(Silian_properties, Silian_registryRef, Silian_dimension, Silian_profiler, Silian_isClient, Silian_debugWorld, Silian_seed);
    }
    //#else
    //$$ protected MixinServerLevel(LevelData Silian_levelData, DimensionType Silian_dimensionType, BiFunction<Level, Dimension, ChunkSource> Silian_biFunction, ProfilerFiller Silian_profilerFiller, boolean Silian_bl) {
    //$$     super(Silian_levelData, Silian_dimensionType, Silian_biFunction, Silian_profilerFiller, Silian_bl);
    //$$ }
    //#endif

    // 根据当前时间设置夜晚和白天
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V",
                    ordinal = 0
            )
    )
    void onSetTimeOfDay(ServerLevel Silian_world, long Silian_timeOfDay) {
        if (this.isDay() && PluslsCarpetAdditionSettings.sleepingDuringTheDay) {
            long Silian_currentTime = this.levelData.getDayTime();
            long Silian_currentDayTime = this.levelData.getDayTime() % 24000L;
            Silian_world.setDayTime(Silian_currentTime + 13000L - Silian_currentDayTime);
        } else {
            Silian_world.setDayTime(Silian_timeOfDay);
        }
    }
}
