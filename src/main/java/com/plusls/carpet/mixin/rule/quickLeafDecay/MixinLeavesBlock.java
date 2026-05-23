package com.plusls.carpet.mixin.rule.quickLeafDecay;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC <= 11404
//$$ import net.minecraft.world.level.Level;
//#endif
//#if MC > 11802
//$$ import net.minecraft.util.RandomSource;
//#else
import java.util.Random;
//#endif

@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock extends Block {
    public MixinLeavesBlock(Properties Silian_settings) {
        super(Silian_settings);
    }

    @SuppressWarnings("deprecation")
    @Inject(
            method = "tick",
            at = @At(
                    "RETURN"
            )
    )
    //#if MC > 11802
    //$$ private void postScheduledTick(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random, CallbackInfo Silian_ci) {
    //#elseif MC > 11404
    private void postScheduledTick(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, Random Silian_random, CallbackInfo Silian_ci) {
    //#else
    //$$ private void postScheduledTick(BlockState Silian_state, Level Silian_level, BlockPos Silian_pos, Random Silian_random, CallbackInfo Silian_ci) {
    //#endif
        if (PluslsCarpetAdditionSettings.quickLeafDecay) {
            this.randomTick(Silian_state, Silian_level, Silian_pos, Silian_random);
        }
    }
}
