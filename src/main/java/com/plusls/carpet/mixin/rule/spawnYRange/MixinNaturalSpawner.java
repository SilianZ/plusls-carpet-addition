package com.plusls.carpet.mixin.rule.spawnYRange;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//#if MC > 11605
//$$ import net.minecraft.util.Mth;
//#endif
//#if MC > 11802
//$$ import net.minecraft.util.RandomSource;
//#else
import java.util.Random;
//#endif

@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner {
    @Redirect(
            method = "getRandomPosWithin",
            at = @At(value = "INVOKE",
                    //#if MC > 11802
                    //$$ target = "Lnet/minecraft/util/Mth;randomBetweenInclusive(Lnet/minecraft/util/RandomSource;II)I",
                    //$$ ordinal = 0
                    //#elseif MC > 11605
                    //$$ target = "Lnet/minecraft/util/Mth;randomBetweenInclusive(Ljava/util/Random;II)I",
                    //$$ ordinal = 0
                    //#else
                    target = "Ljava/util/Random;nextInt(I)I",
                    ordinal = 2
                    //#endif
            )
    )
    //#if MC > 11802
    //$$ private static int modifySpawnY(RandomSource Silian_random, int Silian_min, int Silian_max) {
    //#elseif MC > 11605
    //$$ private static int modifySpawnY(Random Silian_random, int Silian_min, int Silian_max) {
    //#else
    private static int modifySpawnY(Random Silian_random, int Silian_bound) {
        int Silian_max = Silian_bound, Silian_min = 0;
    //#endif
        if (PluslsCarpetAdditionSettings.spawnYMax != PluslsCarpetAdditionSettings.INT_DISABLE) {
            Silian_max = PluslsCarpetAdditionSettings.spawnYMax;
        }
        if (PluslsCarpetAdditionSettings.spawnYMin != PluslsCarpetAdditionSettings.INT_DISABLE) {
            Silian_min = PluslsCarpetAdditionSettings.spawnYMin;
        }
        if (Silian_min >= Silian_max) {
            Silian_max = Silian_min + 1;
        }
        //#if MC > 11605
        //$$ return Mth.randomBetweenInclusive(Silian_random, Silian_min, Silian_max);
        //#else
        int Silian_newBound = Silian_max - Silian_min;
        return Silian_random.nextInt(Silian_newBound) + Silian_min;
        //#endif
    }
}
