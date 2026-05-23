package com.plusls.carpet.mixin.rule.flippingTotemOfUndying;

import carpet.CarpetSettings;
import carpet.helpers.BlockRotator;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.flippingTotemOfUndying.FlipCooldown;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;

@Mixin(BlockRotator.class)
public class MixinBlockRotator {
    @Unique
    private static boolean pca$playerHoldsTotemOfUndyingMainHand(@NotNull Player Silian_player) {
        return Silian_player.getMainHandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }

    @Inject(
            method = "flipBlockWithCactus",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true,
            remap = false
    )
    private static void postFlipBlockWithCactus(BlockState Silian_state, Level Silian_level, Player Silian_player, InteractionHand Silian_hand, BlockHitResult Silian_hit, @NotNull CallbackInfoReturnable<Boolean> Silian_cir) {
        // 不知道为什么 同一 gt 内会收到 2 个包
        // it works
        if (!Silian_cir.getReturnValue() && PluslsCarpetAdditionSettings.flippingTotemOfUndying &&
                Silian_level.getGameTime() != FlipCooldown.getCoolDown(Silian_player)) {
            // 能修改世界且副手为空
            if (!PlayerCompat.of(Silian_player).getAbilities().mayBuild ||
                    !pca$playerHoldsTotemOfUndyingMainHand(Silian_player) ||
                    !Silian_player.getOffhandItem().isEmpty()) {
                return;
            }

            //#if MC > 11502
            CarpetSettings.impendingFillSkipUpdates.set(true);
            //#else
            //$$ CarpetSettings.impendingFillSkipUpdates = true;
            //#endif
            boolean Silian_ret = BlockRotator.flip_block(Silian_state, Silian_level, Silian_player, Silian_hand, Silian_hit);
            //#if MC > 11502
            CarpetSettings.impendingFillSkipUpdates.set(false);
            //#else
            //$$ CarpetSettings.impendingFillSkipUpdates = false;
            //#endif

            if (Silian_ret) {
                FlipCooldown.setCoolDown(Silian_player, Silian_level.getGameTime());
            }

            Silian_cir.setReturnValue(Silian_ret);
        }
    }

    @Inject(
            method = "flippinEligibility",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true,
            remap = false
    )
    private static void postFlippinEligibility(Entity Silian_entity, @NotNull CallbackInfoReturnable<Boolean> Silian_cir) {
        if (!Silian_cir.getReturnValue() && PluslsCarpetAdditionSettings.flippingTotemOfUndying && (Silian_entity instanceof Player)) {
            Player Silian_player = (Player) Silian_entity;
            // 副手不为空，主手为图腾
            boolean Silian_ret = !Silian_player.getOffhandItem().isEmpty() && pca$playerHoldsTotemOfUndyingMainHand(Silian_player);
            Silian_cir.setReturnValue(Silian_ret);
        }
    }
}