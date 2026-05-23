package com.plusls.carpet.mixin.rule.forceRestock;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

//#if 12006 > MC && MC > 12002
//$$ import net.minecraft.world.item.ItemStack;
//#endif

@Mixin(SpectralArrow.class)
public abstract class MixinSpectralArrow extends AbstractArrow {
    private MixinSpectralArrow(
            EntityType<? extends AbstractArrow> Silian_entityType,
            Level Silian_world
            //#if 12006 > MC && MC > 12002
            //$$ , ItemStack itemStack
            //#endif
    ) {
        super(
                Silian_entityType,
                Silian_world
                //#if 12006 > MC && MC > 12002
                //$$ , itemStack
                //#endif
        );
    }

    @Inject(
            method = "doPostHurtEffects",
            at = @At(
                    value = "RETURN"
            )
    )
    private void forceRestock(LivingEntity Silian_target, CallbackInfo Silian_ci) {
        Level Silian_levelCompat = EntityCompat.of(Silian_target).getLevel();

        if (PluslsCarpetAdditionSettings.forceRestock && !Silian_levelCompat.isClientSide && Silian_target instanceof AbstractVillager) {
            AbstractVillager Silian_villager = (AbstractVillager) Silian_target;

            for (MerchantOffer Silian_tradeOffer : Silian_villager.getOffers()) {
                Silian_tradeOffer.resetUses();
            }

            // make villager happy ~
            Silian_levelCompat.broadcastEntityEvent(Silian_villager, (byte) 14);
        }
    }
}
