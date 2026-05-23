package com.plusls.carpet.mixin.rule.powerBoneMeal;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.powerfulBoneMeal.Grow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class MixinBoneMealItem {
    @Inject(
            method = "growCrop",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private static void postGrowCrop(ItemStack Silian_stack, Level Silian_level, BlockPos Silian_pos, @NotNull CallbackInfoReturnable<Boolean> Silian_info) {
        if (!Silian_info.getReturnValueZ() && Silian_level instanceof ServerLevel && PluslsCarpetAdditionSettings.powerfulBoneMeal) {
            BlockState Silian_blockState = Silian_level.getBlockState(Silian_pos);
            Silian_info.setReturnValue(Grow.grow(Silian_stack, Silian_level, Silian_pos, Silian_blockState.getBlock()));
        }
    }
}
